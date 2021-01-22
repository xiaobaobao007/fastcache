package OneToMore.base;

import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.base.FastCacheBaseListDao;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/22，13:52:09
 */
@Cache(location = "OneToMore.domain", primaryKey = "uid", secondaryKey = "id")
public abstract class CacheBaseListDao<T, P, S> extends FastCacheBaseListDao<T, P, S> {
}