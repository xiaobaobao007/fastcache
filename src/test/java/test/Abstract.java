package test;

import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;

/**
 * @author xiaobaobao
 * @date 2021/1/20，21:24
 */

@Cache(location = "abstract", primaryKey = "")
public abstract class Abstract {

	@CacheInitList
	public final void bmy() {

	}
}
