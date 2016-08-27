package ru.sbt.proxy.proxy;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Arrays.asList;
import static ru.sbt.proxy.proxy.Type.FILE;

/**
 * Created by Рябов Дмитрий on 18.08.2016.
 */
public class CacheProxy implements InvocationHandler {
//TODO CacheProxy не должен быть InvocationHandler
    private final Map<Object, Object> resultByArg = new HashMap<>();
    private final Object delegate;

    private final String defaultPath;

    public CacheProxy(Object delegate, String defaultPath) {
        this.delegate = delegate;
        this.defaultPath = defaultPath != null ? defaultPath : "";
    }

    public static <T> T cache(Object delegate, String defaultPath) {
        return (T) Proxy.newProxyInstance(getSystemClassLoader(),
                delegate.getClass().getInterfaces(),
                new CacheProxy(delegate, defaultPath)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.isAnnotationPresent(Cache.class)) return invoke(method, args);

        System.out.println("--------------------------");
        System.out.println(method.getName() + ": cache invoke");
        Cache cache = method.getAnnotation(Cache.class);
        Object[] weightyArgs = checkArgs(method, args, cache.identityBy());

        if (cache.cacheType() == FILE) return objectFromFileSystem(method, args, cache, weightyArgs);
        else return objectFromMemory(method, args, cache, weightyArgs);
    }

    //TODO Вынести в отдельный интерфейс.
    private Object objectFromFileSystem(Method method, Object[] args, Cache cache, Object[] weightyArgs) throws Throwable {
        String path = cache.fileNamePrefix().equals("") ? defaultPath : cache.fileNamePrefix();
        Map<Object, Object> resultsFromFile = null;
        Object result = null;
        try (ObjectInputStream ois = new ObjectInputStream(
                cache.zip() ?
                        new FileInputStream(path + method.getName()) :
                        new ZipInputStream(new FileInputStream(path + method.getName())))) {
            resultsFromFile = (Map<Object, Object>) ois.readObject();
            result = resultsFromFile.get(asList(weightyArgs));
        } catch (NullPointerException ignore) {
        } catch (NotSerializableException e) {
            throw new RuntimeException("Can't write object. Implement 'Serializable' interface.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Из файла считан несуществующий класс. " +
                    "Возможно, повреждён .class файл или не указан serialVersionUID.");
        } catch (ClassCastException e) {
            throw new RuntimeException("Из файла считан неподходящий объект. " +
                    "Убедитесь, что записывается и считывается объект одного класса.");
        } catch (IOException e) {
            throw new RuntimeException("Can't read file.");
        }
        if (resultsFromFile == null) resultsFromFile = new HashMap<>();
        if (result == null) {
            System.out.println("Delegation of " + method.getName());
            result = invoke(method, args);
            if (isPutable(result, cache)) {
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        cache.zip() ?
                                new FileOutputStream(path + method.getName(), true) :
                                new ZipOutputStream(new FileOutputStream(path + method.getName(), true)))) {
                    resultsFromFile.put(asList(weightyArgs), result);
                    oos.writeObject(resultsFromFile);
                } catch (NotSerializableException e) {
                    throw new RuntimeException("Can't write object. Implement 'Serializable' interface.");
                } catch (IOException e) {
                    throw new RuntimeException("Can't create file. Ensure, that you have rights to write files in path.");
                }
            }
        }
        return result;
    }

    private Object objectFromMemory(Method method, Object[] args, Cache cache, Object[] weightyArgs) throws Throwable {
        Object key = key(method, weightyArgs);
        if (!resultByArg.containsKey(key)) {
            System.out.println("Delegation of " + method.getName());
            Object result = invoke(method, args);
            if (isPutable(result, cache)) resultByArg.put(key, result);
        }
        return resultByArg.get(key);
    }

    private boolean isPutable(Object o, Cache cache) {
        if (o instanceof List && (((List) o).size() > cache.listList())) {
            return false;
        } else
            return true;
    }

    private Object invoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object[] checkArgs(Method method, Object[] args, Class[] availableClasses) {
        if (availableClasses.length == 0) {
            return args;
        } else {
            List<Object> tmp = new ArrayList<>();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                for (int j = 0; j < availableClasses.length; j++) {
                    if (method.getParameterTypes()[i] == availableClasses[j]) {
                        tmp.add(args[i]);
                        break;
                    }
                }
            }
            return tmp.toArray();
        }
    }

    private Object key(Method method, Object[] args) {
        List<Object> key = new ArrayList<>();
        key.add(method);
        key.addAll(asList(args));
        return key;
    }
}
