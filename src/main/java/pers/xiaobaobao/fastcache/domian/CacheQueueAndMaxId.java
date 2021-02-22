package pers.xiaobaobao.fastcache.domian;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;

/**
 * 用来记录当前最大的id
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.3
 * @date 2021/2/22，11:00
 */
public class CacheQueueAndMaxId {

	private final Queue<FastCacheBaseCacheObject> queue = new LinkedList<>();
	private long maxId = 0;

	public void addAndSetMaxId(Collection<FastCacheBaseCacheObject> collection, ProxyClass proxyClass) {
		if (collection == null || collection.isEmpty()) {
			return;
		}
		for (FastCacheBaseCacheObject object : collection) {
			addAndSetMaxId(object, proxyClass);
		}
	}

	public void addAndSetMaxId(FastCacheBaseCacheObject object, ProxyClass proxyClass) {
		if (object == null) {
			return;
		}
		try {
			long id = (long) proxyClass.idField.get(object);
			if (id > maxId) {
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

	public long getMaxId() {
		return maxId;
	}

	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}
}
