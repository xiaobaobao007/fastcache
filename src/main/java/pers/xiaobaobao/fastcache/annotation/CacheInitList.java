package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 一对多的关系下，明确是取全部list的方法
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 1.0
 * @date 2021/1/18，17:01
 */
@Inherited
@Retention(RUNTIME)
@Target({METHOD})
public @interface CacheInitList {
}
