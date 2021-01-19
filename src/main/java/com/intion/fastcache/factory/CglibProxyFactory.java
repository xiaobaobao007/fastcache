package com.intion.fastcache.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.AnnotationUtils;

import com.intion.fastcache.annotation.Cache;
import com.intion.fastcache.annotation.CacheInitList;
import com.intion.fastcache.annotation.CacheOperation;
import com.intion.fastcache.domian.CacheObject;
import com.intion.fastcache.exception.CacheKeyException;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.reflections.Reflections;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/18，9:50
 * <p>
 * 需要检测index是否正确
 */

public class CglibProxyFactory implements MethodInterceptor {

	private static final CglibProxyFactory cglibProxy = new CglibProxyFactory();
	private static final CacheFactory cacheFactory = new CacheFactory();

	//缓存的代理对象
	private static final Map<String, ProxyClass> proxyClassMap = new ConcurrentHashMap<>();

	//需要检测的注解对象
	private static final List<Class<? extends Annotation>> ANNOTATION_LIST = new ArrayList<>();

	static {
		ANNOTATION_LIST.add(CacheInitList.class);
		ANNOTATION_LIST.add(CacheOperation.class);
	}

	public static void init(String packageName) throws ClassNotFoundException {
		Reflections reflections;
		if ("".equals(packageName)) {
			reflections = new Reflections();
		} else {
			reflections = new Reflections(packageName);
		}
		Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Cache.class);
		for (Class<?> classZ : classesList) {
			Class.forName(classZ.getName());
		}
	}

	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		ProxyClass proxyClass;
		String hashCode = hashCode(o);
		if ((proxyClass = proxyClassMap.get(hashCode)) != null) {
			CacheOperation cacheOperation = proxyClass.operationMap.get(method.getName());
			if (cacheOperation != null) {
				switch (cacheOperation.operation()) {
					case GET:
						if (cacheOperation.primaryKeyIndex() < objects.length) {
							return cacheFactory.getObject(o, method, objects, methodProxy, proxyClass, hashCode, cacheOperation);
						}
						break;
					case UPDATE_ADD: {
						CacheObject cacheObject = (CacheObject) objects[0];
						cacheFactory.updateOrAdd(cacheObject, hashCode, "" + proxyClass.keyFields[0].get(cacheObject), proxyClass.isListClass(), proxyClass.keyFields);
						break;
					}
					case DELETE: {
						CacheObject cacheObject = (CacheObject) objects[0];
						cacheFactory.delete(cacheObject, hashCode, "" + proxyClass.keyFields[0].get(cacheObject), proxyClass.isListClass(), proxyClass.keyFields);
						break;
					}
				}
			}
		}
		return methodProxy.invokeSuper(o, objects);
	}

	public static <T> T getProxy(Class<T> classz) {
		System.out.println(classz.getName());
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(classz);
		enhancer.setCallback(cglibProxy);

		Cache cache = classz.getAnnotation(Cache.class);
		if (cache == null) {
			//noinspection unchecked
			return (T) enhancer.create();
		}
		if ("".equals(cache.primaryKey())) {
			throw new CacheKeyException("缓存主键未设置", classz);
		}
		if (cache.isList() && "".equals(cache.secondaryKey())) {
			throw new CacheKeyException("缓存副键未设置", classz);
		}

		Field[] keyFields;
		if (cache.isList()) {
			try {
				keyFields = new Field[2];
				keyFields[1] = classz.getDeclaredField(cache.secondaryKey());
				keyFields[1].setAccessible(true);
			} catch (NoSuchFieldException e) {
				throw new CacheKeyException("缓存副键不存在", classz);
			}
		} else {
			keyFields = new Field[1];
		}
		try {
			keyFields[0] = classz.getDeclaredField(cache.primaryKey());
			keyFields[0].setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new CacheKeyException("缓存主键不存在", classz);
		}

		Method initMethod = null;
		Map<String, CacheOperation> operationMap = null;
		//noinspection unchecked
		T t = (T) enhancer.create();
		Method[] methods = classz.getDeclaredMethods();
		Object classObj = null;
		for (Method method : methods) {
			Annotation annotation;
			for (Class<? extends Annotation> clazz : ANNOTATION_LIST) {
				annotation = AnnotationUtils.findAnnotation(method, clazz);
				if (annotation != null) {
					if (annotation instanceof CacheOperation) {
						CacheOperation cacheOperation = (CacheOperation) annotation;
						if (cacheOperation.primaryKeyIndex() < 0 || cacheOperation.primaryKeyIndex() >= keyFields.length) {
							throw new CacheKeyException("主键错误,提供:" + cacheOperation.primaryKeyIndex() + "上限:" + keyFields.length, classz);
						}
						if (cache.isList() && (cacheOperation.secondaryKeyIndex() < 0 || cacheOperation.secondaryKeyIndex() >= keyFields.length)) {
							throw new CacheKeyException("附件错误,提供:" + cacheOperation.secondaryKeyIndex() + "上限:" + keyFields.length, classz);
						}
						if (cacheOperation.isListOperation() && !cache.isList()) {
							continue;
						}
						if (operationMap == null) {
							operationMap = new HashMap<>();
						}
						operationMap.put(method.getName(), cacheOperation);
					} else if (annotation instanceof CacheInitList) {
						initMethod = method;
					}
				}
			}
		}

		if (operationMap != null) {
			proxyClassMap.put(hashCode(t), new ProxyClass(initMethod, keyFields, operationMap));
		}

		return t;
	}

	//因为hashcode方法被代理，会栈溢出
	private static String hashCode(Object object) {
		return ("" + System.identityHashCode(object)).intern();
	}

	private static String getParamsKey(Object[] objects, int primaryKeyIndex, int secondaryKeyIndex) {
		if (primaryKeyIndex < objects.length) {
			if (secondaryKeyIndex < objects.length) {
				return objects[primaryKeyIndex] + "_" + objects[secondaryKeyIndex];
			} else {
				return "" + objects[primaryKeyIndex];
			}
		}
		throw new IndexOutOfBoundsException();
	}

	protected static class ProxyClass {
		protected final Method initMethod;
		protected MethodProxy methodProxy;
		protected final Field[] keyFields;
		protected final Map<String, CacheOperation> operationMap;

		public ProxyClass(Method initMethod, Field[] keyFields, Map<String, CacheOperation> operationMap) {
			this.initMethod = initMethod;
			this.keyFields = keyFields;
			this.operationMap = operationMap;
		}

		public String getPrimaryKeyValue(Object object) throws IllegalAccessException {
			return "" + keyFields[0].get(object);
		}

		public Object getSecondaryKeyValue(Object object) throws IllegalAccessException {
			return keyFields[1].get(object);
		}

		/**
		 * 应该用{@link Cache#isList()}进行判断，但是用keyFields.length刚好也能实现相同效果，还更简便
		 */
		public boolean isListClass() {
			return keyFields.length == 2;
		}
	}

}