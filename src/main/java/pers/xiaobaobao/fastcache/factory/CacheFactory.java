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
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;
import pers.xiaobaobao.fastcache.domian.ProxyClass;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/19，11:27
 */
public class CacheFactory {

	private static final Logger LOG = LoggerFactory.getLogger(CacheFactory.class);

	//一对一缓存对象
	private static final Map<String, Map<String, FastCacheBaseCacheObject>> classOneCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cache>>
	//一对多缓存对象
	private static final Map<String, Map<String, Map<String, FastCacheBaseCacheObject>>> classMoreCacheMap = new ConcurrentHashMap<>();//<class,<pKey,<sKey,cache>>>
	private static final Map<String, Map<String, Queue<FastCacheBaseCacheObject>>> classListCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cacheList>>

	//空对象
	private static final FastCacheBaseCacheObject NULL_CACHE_OBJECT = new FastCacheBaseCacheObject() {
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
			Map<String, Queue<FastCacheBaseCacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());

			Queue<FastCacheBaseCacheObject> listCache = pKeyListCacheMap.get(pKey);

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
						listCache = new LinkedList<>((Collection<FastCacheBaseCacheObject>) invokeResult);
					}
				} else {
					if (proxyClass.getMethodProxy() != null) {
						LOG.debug("【{}-{}】未命中LIST缓存,执行类initMethod", proxyClass.beProxyClass.getSimpleName(), pKey);
						Object invokeResult = proxyClass.getMethodProxy().invokeSuper(o, objects);
						if (invokeResult == null) {
							listCache = new LinkedList<>();
						} else {
							//noinspection unchecked
							listCache = new LinkedList<>((Collection<FastCacheBaseCacheObject>) invokeResult);
						}
					} else {
						LOG.debug("【{}-{}】未命中类INIT LIST缓存", proxyClass.beProxyClass.getSimpleName(), pKey);
						//noinspection unchecked
						listCache = (Queue<FastCacheBaseCacheObject>) proxyClass.initMethod.invoke(o, objects[0]);
						skip = true;
					}
				}

				if (!skip) {
					Map<String, FastCacheBaseCacheObject> sKeyCacheMap = classMoreCacheMap
							                                                     .computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
							                                                     .computeIfAbsent(pKey, k -> new ConcurrentHashMap<>());
					for (FastCacheBaseCacheObject object : listCache) {
						sKeyCacheMap.put(proxyClass.getSecondaryKeyValue(object), object);
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

	protected void update(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass,
	                      boolean isPutNotRemove) throws IllegalAccessException {

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
		if (isPutNotRemove) {
			oneCacheMap.put(key, fastCacheBaseCacheObject);
		} else {
			oneCacheMap.remove(key);
		}
	}

	protected void add(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                   String hashCode,
	                   String pKey,
	                   ProxyClass proxyClass) throws IllegalAccessException {

		update(fastCacheBaseCacheObject, hashCode, pKey, proxyClass, true);

		if (proxyClass.isListClass()) {
			classListCacheMap
					.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(pKey, k -> new LinkedList<>())
					.add(fastCacheBaseCacheObject);
		}
	}

	protected void delete(FastCacheBaseCacheObject fastCacheBaseCacheObject,
	                      String hashCode,
	                      String pKey,
	                      ProxyClass proxyClass) throws IllegalAccessException {

		update(fastCacheBaseCacheObject, hashCode, pKey, proxyClass, false);

		if (proxyClass.isListClass()) {
			Map<String, Queue<FastCacheBaseCacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			Queue<FastCacheBaseCacheObject> fastCacheBaseCacheObjectQueue = pKeyListCacheMap.get(pKey);

			if (fastCacheBaseCacheObjectQueue != null) {
				fastCacheBaseCacheObjectQueue.remove(fastCacheBaseCacheObject);
			}
		}
	}

	private static String getParamsKey(Object key1, Object key2) {
		return key1 + "_" + key2;
	}
}