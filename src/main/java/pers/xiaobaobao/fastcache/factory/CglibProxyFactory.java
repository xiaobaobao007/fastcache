package pers.xiaobaobao.fastcache.factory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheObject;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;
import pers.xiaobaobao.fastcache.exception.CacheKeyException;
import pers.xiaobaobao.fastcache.util.ClassTools;

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

	/**
	 * 强烈建议对dao层所有包进行初始化加载
	 */
	public static void init(String packageName) {
		try {
			ClassTools.loadClass(packageName, Cache.class);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
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
					case GET: {
						return cacheFactory.getObject(o, method, objects, methodProxy, proxyClass, hashCode, cacheOperation);
					}
					case UPDATE: {
						if (objects[cacheOperation.primaryKeyIndex()] == null) {
							return null;
						}
						CacheObject cacheObject = (CacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.update(cacheObject, hashCode, "" + proxyClass.keyFields[cacheOperation.primaryKeyIndex()].get(cacheObject), proxyClass.isListClass(), proxyClass.keyFields);
						break;
					}
					case ADD: {
						if (objects[cacheOperation.primaryKeyIndex()] == null) {
							return null;
						}
						CacheObject cacheObject = (CacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.add(cacheObject, hashCode, "" + proxyClass.keyFields[cacheOperation.primaryKeyIndex()].get(cacheObject), proxyClass.isListClass(), proxyClass.keyFields);
						break;
					}
					case DELETE: {
						if (objects[cacheOperation.primaryKeyIndex()] == null) {
							return null;
						}
						CacheObject cacheObject = (CacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.delete(cacheObject, hashCode, "" + proxyClass.keyFields[cacheOperation.primaryKeyIndex()].get(cacheObject), proxyClass.isListClass(), proxyClass.keyFields);
						break;
					}
				}
			}
		}
		return methodProxy.invokeSuper(o, objects);
	}

	public static <T> T getProxy(Class<T> daoClass) {
		System.out.println("开始加载类:" + daoClass.getName());
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(daoClass);
		enhancer.setCallback(cglibProxy);

		Cache cache = daoClass.getAnnotation(Cache.class);
		if (cache == null) {
			//noinspection unchecked
			return (T) enhancer.create();
		} else {
			cache.classz();
		}
		if ("".equals(cache.primaryKey())) {
			throw new CacheKeyException("缓存主键未设置", daoClass);
		}
		if (cache.isList() && "".equals(cache.secondaryKey())) {
			throw new CacheKeyException("缓存副键未设置", daoClass);
		}

		Field[] keyFields;
		if (cache.isList()) {
			try {
				keyFields = new Field[2];
				keyFields[1] = cache.classz().getDeclaredField(cache.secondaryKey());
				keyFields[1].setAccessible(true);
			} catch (NoSuchFieldException e) {
				throw new CacheKeyException("缓存副键不存在", cache.classz());
			}
		} else {
			keyFields = new Field[1];
		}
		try {
			keyFields[0] = cache.classz().getDeclaredField(cache.primaryKey());
			keyFields[0].setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new CacheKeyException("缓存主键不存在", cache.classz());
		}

		Method initMethod = null;
		Map<String, CacheOperation> operationMap = null;

		//noinspection unchecked
		T t = (T) enhancer.create();
		Method[] methods = daoClass.getDeclaredMethods();
		for (Method method : methods) {
			for (Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation != null) {
					if (annotation instanceof CacheOperation) {
						CacheOperation cacheOperation = (CacheOperation) annotation;
						if (cacheOperation.isListOperation() && !cache.isList()) {
							continue;
						}

						if (cacheOperation.operation() == CacheOperationType.GET) {
							if (cacheOperation.primaryKeyIndex() < 0 || cacheOperation.primaryKeyIndex() >= method.getParameterCount()) {
								throw new CacheKeyException("主键错误,提供:" + cacheOperation.primaryKeyIndex() + "共:" + method.getParameterCount(), cache.classz(), method);
							}
							if (cache.isList() && !cacheOperation.isListOperation() && (cacheOperation.secondaryKeyIndex() < 0 || cacheOperation.secondaryKeyIndex() >= method.getParameterCount())) {
								throw new CacheKeyException("附件错误,提供:" + cacheOperation.secondaryKeyIndex() + "共:" + method.getParameterCount(), cache.classz(), method);
							}
						} else if (cacheOperation.operation() == CacheOperationType.ADD
										   || cacheOperation.operation() == CacheOperationType.UPDATE
										   || cacheOperation.operation() == CacheOperationType.DELETE) {

							if (method.getParameterCount() <= cacheOperation.primaryKeyIndex()) {
								throw new CacheKeyException("主键错误,提供:" + cacheOperation.primaryKeyIndex() + "共:" + method.getParameterCount(), cache.classz(), method);
							}
							if (method.getParameterTypes()[cacheOperation.primaryKeyIndex()] != cache.classz()) {
								throw new CacheKeyException("方法参数类型错误,提供:" + method.getParameterTypes()[cacheOperation.primaryKeyIndex()], cache.classz(), method);
							}
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