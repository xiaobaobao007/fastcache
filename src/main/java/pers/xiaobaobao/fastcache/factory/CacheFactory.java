package pers.xiaobaobao.fastcache.factory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheObject;
import pers.xiaobaobao.fastcache.domian.ProxyClass;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/19，11:27
 */
public class CacheFactory {

	private static final Logger LOG = LoggerFactory.getLogger(CacheFactory.class);

	//一对一缓存对象
	private static final Map<String, Map<String, CacheObject>> classOneCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cache>>
	//一对多缓存对象
	private static final Map<String, Map<String, Map<String, CacheObject>>> classMoreCacheMap = new ConcurrentHashMap<>();//<class,<pKey,<sKey,cache>>>
	private static final Map<String, Map<String, Queue<CacheObject>>> classListCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cacheList>>

	//空对象
	private static final CacheObject NULL_CACHE_OBJECT = new CacheObject() {
	};

	protected Object getObject(Object o,
	                           Method method,
	                           Object[] objects,
	                           MethodProxy methodProxy,
	                           ProxyClass proxyClass,
	                           String hashCode,
	                           CacheOperation cacheOperation) throws Throwable {

		String pKey = "" + objects[cacheOperation.primaryKeyIndex()];
		if (proxyClass.isListClass()) {
			Map<String, Queue<CacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());

			Queue<CacheObject> listCache = pKeyListCacheMap.get(pKey);

			if (listCache == null) {
				boolean skip = false;
				if (proxyClass.initMethod.getName().equals(method.getName())) {
					LOG.debug("【{}-{}】未命中LIST缓存,类未缓存initMethod", proxyClass.beProxyClass.getSimpleName(), pKey);
					proxyClass.setMethodProxy(methodProxy);
					Object invokeResult = methodProxy.invokeSuper(o, objects);
					if (invokeResult == null) {
						listCache = new LinkedList<>();
					} else {
						//noinspection unchecked
						listCache = new LinkedList<>((Collection<CacheObject>) invokeResult);
					}
				} else {
					if (proxyClass.getMethodProxy() != null) {
						LOG.debug("【{}-{}】未命中LIST缓存,执行类initMethod", proxyClass.beProxyClass.getSimpleName(), pKey);
						Object invokeResult = proxyClass.getMethodProxy().invokeSuper(o, objects);
						if (invokeResult == null) {
							listCache = new LinkedList<>();
						} else {
							//noinspection unchecked
							listCache = new LinkedList<>((Collection<CacheObject>) invokeResult);
						}
					} else {
						LOG.debug("【{}-{}】未命中类INIT LIST缓存", proxyClass.beProxyClass.getSimpleName(), pKey);
						//noinspection unchecked
						listCache = (Queue<CacheObject>) proxyClass.initMethod.invoke(o, objects[0]);
						skip = true;
					}
				}

				if (!skip) {
					Map<String, CacheObject> sKeyCacheMap = classMoreCacheMap
							                                        .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
							                                        .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
					for (CacheObject object : listCache) {
						sKeyCacheMap.put(getParamsKey(proxyClass.getPrimaryKeyValue(object), proxyClass.getSecondaryKeyValue(object)), object);
					}
					pKeyListCacheMap.put(pKey, listCache);
				}
			}

			if (cacheOperation.isListOperation()) {
				//获得list
				return listCache;
			}
		}

		//获得单个
		Map<String, CacheObject> oneCacheMap;
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

		CacheObject cacheObject = oneCacheMap.get(key);
		if (!proxyClass.isListClass()) {
			if (cacheObject == null) {
				LOG.debug("【{}-{}】未命中缓存", proxyClass.beProxyClass.getSimpleName(), key);
				cacheObject = (CacheObject) methodProxy.invokeSuper(o, objects);
				oneCacheMap.put(key, cacheObject == null ? NULL_CACHE_OBJECT : cacheObject);
			} else if (cacheObject == NULL_CACHE_OBJECT) {
				return null;
			}
		}
		return cacheObject;
	}

	protected void update(CacheObject cacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass,
	                      boolean isPutNotRemove) throws IllegalAccessException {

		Map<String, CacheObject> oneCacheMap;
		String key;
		if (proxyClass.isListClass()) {
			oneCacheMap = classMoreCacheMap
					              .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					              .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
			key = proxyClass.getSecondaryKeyValue(cacheObject);
		} else {
			oneCacheMap = classOneCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			key = pKey;
		}
		if (isPutNotRemove) {
			oneCacheMap.put(key, cacheObject);
		} else {
			oneCacheMap.remove(key);
		}
	}

	protected void add(CacheObject cacheObject,
	                   String hashCode,
	                   String pKey,
	                   ProxyClass proxyClass) throws IllegalAccessException {

		update(cacheObject, hashCode, pKey, proxyClass, true);

		if (proxyClass.isListClass()) {
			classListCacheMap
					.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(pKey, k -> new LinkedList<>())
					.add(cacheObject);
		}
	}

	protected void delete(CacheObject cacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass) throws IllegalAccessException {

		update(cacheObject, hashCode, pKey, proxyClass, false);

		if (proxyClass.isListClass()) {
			Map<String, Queue<CacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			Queue<CacheObject> cacheObjectQueue = pKeyListCacheMap.get(pKey);

			if (cacheObjectQueue != null) {
				cacheObjectQueue.remove(cacheObject);
			}
		}
	}

	private static String getParamsKey(Object key1, Object key2) {
		return key1 + "_" + key2;
	}
}