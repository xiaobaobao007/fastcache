package com.intion.fastcache.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.intion.fastcache.annotation.CacheOperation;
import com.intion.fastcache.domian.CacheObject;

import net.sf.cglib.proxy.MethodProxy;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/19，11:27
 */
public class CacheFactory {
	//缓存对象
	private static final Map<String, Map<String, Map<String, CacheObject>>> classCacheMap = new ConcurrentHashMap<>();//<class,<pKey,<sKey,cache>>>
	private static final Map<String, Map<String, List<CacheObject>>> classListCacheMap = new ConcurrentHashMap<>();//<class,<pKey,cacheList>>

	//空对象
	private static final CacheObject NULL_CACHE_OBJECT = new CacheObject() {
	};
	private static final List<CacheObject> NULL_CACHE_LIST_OBJECT = new ArrayList<>();

	protected Object getObject(Object o, Method method, Object[] objects, MethodProxy methodProxy, CglibProxyFactory.ProxyClass proxyClass, String hashCode, CacheOperation cacheOperation) throws Throwable {
		Map<String, List<CacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());

		String key = "" + objects[cacheOperation.primaryKeyIndex()];
		List<CacheObject> listCache = pKeyListCacheMap.get(key);

		Map<String, Map<String, CacheObject>> pKeyCacheMap = null;

		if (listCache == null) {
			boolean skip = false;
			if (proxyClass.initMethod.getName().equals(method.getName())) {
				proxyClass.methodProxy = methodProxy;
				System.out.println("未命中缓存-initMethod");
				//noinspection unchecked
				listCache = (List<CacheObject>) methodProxy.invokeSuper(o, objects);
			} else {
				if (proxyClass.methodProxy != null) {
					System.out.println("未命中缓存-methodProxy");
					//noinspection unchecked
					listCache = (List<CacheObject>) proxyClass.methodProxy.invokeSuper(o, objects);
				} else {
					System.out.println("未命中缓存-invoke");
					//noinspection unchecked
					listCache = (List<CacheObject>) proxyClass.initMethod.invoke(o, objects[0]);
					skip = true;
				}
			}

			if (!skip) {
				if (listCache == null) {
					listCache = NULL_CACHE_LIST_OBJECT;
				} else {
					if (pKeyCacheMap == null) {
						pKeyCacheMap = classCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
					}
					Map<String, CacheObject> sKeyCacheMap = pKeyCacheMap.computeIfAbsent("" + objects[cacheOperation.primaryKeyIndex()], k -> new ConcurrentHashMap<>());
					for (CacheObject object : listCache) {
						sKeyCacheMap.put(getParamsKey(proxyClass.getPrimaryKeyValue(object), proxyClass.getSecondaryKeyValue(object)), object);
					}
				}
				pKeyListCacheMap.put(key, listCache);
			}
		} else if (listCache == NULL_CACHE_LIST_OBJECT) {
			return null;
		}

		if (cacheOperation.isListOperation()) {
			//获得list
			return listCache;
		} else {
			//获得单个
			if (pKeyCacheMap == null) {
				pKeyCacheMap = classCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			}
			Map<String, CacheObject> sKeyCacheMap = pKeyCacheMap.computeIfAbsent("" + objects[cacheOperation.primaryKeyIndex()], k -> new ConcurrentHashMap<>());
			String sKey = proxyClass.isListClass() ? "" + objects[cacheOperation.secondaryKeyIndex()] : "";
			CacheObject cacheObject = sKeyCacheMap.get(sKey);
			if (!proxyClass.isListClass()) {
				if (cacheObject == null) {
					System.out.println("未命中单个缓存");
					cacheObject = (CacheObject) methodProxy.invokeSuper(o, objects);
					sKeyCacheMap.put(sKey, cacheObject == null ? NULL_CACHE_OBJECT : cacheObject);
				} else if (cacheObject == NULL_CACHE_OBJECT) {
					return null;
				}
			}
			return cacheObject;
		}
	}

	protected void updateOrAdd(CacheObject cacheObject, String hashCode, String pkey, boolean isListClass, Field[] fields) throws IllegalAccessException {
		Map<String, CacheObject> sKeyCacheMap = classCacheMap
														.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
														.computeIfAbsent(pkey, k -> new ConcurrentHashMap<>());

		String sKey = isListClass ? "" + fields[1].get(cacheObject) : "";
		sKeyCacheMap.put(sKey, cacheObject);

		if (isListClass) {
			Map<String, List<CacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			List<CacheObject> cacheObjectList = pKeyListCacheMap.get(pkey);
			if (cacheObjectList == null || cacheObjectList == NULL_CACHE_LIST_OBJECT) {
				cacheObjectList = new ArrayList<>();
				cacheObjectList.add(cacheObject);
			} else {
				int i;
				for (i = cacheObjectList.size() - 1; i >= 0; i--) {
					if (cacheObjectList.get(i) == cacheObject) {
						cacheObjectList.set(i, cacheObject);
						break;
					}
				}
				if (i < 0) {
					cacheObjectList.add(cacheObject);
				}
			}
		}
	}

	protected void delete(CacheObject cacheObject, String hashCode, String pkey, boolean isListClass, Field[] fields) throws IllegalAccessException {
		Map<String, CacheObject> sKeyCacheMap = classCacheMap
														.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>())
														.computeIfAbsent(pkey, k -> new ConcurrentHashMap<>());

		String sKey = isListClass ? "" + fields[1].get(cacheObject) : "";
		sKeyCacheMap.remove(sKey);

		if (isListClass) {
			Map<String, List<CacheObject>> pKeyListCacheMap = classListCacheMap.computeIfAbsent(hashCode, k -> new ConcurrentHashMap<>());
			List<CacheObject> cacheObjectList = pKeyListCacheMap.get(pkey);
			if (cacheObjectList != null || cacheObjectList != NULL_CACHE_LIST_OBJECT) {
				for (int i = cacheObjectList.size() - 1; i >= 0; i--) {
					if (cacheObjectList.get(i) == cacheObject) {
						cacheObjectList.remove(i);
						break;
					}
				}
			}
		}
	}

	private static String getParamsKey(Object key1, Object key2) {
		return key1 + "_" + key2;
	}
}
