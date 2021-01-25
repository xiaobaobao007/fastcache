package pers.xiaobaobao.fastcache.base;

import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;

/**
 * dao层类统一定义父类，来避免写过多的繁琐注解
 * <p>
 * 一对一关系的父类
 * <p>
 * T 对应的po类
 * P 主键类型
 * <p>
 * * 当然你可以把数据库的操作也在此处整合，但是不建议你这么做！！！，>>>（高内聚，低耦合）<<<
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.0
 * @date 2021/1/22，13:52:09
 */
public abstract class FastCacheBaseOneDao<T, P> implements FastCacheBaseCacheObject {

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
