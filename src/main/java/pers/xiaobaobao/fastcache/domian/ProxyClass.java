package pers.xiaobaobao.fastcache.domian;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.MethodProxy;
import pers.xiaobaobao.fastcache.annotation.CacheOperation;

/**
 * 一个简单的pojo类，用来缓存被代理的数据
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.3
 * @date 2021/2/22，11:00
 */
public class ProxyClass {

	//被代理的dao层对象
	public final Object proxyClass;
	//未被代理类的原始类
	public final Class<?> beProxyClass;
	//在一对多关系下，需要去执行initListMethod获得list（未被代理的方法）
	public final Method initListMethod;
	//在一对多关系下，需要去执行initListMethod获得list（被代理的方法）
	private MethodProxy initListMethodProxy;

	/**
	 * 对应po类下需要进行反射的属性值，
	 * 一对一关系下，keyFields的length=1，即只有主键的映射
	 * 一对多关系下，keyFields的length=2，0是主键的映射，1是副键的映射
	 */
	public final Field[] keyFields;
	public Field idField;
	//缓存dao层方法的注解
	public final Map<String, CacheOperation> operationMap;

	public ProxyClass(Object proxyClass, Class<?> beProxyClass, Method initListMethod, Field[] keyFields, Map<String, CacheOperation> operationMap, Field idField) {
		this.proxyClass = proxyClass;
		this.beProxyClass = beProxyClass;
		this.initListMethod = initListMethod;
		this.keyFields = keyFields;
		this.operationMap = operationMap;
		this.idField = idField;
	}

	/**
	 * @param object po类
	 * @return 返回po类下的主键属性值，因为要进行缓存，所以把主键映射为string类型
	 * @throws IllegalAccessException 除非传入的不是指定类，否则一般不会报错
	 */
	public String getPrimaryKeyValue(Object object) throws IllegalAccessException {
		return "" + keyFields[0].get(object);
	}

	/**
	 * @param object po类
	 * @return 返回po类下的副键属性值，因为要进行缓存，所以把主键映射为string类型
	 * @throws IllegalAccessException 除非传入的不是指定类，否则一般不会报错
	 */
	public String getSecondaryKeyValue(Object object) throws IllegalAccessException {
		return "" + keyFields[1].get(object);
	}

	/**
	 * 判断是不是一对多的关系
	 */
	public boolean isListClass() {
		return keyFields.length == 2;
	}

	public MethodProxy getInitListMethodProxy() {
		return initListMethodProxy;
	}

	public void setInitListMethodProxy(MethodProxy initListMethodProxy) {
		this.initListMethodProxy = initListMethodProxy;
	}

	public Object getInitList(Object key) throws InvocationTargetException, IllegalAccessException {
		return initListMethod.invoke(proxyClass, key);
	}

}
