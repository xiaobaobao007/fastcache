package pers.xiaobaobao.fastcache.base;

import java.util.List;

import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/22，13:52:09
 */
public abstract class FastCacheBaseListDao<T,P,S> implements FastCacheBaseCacheObject {

	@CacheInitList
	@CacheOperation(isListOperation = true, operation = CacheOperationType.GET)
	public List<T> getList(P p) {
		return getListByPKeys(p);
	}

	protected abstract List<T> getListByPKeys(P uid);

	@CacheOperation(operation = CacheOperationType.GET)
	public T getOne(P p, S s) {
		return getOneByPSKeys(p, s);
	}

	protected abstract T getOneByPSKeys(P p, S s);

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
