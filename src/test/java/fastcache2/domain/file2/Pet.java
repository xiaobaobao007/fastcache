package fastcache2.domain.file2;

import pers.xiaobaobao.fastcache.annotation.Id;
import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/20，15:53:57
 */
public class Pet implements FastCacheBaseCacheObject {

	private int uid;
	@Id
	private long id;
	private int num;

	public Pet(int uid, int id) {
		this.uid = uid;
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return "{" + "uid=" + uid + ", id=" + id + ", param=" + num + '}';
	}
}
