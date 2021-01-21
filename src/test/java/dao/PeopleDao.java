package dao;

import domain.People;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.domian.CacheObject;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;
import pers.xiaobaobao.fastcache.factory.CglibProxyFactory;

/**
 * 一对一的关系
 *
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
@Cache(location = "", primaryKey = "userId")
public class PeopleDao implements CacheObject {

	public static PeopleDao dao = CglibProxyFactory.getProxy(PeopleDao.class);

	public PeopleDao() {
	}

	@CacheOperation(operation = CacheOperationType.GET)
	public People get(int uid) {
		if (uid % 2 == 0) {
			return new People(uid);
		}
		return null;
	}

	@CacheOperation(operation = CacheOperationType.ADD)
	public void add(People peopleDao) {
	}

	@CacheOperation(operation = CacheOperationType.UPDATE)
	public void update(People peopleDao) {
	}

	@CacheOperation(operation = CacheOperationType.DELETE)
	public void delete(People peopleDao) {
	}

}