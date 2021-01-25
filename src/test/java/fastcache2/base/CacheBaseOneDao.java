package fastcache2.base;

import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.base.FastCacheBaseOneDao;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/22，13:52:09
 */
@Cache(location = "fastcache2.domain", primaryKey = "userId")
public abstract class CacheBaseOneDao<T, P> extends FastCacheBaseOneDao<T, P> {
}