package ru.sbt.proxy.proxy.strategies;

import ru.sbt.proxy.proxy.Cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Рябов Дмитрий on 30.08.2016.
 */
public abstract class AbstractProxyStrategy implements ProxyStrategy {


	protected boolean isPutable(Object o, Cache cache) {
		return !(o instanceof List && (((List) o).size() > cache.listList()));
	}

	protected Object invoke(Object delegate, Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(delegate, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Impossible");
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	abstract protected Object key(Method method, Object[] args);
}
