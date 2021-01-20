import java.lang.annotation.Inherited;

import com.sun.istack.internal.Interned;

import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheInitList;

/**
 * @author xiaobaobao
 * @date 2021/1/20，21:24
 */

@Cache(classz = aaa.class, primaryKey = "")
public abstract class aaa {

	@CacheInitList
	public final void bmy() {

	}
}
