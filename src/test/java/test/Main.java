package test;

import pers.xiaobaobao.fastcache.annotation.Cache;

/**
 * @author xiaobaobao
 * @date 2021/1/20，21:24
 */
public class Main {
	public static void main(String[] args) {
		System.out.println(Demo.class.getAnnotation(Cache.class));
	}
}