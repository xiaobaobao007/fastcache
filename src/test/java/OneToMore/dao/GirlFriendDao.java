package OneToMore.dao;

import OneToMore.base.CacheBaseOneDao;
import OneToMore.domain.GirlFriend;
import pers.xiaobaobao.fastcache.factory.CglibProxyFactory;

/**
 * 一对一的关系
 *
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
public class GirlFriendDao extends CacheBaseOneDao<GirlFriend, Integer> {

	public static GirlFriendDao dao = CglibProxyFactory.getProxy(GirlFriendDao.class);

	public GirlFriendDao() {
	}

	@Override
	protected GirlFriend getOneByPKey(Integer uid) {
		if (uid % 2 == 0) {
			return new GirlFriend(uid);
		}
		return null;
	}

	@Override
	protected void updateOne(GirlFriend girlFriend) {

	}

	@Override
	protected void saveOne(GirlFriend girlFriend) {

	}

	@Override
	protected void deleteOne(GirlFriend girlFriend) {

	}
}