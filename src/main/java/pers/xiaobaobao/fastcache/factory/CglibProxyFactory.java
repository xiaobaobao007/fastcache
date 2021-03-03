package pers.xiaobaobao.fastcache.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.annotation.Id;
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;
import pers.xiaobaobao.fastcache.domian.ProxyClass;
import pers.xiaobaobao.fastcache.exception.CacheKeyException;
import pers.xiaobaobao.fastcache.util.ClassTools;
import pers.xiaobaobao.fastcache.util.StringTools;

/**
 * 代理对象操作类
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.3
 * @date 2021/2/22，11:00
 */

@SuppressWarnings("unchecked")
public class CglibProxyFactory implements MethodInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(CglibProxyFactory.class);

	private static final CglibProxyFactory cglibProxy = new CglibProxyFactory();
	private static final CacheFactory cacheFactory = new CacheFactory();

	//缓存的代理对象
	protected static final Map<String, ProxyClass> proxyClassMap = new HashMap<>();
	protected static final Map<Class<?>, String> beProxyClassHashCode = new HashMap<>();

	public static void init(String packageName) {
		init(packageName, true);
	}

	/**
	 * 强烈建议对dao层所有包先进行初始化加载！！！！！！！！！！！！！！！！！！！！！！！！！！
	 *
	 * @param packageName 包名
	 * @param detailed    是否输出详细的加载类信息
	 */
	public static void init(String packageName, boolean detailed) {
		LOG.debug("开始扫描【{}】包下的类缓存", packageName);
		List<Class<?>> classList = ClassTools.loadClassByAnnotation(packageName, Cache.class);
		if (detailed) {
			LOG.info("【{}】包缓存类，成功加载【{}个】:【{}】", packageName, classList.size(), classList);
		} else {
			StringBuilder sb = new StringBuilder();
			for (Class<?> cl : classList) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(cl.getSimpleName());
			}
			LOG.info("【{}】包缓存类，成功加载【{}个】:【{}】", packageName, classList.size(), sb.toString());
		}
	}

	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		//已经完成了计算
		boolean hadDo = false;
		//结果
		Object result = null;
		//发生了错误
		Exception resultException = null;

		ProxyClass proxyClass;
		String hashCode = hashCode(o);
		if ((proxyClass = proxyClassMap.get(hashCode)) != null) {
			CacheOperation cacheOperation = proxyClass.operationMap.get(method.getName());
			if (cacheOperation != null) {

				if (objects[cacheOperation.primaryKeyIndex()] == null) {
					LOG.error("{},【{}-{}】，传入参数为空，取消执行", cacheOperation.operation().name(), proxyClass.beProxyClass.getName(), method.getName());
					return null;
				}
				if (cacheOperation.operation() != CacheOperationType.GET) {
					try {
						//除了查找，其余操作都先执行数据库操作，
						//为了防止数据回滚，但是已经插入缓存的错误
						hadDo = true;
						result = methodProxy.invokeSuper(o, objects);
					} catch (Exception e) {
						LOG.error("fastcache出现错误",e);
						resultException = e;
						if (cacheOperation.operation() != CacheOperationType.UPDATE) {
							return null;
						}
					}
				}

				switch (cacheOperation.operation()) {
					case GET: {
						return cacheFactory.getObject(o, method, objects, methodProxy, proxyClass, hashCode, cacheOperation);
					}
					case UPDATE: {
						FastCacheBaseCacheObject fastCacheBaseCacheObject = (FastCacheBaseCacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.update(fastCacheBaseCacheObject, hashCode, proxyClass.getPrimaryKeyValue(fastCacheBaseCacheObject), proxyClass,
								true, resultException != null);
						if (resultException != null) {
							throw resultException;
						}
						break;
					}
					case ADD: {
						FastCacheBaseCacheObject fastCacheBaseCacheObject = (FastCacheBaseCacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.add(fastCacheBaseCacheObject, hashCode, proxyClass.getPrimaryKeyValue(fastCacheBaseCacheObject), proxyClass);
						break;
					}
					case DELETE: {
						FastCacheBaseCacheObject fastCacheBaseCacheObject = (FastCacheBaseCacheObject) objects[cacheOperation.primaryKeyIndex()];
						cacheFactory.delete(fastCacheBaseCacheObject, hashCode, proxyClass.getPrimaryKeyValue(fastCacheBaseCacheObject), proxyClass);
						break;
					}
				}
			}
		}

		if (hadDo) {
			return result;
		}
		return methodProxy.invokeSuper(o, objects);
	}

	/**
	 * 获得dao层的单例对象
	 */
	public static <T> T getProxy(Class<T> daoClass) {
		// LOG.debug("开始进行类缓存:【{}】", daoClass.getName());
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(daoClass);
		enhancer.setCallback(cglibProxy);

		Cache cache = daoClass.getAnnotation(Cache.class);
		if (cache == null) {
			return (T) enhancer.create();
		}
		Class<?> poClass = null;
		try {
			poClass = ClassTools.getDaoToPo(cache.location(), daoClass);
		} catch (Exception ignored) {
		}
		if (poClass == null) {
			LOG.warn("【{}】无法根据【{}】匹配或者未扫描到对应PO类", daoClass.getName(), cache.location());
			return (T) enhancer.create();
		}

		// LOG.debug("【{}】成功匹配到【{}】", daoClass.getName(), poClass.getName());
		if (StringTools.isNull(cache.primaryKey())) {
			LOG.warn("【{}】缓存主键设置为空", daoClass.getName());
			throw new CacheKeyException();
		}

		boolean isListCache = !StringTools.isNull(cache.secondaryKey());

		Field[] keyFields;
		Field idField = null;
		if (isListCache) {
			try {
				keyFields = new Field[2];
				keyFields[1] = poClass.getDeclaredField(cache.secondaryKey());
				keyFields[1].setAccessible(true);
			} catch (NoSuchFieldException e) {
				LOG.warn("【{}】缓存副键反射不到", poClass.getName());
				throw new CacheKeyException();
			}

			for (Field field : poClass.getDeclaredFields()) {
				if (field.getAnnotation(Id.class) != null) {
					idField = field;
					idField.setAccessible(true);
				}
			}

			if (idField != null) {
				LOG.debug("【{}】配置id标签域【{}】", poClass.getName(), idField.getName());
			}
		} else {
			keyFields = new Field[1];
		}
		try {
			keyFields[0] = poClass.getDeclaredField(cache.primaryKey());
			keyFields[0].setAccessible(true);
		} catch (NoSuchFieldException e) {
			LOG.warn("【{}】缓存主键反射不到", poClass.getName());
			throw new CacheKeyException();
		}

		Method initListMethod = null;
		Map<String, CacheOperation> operationMap = null;

		T t = (T) enhancer.create();
		Method[] methods = daoClass.getMethods();
		for (Method method : methods) {
			for (Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation != null) {
					if (annotation instanceof CacheOperation) {
						CacheOperation cacheOperation = (CacheOperation) annotation;
						if (cacheOperation.isListOperation() && !isListCache) {
							continue;
						}

						if (cacheOperation.primaryKeyIndex() < 0 || cacheOperation.primaryKeyIndex() >= method.getParameterCount()) {
							LOG.warn("【{}-{}】，方法主键index错误，提供【{}】，上限【{}】", poClass.getName(), method.getName(), cacheOperation.primaryKeyIndex(), method.getParameterCount());
							throw new CacheKeyException();
						}
						if (cacheOperation.operation() == CacheOperationType.GET) {
							if (isListCache && !cacheOperation.isListOperation() && (cacheOperation.secondaryKeyIndex() < 0 || cacheOperation.secondaryKeyIndex() >= method.getParameterCount())) {
								LOG.warn("【{}-{}】，方法副键index错误，提供【{}】，上限【{}】", poClass.getName(), method.getName(), cacheOperation.secondaryKeyIndex(), method.getParameterCount());
								throw new CacheKeyException();
							}
						}

						if (operationMap == null) {
							operationMap = new HashMap<>();
						}
						operationMap.put(method.getName(), cacheOperation);
					} else if (annotation instanceof CacheInitList) {
						initListMethod = method;
					}
				}
			}
		}

		if (operationMap != null) {
			String hashCode = hashCode(t);
			proxyClassMap.put(hashCode, new ProxyClass(t, poClass, initListMethod, keyFields, operationMap, idField));
			beProxyClassHashCode.put(poClass, hashCode);
		}
		return t;
	}

	//因为hashcode方法被代理，所以需要自己实现，否则会栈溢出
	private static String hashCode(Object object) {
		// return object.hashCode();
		return ("" + System.identityHashCode(object)).intern();
	}

}