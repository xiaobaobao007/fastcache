package pers.xiaobaobao.fastcache.factory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;
import pers.xiaobaobao.fastcache.domian.ProxyClass;

/**
 * 缓存操作工厂
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.1
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
	//发生过回滚的对象，当再次获取的时候，从数据库读取
	private static final FastCacheBaseCacheObject ERROR_CACHE_OBJECT = new FastCacheBaseCacheObject() {
	};

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
		Queue<FastCacheBaseCacheObject> listCache = null;
		if (proxyClass.isListClass()) {
			Map<String, Queue<FastCacheBaseCacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());

			listCache = pKeyListCacheMap.get(pKey);

			if (listCache == null) {
				boolean skip = false;
				if (proxyClass.initListMethod.getName().equals(method.getName())) {
					if (proxyClass.getInitListMethodProxy() == null) {
						LOG.debug("【{}-{}】开始缓存LIST，并缓存initListMethod方法", proxyClass.beProxyClass.getSimpleName(), pKey);
						proxyClass.setInitListMethodProxy(methodProxy);
					} else {
						LOG.debug("【{}-{}】开始缓存LIST", proxyClass.beProxyClass.getSimpleName(), pKey);
					}
					Object invokeResult = methodProxy.invokeSuper(o, objects);
					if (invokeResult == null) {
						listCache = new LinkedList<>();
					} else {
						//noinspection unchecked
						listCache = new LinkedList<>((Collection<FastCacheBaseCacheObject>) invokeResult);
					}
				} else {
					if (proxyClass.getInitListMethodProxy() != null) {
						LOG.debug("【{}-{}】开始缓存LIST，执行已缓存的initListMethod方法", proxyClass.beProxyClass.getSimpleName(), pKey);
						Object invokeResult = proxyClass.getInitListMethodProxy().invokeSuper(o, objects);
						if (invokeResult == null) {
							listCache = new LinkedList<>();
						} else {
							//noinspection unchecked
							listCache = new LinkedList<>((Collection<FastCacheBaseCacheObject>) invokeResult);
						}
					} else {
						LOG.debug("【{}-{}】取one先缓存LIST", proxyClass.beProxyClass.getSimpleName(), pKey);
						//noinspection unchecked
						listCache = (Queue<FastCacheBaseCacheObject>) proxyClass.initListMethod.invoke(o, objects[0]);
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
		if (proxyClass.isListClass()) {
			if (fastCacheBaseCacheObject == ERROR_CACHE_OBJECT) {

				fastCacheBaseCacheObject = (FastCacheBaseCacheObject) methodProxy.invokeSuper(o, objects);

				if (fastCacheBaseCacheObject == null) {
					oneCacheMap.remove(key);
				} else {
					oneCacheMap.put(key, fastCacheBaseCacheObject);
				}

				Objects.requireNonNull(listCache).add(fastCacheBaseCacheObject);
			}
		} else {
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
				oneCacheMap.put(key, ERROR_CACHE_OBJECT);
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
					.computeIfAbsent(pKey, k -> new LinkedList<>())
					.add(fastCacheBaseCacheObject);
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
		Map<String, Queue<FastCacheBaseCacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
		Queue<FastCacheBaseCacheObject> fastCacheBaseCacheObjectQueue = pKeyListCacheMap.get(pKey);

		if (fastCacheBaseCacheObjectQueue != null) {
			fastCacheBaseCacheObjectQueue.remove(fastCacheBaseCacheObject);
		}
	}

}