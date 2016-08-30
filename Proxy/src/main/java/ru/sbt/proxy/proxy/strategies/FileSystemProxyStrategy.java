package ru.sbt.proxy.proxy.strategies;

import ru.sbt.proxy.proxy.Cache;
import ru.sbt.proxy.proxy.Proxy;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.asList;

/**
 * Created by Рябов Дмитрий on 30.08.2016.
 */
public class FileSystemProxyStrategy extends AbstractProxyStrategy {

	@Override
	public Type getType() {
		return Type.FILE;
	}

	@Override
	public Object getResult(Object delegate, Method method, Object[] args, Cache cache, Object[] weightyArgs, Proxy proxy) throws Throwable {
		String path = cache.fileNamePrefix().equals("") ? proxy.getDefaultPath() : cache.fileNamePrefix();
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
			System.err.println("Can't write object. Implement 'Serializable' interface.");
		} catch (ClassNotFoundException e) {
			System.err.println("Из файла считан несуществующий класс. " +
					"Возможно, повреждён .class файл или не указан serialVersionUID.");
		} catch (ClassCastException e) {
			System.err.println("Из файла считан неподходящий объект. " +
					"Убедитесь, что записывается и считывается объект одного класса.");
		} catch (IOException e) {
			System.err.println("Can't read file.");
		}
		if (resultsFromFile == null) resultsFromFile = new HashMap<>();
		if (result == null) {
			System.out.println("Delegation of " + method.getName());
			result = invoke(delegate, method, args);
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

	@Override
	protected Object key(Method method, Object[] args) {
		List<Object> key = new ArrayList<>();
		key.add(method);
		key.addAll(asList(args));
		return key;
	}
}
