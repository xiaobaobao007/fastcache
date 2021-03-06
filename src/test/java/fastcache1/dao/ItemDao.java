package fastcache1.dao;

import java.util.ArrayList;
import java.util.List;

import fastcache1.domain.Item;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;
import pers.xiaobaobao.fastcache.domian.CacheOperationType;
import pers.xiaobaobao.fastcache.factory.CglibProxyFactory;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
@Cache(location = "fastcache1.domain.Item", primaryKey = "uid", secondaryKey = "id")
public class ItemDao implements FastCacheBaseCacheObject {

	public static ItemDao dao = CglibProxyFactory.getProxy(ItemDao.class);

	public ItemDao() {
	}

	@CacheInitList
	@CacheOperation(isListOperation = true, operation = CacheOperationType.GET)
	public List<Item> getList(int uid) {
		//todo 用于模拟从数据库取得数据
		if (uid % 2 == 0) {
			List<Item> list = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				list.add(new Item(uid, i));
			}
			return list;
		}
		return null;
	}

	@CacheOperation(operation = CacheOperationType.GET)
	public Item getOne(int uid, int id) {
		return null;
	}

	@CacheOperation(operation = CacheOperationType.UPDATE)
	public void update(Item item) {
	}

	@CacheOperation(operation = CacheOperationType.ADD)
	public void add(Item item) {
	}

	@CacheOperation(operation = CacheOperationType.DELETE)
	public void delete(Item item) {
	}

}