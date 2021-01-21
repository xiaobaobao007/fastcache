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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheObject;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;
import pers.xiaobaobao.fastcache.domian.ProxyClass;
import pers.xiaobaobao.fastcache.exception.CacheKeyException;
import pers.xiaobaobao.fastcache.util.ClassTools;
import pers.xiaobaobao.fastcache.util.StringTools;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/18，9:50
 * <p>
 * 需要检测index是否正确
 */

public class CglibProxyFactory implements MethodInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(CglibProxyFactory.class);

	private static final CglibProxyFactory cglibProxy = new CglibProxyFactory();
	private static final CacheFactory cacheFactory = new CacheFactory();

	//缓存的代理对象
	private static final Map<String, ProxyClass> proxyClassMap = new ConcurrentHashMap<>();

	/**
	 * 强烈建议对dao层所有包进行初始化加载
	 */
	public static void init(String packageName) {
		LOG.debug("开始扫描【{}】类缓存包", packageName);
		int num = 0;
		try {
			num = ClassTools.loadClass(packageName, Cache.class);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		LOG.info("【{}】类缓存包，成功加载{}个类", packageName, num);
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
						cacheFactory.update(cacheObject, hashCode, proxyClass.getPrimaryKeyValue(cacheObject), proxyClass, true);
						break;
					}
					case ADD: {
						if (objects[cacheOperation.primaryKeyIndex()] == null) {
							return null;
						}
						CacheObject cacheObject = (CacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.add(cacheObject, hashCode, proxyClass.getPrimaryKeyValue(cacheObject), proxyClass);
						break;
					}
					case DELETE: {
						if (objects[cacheOperation.primaryKeyIndex()] == null) {
							return null;
						}
						CacheObject cacheObject = (CacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.delete(cacheObject, hashCode, proxyClass.getPrimaryKeyValue(cacheObject), proxyClass);
						break;
					}
				}
			}
		}
		return methodProxy.invokeSuper(o, objects);
	}

	public static <T> T getProxy(Class<T> daoClass) {
		LOG.debug("开始进行类缓存:【{}】", daoClass.getName());
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
		if (StringTools.isNull(cache.primaryKey())) {
			LOG.warn("【{}】缓存主键设置为空", daoClass.getName());
			throw new CacheKeyException();
		}

		boolean isListCache = !StringTools.isNull(cache.secondaryKey());

		Field[] keyFields;
		if (isListCache) {
			try {
				keyFields = new Field[2];
				keyFields[1] = cache.classz().getDeclaredField(cache.secondaryKey());
				keyFields[1].setAccessible(true);
			} catch (NoSuchFieldException e) {
				LOG.warn("【{}】缓存副键反射不到", cache.classz().getName());
				throw new CacheKeyException();
			}
		} else {
			keyFields = new Field[1];
		}
		try {
			keyFields[0] = cache.classz().getDeclaredField(cache.primaryKey());
			keyFields[0].setAccessible(true);
		} catch (NoSuchFieldException e) {
			LOG.warn("【{}】缓存主键反射不到", cache.classz().getName());
			throw new CacheKeyException();
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
						if (cacheOperation.isListOperation() && !isListCache) {
							continue;
						}

						if (cacheOperation.primaryKeyIndex() < 0 || cacheOperation.primaryKeyIndex() >= method.getParameterCount()) {
							LOG.warn("【{}-{}】，方法主键index错误，提供【{}】，上限【{}】", cache.classz().getName(), method.getName(), cacheOperation.primaryKeyIndex(), method.getParameterCount());
							throw new CacheKeyException();
						}
						if (cacheOperation.operation() == CacheOperationType.GET) {
							if (isListCache && !cacheOperation.isListOperation() && (cacheOperation.secondaryKeyIndex() < 0 || cacheOperation.secondaryKeyIndex() >= method.getParameterCount())) {
								LOG.warn("【{}-{}】，方法副键index错误，提供【{}】，上限【{}】", cache.classz().getName(), method.getName(), cacheOperation.secondaryKeyIndex(), method.getParameterCount());
								throw new CacheKeyException();
							}
						} else if (cacheOperation.operation() == CacheOperationType.ADD
								           || cacheOperation.operation() == CacheOperationType.UPDATE
								           || cacheOperation.operation() == CacheOperationType.DELETE) {

							if (method.getParameterTypes()[cacheOperation.primaryKeyIndex()] != cache.classz()) {
								LOG.warn("【{}-{}】，方法参数【{}】不匹配【{}】", cache.classz().getName(), method.getName(), method.getParameterTypes()[cacheOperation.primaryKeyIndex()], cache.classz());
								throw new CacheKeyException();
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
			proxyClassMap.put(hashCode(t), new ProxyClass(daoClass, initMethod, keyFields, operationMap));
		}

		return null;
	}

	//因为hashcode方法被代理，会栈溢出
	private static String hashCode(Object object) {
		return ("" + System.identityHashCode(object)).intern();
	}

}