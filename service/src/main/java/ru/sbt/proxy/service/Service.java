package ru.sbt.proxy.service;

import ru.sbt.proxy.proxy.Cache;

import java.util.Date;
import java.util.List;

import static ru.sbt.proxy.proxy.Type.*;

/**
 * Created by Рябов Дмитрий on 18.08.2016.
 */
public interface Service {
	@Cache(cacheType = FILE, fileNamePrefix = "data", zip = true, identityBy = {String.class, double.class})
	List<String> run(String item, double value, Date date);

	@Cache(cacheType = IN_MEMORY, listList = 100_000)
	List<String> work(String item);

}
