package pers.xiaobaobao.fastcache.util;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/21，11:01:44
 */
public class StringTools {
	public static boolean isNull(String s) {
		return s == null || s.length() == 0 || s.equals(" ");
	}
}