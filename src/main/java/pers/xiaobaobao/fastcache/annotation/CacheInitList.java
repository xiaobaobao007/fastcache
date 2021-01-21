package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/18，17:01
 */
@Inherited
@Retention(RUNTIME)
@Target({METHOD})
public @interface CacheInitList {
}