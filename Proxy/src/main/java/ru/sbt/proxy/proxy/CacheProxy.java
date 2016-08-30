package ru.sbt.proxy.proxy;

import ru.sbt.proxy.proxy.strategies.ProxyStrategy;

import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * Created by Рябов Дмитрий on 18.08.2016.
 */
public class CacheProxy {

    public static <T> T cache(Object delegate, String defaultPath, ProxyStrategy... strategies) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(getSystemClassLoader(),
                delegate.getClass().getInterfaces(),
                new Proxy(delegate, defaultPath, strategies)
        );
    }
}
