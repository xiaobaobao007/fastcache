package pers.xiaobaobao.fastcache.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;

/**
 * 通过包名获取class
 *
 * @author bao meng yang <932824098@qq.com>
 * @date 2021/1/20，14:33
 */
public class ClassTools {

	/**
	 * @param packageName 需要被加载的包名
	 * @param annotation  只有指定被注解的才能被加载
	 */
	public static void loadClass(String packageName, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
		Enumeration<URL> dirs;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		dirs = classLoader.getResources(packageName.replace('.', '/'));
		while (dirs.hasMoreElements()) {
			URL url = dirs.nextElement();
			String protocol = url.getProtocol();
			if ("file".equals(protocol)) {
				File dir = new File(url.getFile());
				if (dir.exists() && dir.isDirectory()) {
					File[] defiles = dir.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".class"));
					if (defiles != null) {
						for (File file : defiles) {
							String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
							if (classLoader.loadClass(className).getAnnotation(annotation) != null) {
								Class.forName(className, true, classLoader);
							}
						}
					}
				}
			}
		}
	}

}