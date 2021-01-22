package OneToMore.dao;

import java.util.ArrayList;
import java.util.List;

import OneToMore.base.CacheBaseListDao;
import OneToMore.domain.Pet;
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
		if (uid % 2 == 0) {
			return new Pet(uid, id);
		}
		return null;
	}

	@Override
	protected void updateOne(Pet pet) {

	}

	@Override
	protected void addOne(Pet pet) {

	}

	@Override
	protected void deleteOne(Pet pet) {

	}

}