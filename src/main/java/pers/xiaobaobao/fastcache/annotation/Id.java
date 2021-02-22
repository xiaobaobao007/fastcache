package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义在dao层上
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.3
 * @date 2021/2/22，11:00
 */
@Inherited
@Retention(RUNTIME)
@Target({FIELD})
public @interface Id {
}