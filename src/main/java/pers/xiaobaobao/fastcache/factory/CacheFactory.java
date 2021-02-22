package pers.xiaobaobao.fastcache.factory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;
import pers.xiaobaobao.fastcache.domian.CacheQueueAndMaxId;
import pers.xiaobaobao.fastcache.domian.ProxyClass;
import pers.xiaobaobao.fastcache.util.StringTools;

/**
 * 缓存操作工厂
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.3
 * @date 2021/2/22，11:00
 */
public class CacheFactory {

	private static final Logger LOG = LoggerFactory.getLogger(CacheFactory.class);

	//一对一缓存对象
	private static final Map<String, Map<String, FastCacheBaseCacheObject>> classOneCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cache>>
	//一对多缓存对象
	private static final Map<String, Map<String, Map<String, FastCacheBaseCacheObject>>> classMoreCacheMap = new ConcurrentHashMap<>();//<class,<pKey,<sKey,cache>>>
	private static final Map<String, Map<String, CacheQueueAndMaxId>> classListCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cacheList>>

	//空对象
	private static final FastCacheBaseCacheObject NULL_CACHE_OBJECT = new FastCacheBaseCacheObject() {
	};

	/**
	 * @param clazz dao层对应的pojo类
	 * @param key   主键值
	 * @return 需要自己进行强转, 类型由pojo的pKey决定
	 */
	public static long getMaxId(Class<?> clazz, Object key) {
		String pKey = key.toString();
		String hashCode = CglibProxyFactory.beProxyClassHashCode.get(clazz);
		if (StringTools.isNull(hashCode)) {
			LOG.error("{}未被代理", clazz.getName());
			return -1;
		}
		ProxyClass proxyClass = CglibProxyFactory.proxyClassMap.get(hashCode);
		if (proxyClass == null) {
			LOG.error("{}被代理了，但是代理类未找到", clazz.getName());
			return -2;
		}
		Map<String, CacheQueueAndMaxId> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, (k) -> new ConcurrentHashMap<>());
		CacheQueueAndMaxId cacheQueueAndMaxId = pKeyListCacheMap.get(pKey);
		synchronized (("MAX_ID_" + hashCode + pKey).intern()) {
			if (cacheQueueAndMaxId == null) {
				if ((cacheQueueAndMaxId = pKeyListCacheMap.get(pKey)) == null) {
					try {
						Object invokeResult = proxyClass.getInitList(key);
						cacheQueueAndMaxId = getCacheQueueAndMaxId(pKey, hashCode, proxyClass, pKeyListCacheMap, invokeResult);
					} catch (Exception e) {
						LOG.error("反射出错", e);
						return -3;
					}
				}
				pKeyListCacheMap.put(pKey, cacheQueueAndMaxId);
			}

			long maxId = cacheQueueAndMaxId.getMaxId();
			cacheQueueAndMaxId.setMaxId(maxId + 1);
		}
		return cacheQueueAndMaxId.getMaxId();
	}

	private static CacheQueueAndMaxId getCacheQueueAndMaxId(String pKey, String hashCode, ProxyClass proxyClass, Map<String, CacheQueueAndMaxId> pKeyListCacheMap, Object invokeResult) throws IllegalAccessException {
		CacheQueueAndMaxId cacheQueueAndMaxId;
		cacheQueueAndMaxId = new CacheQueueAndMaxId();
		if (invokeResult != null) {
			//noinspection unchecked
			cacheQueueAndMaxId.addAndSetMaxId((Collection<FastCacheBaseCacheObject>) invokeResult, proxyClass);
		}

		Map<String, FastCacheBaseCacheObject> sKeyCacheMap = classMoreCacheMap
				                                                     .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
				                                                     .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
		for (FastCacheBaseCacheObject object : cacheQueueAndMaxId.getQueue()) {
			sKeyCacheMap.put(proxyClass.getSecondaryKeyValue(object), object);
		}
		pKeyListCacheMap.put(pKey, cacheQueueAndMaxId);
		return cacheQueueAndMaxId;
	}

	/**
	 * 取
	 */
	protected Object getObject(Object o,
	                           Method method,
	                           Object[] objects,
	                           MethodProxy methodProxy,
	                           ProxyClass proxyClass,
	                           String hashCode,
	                           CacheOperation cacheOperation) throws Throwable {

		String pKey = "" + objects[cacheOperation.primaryKeyIndex()];
		CacheQueueAndMaxId cacheQueueAndMaxId;
		if (proxyClass.isListClass()) {
			Map<String, CacheQueueAndMaxId> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());

			cacheQueueAndMaxId = pKeyListCacheMap.get(pKey);

			if (cacheQueueAndMaxId == null) {
				boolean addQueue = true;
				Object invokeResult;
				if (proxyClass.initListMethod.getName().equals(method.getName())) {
					if (proxyClass.getInitListMethodProxy() == null) {
						LOG.debug("【{}-{}】开始缓存LIST，并缓存initListMethod方法", proxyClass.beProxyClass.getSimpleName(), pKey);
						proxyClass.setInitListMethodProxy(methodProxy);
					} else {
						LOG.debug("【{}-{}】开始缓存LIST", proxyClass.beProxyClass.getSimpleName(), pKey);
					}
					invokeResult = methodProxy.invokeSuper(o, objects);
				} else {
					if (proxyClass.getInitListMethodProxy() != null) {
						LOG.debug("【{}-{}】开始缓存LIST，执行已缓存的initListMethod方法", proxyClass.beProxyClass.getSimpleName(), pKey);
						invokeResult = proxyClass.getInitListMethodProxy().invokeSuper(o, objects);
					} else {
						LOG.debug("【{}-{}】取one先缓存LIST", proxyClass.beProxyClass.getSimpleName(), pKey);
						invokeResult = proxyClass.initListMethod.invoke(o, objects[0]);
						addQueue = false;
					}
				}

				if (addQueue) {
					cacheQueueAndMaxId = getCacheQueueAndMaxId(pKey, hashCode, proxyClass, pKeyListCacheMap, invokeResult);
				}
			}

			if (cacheQueueAndMaxId != null && cacheOperation.isListOperation()) {
				//获得list
				return cacheQueueAndMaxId.getQueue();
			}
		}

		//获得单个
		Map<String, FastCacheBaseCacheObject> oneCacheMap;
		String key;

		if (proxyClass.isListClass()) {
			oneCacheMap = classMoreCacheMap
					              .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					              .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
			key = "" + objects[cacheOperation.secondaryKeyIndex()];
		} else {
			oneCacheMap = classOneCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			key = pKey;
		}

		FastCacheBaseCacheObject fastCacheBaseCacheObject = oneCacheMap.get(key);
		if (!proxyClass.isListClass()) {
			if (fastCacheBaseCacheObject == null) {
				LOG.debug("【{}-{}】未命中缓存", proxyClass.beProxyClass.getSimpleName(), key);
				fastCacheBaseCacheObject = (FastCacheBaseCacheObject) methodProxy.invokeSuper(o, objects);
				oneCacheMap.put(key, fastCacheBaseCacheObject == null ? NULL_CACHE_OBJECT : fastCacheBaseCacheObject);
			} else if (fastCacheBaseCacheObject == NULL_CACHE_OBJECT) {
				return null;
			}
		}
		return fastCacheBaseCacheObject;
	}

	/**
	 * 更新
	 */
	protected void update(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass,
	                      boolean isPutNotRemove,
	                      boolean hasError) throws IllegalAccessException {

		Map<String, FastCacheBaseCacheObject> oneCacheMap;
		String key;
		if (proxyClass.isListClass()) {
			oneCacheMap = classMoreCacheMap
					              .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					              .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
			key = proxyClass.getSecondaryKeyValue(fastCacheBaseCacheObject);
		} else {
			oneCacheMap = classOneCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			key = pKey;
		}

		if (hasError) {
			if (proxyClass.isListClass()) {
				oneCacheMap.remove(key);
				deleteInList(fastCacheBaseCacheObject, hashCode, pKey);
			} else {
				oneCacheMap.remove(key);
			}
		} else {
			if (isPutNotRemove) {
				oneCacheMap.put(key, fastCacheBaseCacheObject);
			} else {
				oneCacheMap.remove(key);
			}
		}
	}

	/**
	 * 增加
	 */
	protected void add(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                   String hashCode,
	                   String pKey,
	                   ProxyClass proxyClass) throws IllegalAccessException {

		update(fastCacheBaseCacheObject, hashCode, pKey, proxyClass, true, false);

		if (proxyClass.isListClass()) {
			classListCacheMap
					.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(pKey, k -> new CacheQueueAndMaxId())
					.addAndSetMaxId(fastCacheBaseCacheObject, proxyClass);
		}
	}

	/**
	 * 删除
	 */
	protected void delete(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass) throws IllegalAccessException {

		update(fastCacheBaseCacheObject, hashCode, pKey, proxyClass, false, false);

		if (proxyClass.isListClass()) {
			deleteInList(fastCacheBaseCacheObject, hashCode, pKey);
		}
	}

	private void deleteInList(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                          String hashCode,
	                          String pKey) {
		CacheQueueAndMaxId cacheQueueAndMaxId = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>()).get(pKey);
		if (cacheQueueAndMaxId != null) {
			cacheQueueAndMaxId.getQueue().remove(fastCacheBaseCacheObject);
		}
	}

}