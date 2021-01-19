package com.intion.fastcache.dao;

import java.util.ArrayList;
import java.util.List;

import com.intion.fastcache.annotation.Cache;
import com.intion.fastcache.annotation.CacheInitList;
import com.intion.fastcache.annotation.CacheOperation;
import com.intion.fastcache.domian.CacheObject;
import com.intion.fastcache.domian.CacheOperationType;
import com.intion.fastcache.factory.CglibProxyFactory;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
@Cache(isList = true, primaryKey = "uid", secondaryKey = "id")
public class ListDemoDao implements CacheObject {

	public static ListDemoDao dao = CglibProxyFactory.getProxy(ListDemoDao.class);

	private int uid;
	private int id;

	public ListDemoDao() {
	}

	public ListDemoDao(int uid, int id) {
		this.uid = uid;
		this.id = id;
	}

	@CacheInitList
	@CacheOperation(isListOperation = true, operation = CacheOperationType.GET)
	public List<ListDemoDao> getList(int uid) {
		List<ListDemoDao> list = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			list.add(new ListDemoDao(uid, i));
		}
		return list;
	}

	@CacheOperation(operation = CacheOperationType.GET)
	public ListDemoDao getOne(int uid, int id) {
		return new ListDemoDao(uid, id);
	}

	@CacheOperation(operation = CacheOperationType.UPDATE_ADD)
	public ListDemoDao update(int uid, int id) {
		return new ListDemoDao(uid, id);
	}

	@CacheOperation(operation = CacheOperationType.UPDATE_ADD)
	public ListDemoDao delete(int uid, int id) {
		return new ListDemoDao(uid, id);
	}

	@Override
	public String toString() {
		return "{" +
					   "uid=" + uid +
					   ", id=" + id +
					   '}';
	}

}