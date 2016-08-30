package ru.sbt.proxy.proxy.strategies;

import ru.sbt.proxy.proxy.Cache;
import ru.sbt.proxy.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Created by Рябов Дмитрий on 30.08.2016.
 */
public class InMemoryProxyStrategy extends AbstractProxyStrategy {

	private final Map<Object, Object> resultByArg = new HashMap<>();

	@Override
	public Type getType() {
		return Type.IN_MEMORY;
	}

	@Override
	public Object getResult(Object delegate, Method method, Object[] args, Cache cache, Object[] weightyArgs, Proxy proxy) throws Throwable {
		Object key = key(method, weightyArgs);
		if (!resultByArg.containsKey(key)) {
			System.out.println("Delegation of " + method.getName());
			Object result = invoke(delegate, method, args);
			if (isPutable(result, cache)) resultByArg.put(key, result);
		}
		return resultByArg.get(key);
	}

	@Override
	protected Object key(Method method, Object[] args) {
		List<Object> key = new ArrayList<>();
		key.add(method);
		key.addAll(asList(args));
		return key;
	}
}
