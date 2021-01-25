package pers.xiaobaobao.fastcache.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义在dao层上
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 1.0
 * @date 2021/1/16，18:22
 */
@Inherited
@Retention(RUNTIME)
@Target({TYPE})
public @interface Cache {

	/**
	 * 包的地址：以“.”的形式分割，根据地址搜索dao对象对应的po类，
	 * 实现为{@link pers.xiaobaobao.fastcache.util.ClassTools#getDaoToPo(String, Class)}
	 *
	 * <p>
	 * 可以写dao层对应的po类
	 * 或者写po上层包的地址,上层
	 */
	String location();

	/**
	 * @return 主键，
	 * 在女友类（GirlFriend）中 对应userId
	 * 在宠物类（Pet）中 uid
	 */
	String primaryKey();

	/**
	 * 只要在dao层类上定义此名字，即默认dao类对应的po类是一对多的关系
	 *
	 * @return 副键，在宠物类（Pet）中 id
	 */
	String secondaryKey() default "";
}