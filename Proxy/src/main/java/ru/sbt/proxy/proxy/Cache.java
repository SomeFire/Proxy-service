package ru.sbt.proxy.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Рябов Дмитрий on 18.08.2016.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Cache {
	Type cacheType() default Type.IN_MEMORY;
	int listList() default 0;
	String fileNamePrefix() default "";
	boolean zip() default false;
	Class[] identityBy() default {};
}
