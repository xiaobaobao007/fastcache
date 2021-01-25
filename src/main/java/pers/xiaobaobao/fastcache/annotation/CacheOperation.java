package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import pers.xiaobaobao.fastcache.domian.CacheOperationType;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 缓存方法操作对象
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 1.0
 * @date 2021/1/16，18:22
 */
@Inherited
@Retention(RUNTIME)
@Target({METHOD})
public @interface CacheOperation {
	/**
	 * 注意，{@link Cache#secondaryKey()},只有类是一对多的关系，方法的list方法才有效
	 *
	 * @return 是否是操作list的方法，
	 */
	boolean isListOperation() default false;

	/**
	 * @return 操作类型，增删改查
	 */
	CacheOperationType operation() default CacheOperationType.NULL;

	/**
	 * @return 方法参数主键的位置，支持自定义位置，用于架构的融合，更好的控制
	 */
	int primaryKeyIndex() default 0;

	/**
	 * @return 方法参数副键的位置，支持自定义位置，用于架构的融合，更好的控制
	 */
	int secondaryKeyIndex() default 1;
}