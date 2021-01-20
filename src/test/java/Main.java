import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.reflect.Method;

import pers.xiaobaobao.fastcache.annotation.CacheInitList;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author xiaobaobao
 * @date 2021/1/20，21:24
 */
public class Main {
	public static void main(String[] args) {
		for (Method method : bbb.class.getMethods()) {
			System.out.println(method.getName() + " " + method.isAnnotationPresent(CacheInitList.class));
		}
	}
}

// public class Main {
//
// 	@Inherited  //可以被继承
// 	@Retention(RUNTIME)   //可以通过反射读取注解
// 	public @interface MyAnnotation {
// 		String value();
// 	}
//
// 	@MyAnnotation(value = "类名上的注解")
// 	public abstract class ParentClass {
//
// 		@MyAnnotation(value = "父类的abstractMethod方法")
// 		public abstract void abstractMethod();
//
// 		@MyAnnotation(value = "父类的doExtends方法")
// 		public void doExtends() {
// 			System.out.println(" ParentClass doExtends ...");
// 		}
//
// 		@MyAnnotation(value = "父类的doHandle方法")
// 		public void doHandle() {
// 			System.out.println(" ParentClass doHandle ...");
// 		}
// 	}
//
// 	public class SubClass extends ParentClass {
//
// 		@Override
// 		public void abstractMethod() {
// 			System.out.println("子类实现父类的abstractMethod抽象方法");
// 		}
//
// 		@Override
// 		public void doHandle() {
// 			System.out.println("子类覆盖父类的doHandle方法");
// 		}
// 	}
//
// 	public static void main(String[] args) throws SecurityException, NoSuchMethodException {
//
// 		Class<SubClass> clazz = SubClass.class;
//
// 		if (clazz.isAnnotationPresent(MyAnnotation.class)) {
// 			MyAnnotation cla = clazz.getAnnotation(MyAnnotation.class);
// 			System.out.println("子类继承到父类类上Annotation,其信息如下：" + cla.value());
// 		} else {
// 			System.out.println("子类没有继承到父类类上Annotation");
// 		}
//
// 		// 实现抽象方法测试
// 		Method method = clazz.getMethod("abstractMethod");
// 		if (method.isAnnotationPresent(MyAnnotation.class)) {
// 			MyAnnotation ma = method.getAnnotation(MyAnnotation.class);
// 			System.out.println("子类实现父类的abstractMethod抽象方法，继承到父类抽象方法中的Annotation,其信息如下：" + ma.value());
// 		} else {
// 			System.out.println("子类实现父类的abstractMethod抽象方法，没有继承到父类抽象方法中的Annotation");
// 		}
//
// 		//继承测试
// 		Method methodOverride = clazz.getMethod("doExtends");
// 		if (methodOverride.isAnnotationPresent(MyAnnotation.class)) {
// 			MyAnnotation ma = methodOverride.getAnnotation(MyAnnotation.class);
// 			System.out.println("子类继承父类的doExtends方法，继承到父类doExtends方法中的Annotation,其信息如下：" + ma.value());
// 		} else {
// 			System.out.println("子类继承父类的doExtends方法，没有继承到父类doExtends方法中的Annotation");
// 		}
//
// 		//覆盖测试
// 		Method method3 = clazz.getMethod("doHandle");
// 		if (method3.isAnnotationPresent(MyAnnotation.class)) {
// 			MyAnnotation ma = method3.getAnnotation(MyAnnotation.class);
// 			System.out.println("子类覆盖父类的doHandle方法，继承到父类doHandle方法中的Annotation,其信息如下：" + ma.value());
// 		} else {
// 			System.out.println("子类覆盖父类的doHandle方法，没有继承到父类doHandle方法中的Annotation");
// 		}
// 	}
// }
