package ru.sbt.proxy.service;

import ru.sbt.proxy.proxy.CacheProxy;
import ru.sbt.proxy.proxy.strategies.FileSystemProxyStrategy;
import ru.sbt.proxy.proxy.strategies.InMemoryProxyStrategy;

import java.util.Date;
import java.util.List;

/**
 * Created by Рябов Дмитрий on 19.08.2016.
 */
public class ServiceImpl implements Service{
	@Override
	public List<String> run(String item, double value, Date date) {
		return null;
	}

	@Override
	public List<String> work(String item) {
		return null;
	}

	public static void main(String[] args) {
		Service s = CacheProxy.cache(new ServiceImpl(), "", new InMemoryProxyStrategy(), new FileSystemProxyStrategy());
		s.run(null, 0d, null);
		s.work(null);
	}
}
