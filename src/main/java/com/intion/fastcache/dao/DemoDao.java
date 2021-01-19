package com.intion.fastcache.dao;

import com.intion.fastcache.annotation.Cache;
import com.intion.fastcache.annotation.CacheOperation;
import com.intion.fastcache.domian.CacheObject;
import com.intion.fastcache.domian.CacheOperationType;
import com.intion.fastcache.factory.CglibProxyFactory;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
@Cache(primaryKey = "uid", secondaryKey = "id")
public class DemoDao implements CacheObject {
	public static DemoDao dao = CglibProxyFactory.getProxy(DemoDao.class);

	private int uid;
	private int id;

	public DemoDao() {
	}

	public DemoDao(int uid, int id) {
		this.uid = uid;
		this.id = id;
	}

	@CacheOperation(operation = CacheOperationType.GET)
	public DemoDao get(int uid, int id) {
		return new DemoDao(uid, id);
	}

	@CacheOperation(operation = CacheOperationType.UPDATE_ADD)
	public void uptdate(DemoDao demoDao) {
	}

	@CacheOperation(operation = CacheOperationType.DELETE)
	public void delete(DemoDao demoDao) {
	}

}