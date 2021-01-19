package com.intion.fastcache.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.intion.fastcache.domian.CacheOperationType;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，18:22
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface CacheOperation {
	boolean isListOperation() default false;

	CacheOperationType operation() ;

	int primaryKeyIndex() default 0;

	int secondaryKeyIndex() default 1;
}