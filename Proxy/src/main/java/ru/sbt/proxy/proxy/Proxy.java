package ru.sbt.proxy.proxy;

import ru.sbt.proxy.proxy.strategies.ProxyStrategy;
import ru.sbt.proxy.proxy.strategies.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Рябов Дмитрий on 30.08.2016.
 */
public class Proxy implements InvocationHandler {
	private final Object delegate;
	private final String defaultPath;
	private final EnumMap<Type, ProxyStrategy> strategies;

	public Object getDelegate() {
		return delegate;
	}

	public String getDefaultPath() {
		return defaultPath;
	}

	Proxy(Object delegate, String defaultPath, ProxyStrategy... strategies) {
		this.delegate = delegate;
		this.defaultPath = defaultPath != null ? defaultPath : "";
		this.strategies = new EnumMap<>(Arrays.stream(strategies)
				.collect(Collectors.toMap(ProxyStrategy::getType, p -> p)));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!method.isAnnotationPresent(Cache.class)) return invoke(method, args);

		Cache cache = method.getAnnotation(Cache.class);
		Object[] weightyArgs = getWeightyArgs(method, args, cache.identityBy());
		return strategies.get(cache.cacheType()).getResult(delegate, method, args, cache, weightyArgs, this);
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

	private Object[] getWeightyArgs(Method method, Object[] args, Class[] availableClasses) {
		if (availableClasses.length == 0) return args;
		List<Object> tmp = new ArrayList<>();
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			for (Class availableClass : availableClasses) {
				if (method.getParameterTypes()[i] == availableClass) {
					tmp.add(args[i]);
					break;
				}
			}
		}
		return tmp.toArray();
	}
}