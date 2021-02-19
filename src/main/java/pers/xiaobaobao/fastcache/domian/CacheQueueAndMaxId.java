package pers.xiaobaobao.fastcache.domian;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;

/**
 * 用来记录当前最大的id
 *
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/2/19，9:48:53
 */
public class CacheQueueAndMaxId {

	private final Queue<FastCacheBaseCacheObject> queue = new LinkedList<>();
	private Object maxId;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addAndSetMaxId(Collection<FastCacheBaseCacheObject> collection, ProxyClass proxyClass) {
		if (collection == null || collection.isEmpty()) {
			return;
		}
		for (FastCacheBaseCacheObject object : collection) {
			try {
				Comparable id = (Comparable) proxyClass.keyFields[1].get(object);
				if (maxId == null || id.compareTo(maxId) > 0) {
					maxId = id;
				}
				queue.add(object);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addAndSetMaxId(FastCacheBaseCacheObject object, ProxyClass proxyClass) {
		if (object == null) {
			return;
		}
		try {
			Comparable id = (Comparable) proxyClass.keyFields[1].get(object);
			if (id.compareTo(maxId) > 0) {
				maxId = id;
			}
			queue.add(object);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Queue<FastCacheBaseCacheObject> getQueue() {
		return queue;
	}

	public Object getMaxId() {
		return maxId;
	}

	public void setMaxId(Object maxId) {
		this.maxId = maxId;
	}
}
