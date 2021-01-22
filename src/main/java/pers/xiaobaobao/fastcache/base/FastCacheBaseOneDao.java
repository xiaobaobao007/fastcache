package pers.xiaobaobao.fastcache.base;

import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/22，13:52:09
 */
public abstract class FastCacheBaseOneDao<T,P> implements FastCacheBaseCacheObject {

	@CacheOperation(operation = CacheOperationType.GET)
	public T get(P p) {
		return getOneByPKey(p);
	}

	protected abstract T getOneByPKey(P uid);

	@CacheOperation(operation = CacheOperationType.UPDATE)
	public void update(T t) {
		updateOne(t);
	}

	protected abstract void updateOne(T t);

	@CacheOperation(operation = CacheOperationType.ADD)
	public void save(T t) {
		saveOne(t);
	}

	protected abstract void saveOne(T t);

	@CacheOperation(operation = CacheOperationType.DELETE)
	public void delete(T t) {
		deleteOne(t);
	}

	protected abstract void deleteOne(T t);
}
