package pers.xiaobaobao.fastcache.exception;

import java.lang.reflect.Method;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/19，11:13
 */
public class CacheKeyException extends RuntimeException {
	public CacheKeyException(String s, Class<?> cla) {
		super("[" + cla.getSimpleName() + "]" + s);
	}

	public CacheKeyException(String s, Class<?> cla, Method method) {
		super("[" + cla.getSimpleName() + "-" + method.getName() + "]" + s);
	}
}
