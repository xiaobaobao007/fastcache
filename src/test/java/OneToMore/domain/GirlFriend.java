package OneToMore.domain;

import pers.xiaobaobao.fastcache.base.FastCacheBaseCacheObject;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/20，15:50:50
 */
public class GirlFriend implements FastCacheBaseCacheObject {

	private int userId;
	private int age;

	public GirlFriend(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "{" + "idCard=" + userId + ", age=" + age + '}';
	}

}