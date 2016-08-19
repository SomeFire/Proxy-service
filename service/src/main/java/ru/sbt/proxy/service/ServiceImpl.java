package ru.sbt.proxy.service;

import ru.sbt.proxy.proxy.Cache;
import ru.sbt.proxy.proxy.CacheProxy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.sbt.proxy.proxy.Type.FILE;

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
		Service s = CacheProxy.cache(new ServiceImpl(), "");
		s.run(null, 0d, null);
		s.work(null);
	}
}
