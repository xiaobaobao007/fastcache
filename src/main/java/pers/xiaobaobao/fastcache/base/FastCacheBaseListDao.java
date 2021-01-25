package pers.xiaobaobao.fastcache.base;

import java.util.List;

import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;

/**
 * dao层类统一定义父类，来避免写过多的繁琐注解
 * <p>
 * 一对多关系的父类
 * <p>
 * T 对应的po类
 * P 主键类型
 * S 副键类型
 * <p>
 * 当然你可以把数据库的操作也在此处整合，但是不建议你这么做！！！，>>>（高内聚，低耦合）<<<
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.0
 * @date 2021/1/22，13:52:09
 */
public abstract class FastCacheBaseListDao<T, P, S> implements FastCacheBaseCacheObject {

	/**
	 * 获取全部list缓存
	 */
	@CacheInitList
	@CacheOperation(isListOperation = true, operation = CacheOperationType.GET)
	public List<T> getList(P p) {
		return getListByPKeys(p);
	}

	/**
	 * 自定义实现list数据库读取
	 */
	protected abstract List<T> getListByPKeys(P p);

	@CacheOperation(operation = CacheOperationType.GET)
	public T getOne(P p, S s) {
		return getOneByPSKeys(p, s);
	}

	/**
	 * 自定义实现数据库读取
	 */
	protected abstract T getOneByPSKeys(P p, S s);

	@CacheOperation(operation = CacheOperationType.UPDATE)
	public void update(T t) {
		updateOne(t);
	}

	/**
	 * 自定义实现数据库更新
	 */
	protected abstract void updateOne(T t);

	@CacheOperation(operation = CacheOperationType.ADD)
	public void save(T t) {
		saveOne(t);
	}

	/**
	 * 自定义实现数据库保存
	 */
	protected abstract void saveOne(T t);

	@CacheOperation(operation = CacheOperationType.DELETE)
	public void delete(T t) {
		deleteOne(t);
	}

	/**
	 * 自定义实现数据库删除
	 */
	protected abstract void deleteOne(T t);
}
