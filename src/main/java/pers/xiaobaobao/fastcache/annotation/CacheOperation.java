package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import pers.xiaobaobao.fastcache.domian.CacheOperationType;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，18:22
 */
@Inherited
@Retention(RUNTIME)
@Target({METHOD})
public @interface CacheOperation {
	boolean isListOperation() default false;

	CacheOperationType operation() default CacheOperationType.NULL;

	int primaryKeyIndex() default 0;

	int secondaryKeyIndex() default 1;
}