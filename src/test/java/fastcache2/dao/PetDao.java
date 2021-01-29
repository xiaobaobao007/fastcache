package fastcache2.dao;

import java.util.ArrayList;
import java.util.List;

import fastcache2.base.CacheBaseListDao;
import fastcache2.domain.file2.Pet;
import pers.xiaobaobao.fastcache.factory.CglibProxyFactory;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/16，17:41
 */
public class PetDao extends CacheBaseListDao<Pet, Integer, Integer> {

	public static PetDao dao = CglibProxyFactory.getProxy(PetDao.class);

	public PetDao() {
	}

	@Override
	protected List<Pet> getListByPKeys(Integer uid) {
		//todo 用于模拟从数据库取得数据
		if (uid % 2 == 0) {
			List<Pet> list = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				list.add(new Pet(uid, i));
			}
			return list;
		}
		return null;
	}

	@Override
	protected Pet getOneByPSKeys(Integer uid, Integer id) {
		return new Pet(uid, id);
	}

	@Override
	protected void updateOne(Pet pet) {
		throw new NullPointerException();
	}

	@Override
	protected void saveOne(Pet pet) {

	}

	@Override
	protected void deleteOne(Pet pet) {

	}

}