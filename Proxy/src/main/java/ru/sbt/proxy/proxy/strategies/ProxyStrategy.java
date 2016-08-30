package ru.sbt.proxy.proxy.strategies;

import ru.sbt.proxy.proxy.Cache;
import ru.sbt.proxy.proxy.Proxy;

import java.lang.reflect.Method;

/**
 * Created by Рябов Дмитрий on 30.08.2016.
 */
public interface ProxyStrategy {
	Type getType();
	Object getResult(Object delegate, Method method, Object[] args, Cache cache, Object[] weightyArgs, Proxy proxy) throws Throwable;
}
