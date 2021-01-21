package pers.xiaobaobao.fastcache.domian;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.MethodProxy;
import pers.xiaobaobao.fastcache.annotation.Cache;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;

/**
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/21，15:11:07
 */
public class ProxyClass {
	public final Class<?> beProxyClass;
	public final Method initMethod;
	private MethodProxy methodProxy;
	public final Field[] keyFields;
	public final Map<String, CacheOperation> operationMap;

	public ProxyClass(Class<?> beProxyClass, Method initMethod, Field[] keyFields, Map<String, CacheOperation> operationMap) {
		this.beProxyClass = beProxyClass;
		this.initMethod = initMethod;
		this.keyFields = keyFields;
		this.operationMap = operationMap;
	}

	public String getPrimaryKeyValue(Object object) throws IllegalAccessException {
		return "" + keyFields[0].get(object);
	}

	public String getSecondaryKeyValue(Object object) throws IllegalAccessException {
		return "" + keyFields[1].get(object);
	}

	/**
	 * 应该用{@link Cache#isList()}进行判断，但是用keyFields.length刚好也能实现相同效果，还更简便，减少了一个内部属性
	 */
	public boolean isListClass() {
		return keyFields.length == 2;
	}

	public MethodProxy getMethodProxy() {
		return methodProxy;
	}

	public void setMethodProxy(MethodProxy methodProxy) {
		this.methodProxy = methodProxy;
	}
}
