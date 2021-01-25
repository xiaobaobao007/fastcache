package pers.xiaobaobao.fastcache.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;

/**
 * class工具类
 *
 * @author bao meng yang <932824098@qq.com>
 * @version 2.0
 * @date 2021/1/20，14:33
 */
public class ClassTools {

	/**
	 * 寻找po类位置
	 *
	 * @param location po类地址，或者包的地址，寻找逻辑会在每个文件夹搜索
	 * @param daoClass dao层类class
	 * @return po类
	 * @throws IOException 寻找不到po类
	 */
	public static Class<?> getDaoToPo(String location, Class<?> daoClass) throws IOException {
		String daoToPoName = daoClass.getSimpleName();
		if (!StringTools.isNull(daoToPoName) && daoToPoName.length() > 3) {
			daoToPoName = daoToPoName.substring(0, daoToPoName.length() - 3);
		} else {
			return null;
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			return classLoader.loadClass(location);
		} catch (ClassNotFoundException e) {
			Enumeration<URL> dirs = classLoader.getResources(location.replace('.', '/'));
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					return getClassByDeepDir(new File(url.getFile()), daoToPoName, location, classLoader);
				}
			}
		}
		return null;
	}

	private static Class<?> getClassByDeepDir(File dir, String daoToPoName, String location, ClassLoader classLoader) {
		if (dir.exists()) {
			File[] defiles = dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".class"));
			if (defiles == null) {
				return null;
			}
			for (File file : defiles) {
				if (file.isDirectory()) {
					Class<?> classz = getClassByDeepDir(file, daoToPoName, location, classLoader);
					if (classz != null) {
						return classz;
					}
				} else {
					String poName = file.getName().substring(0, file.getName().length() - 6);
					if (!poName.equals(daoToPoName)) {
						continue;
					}
					String a = location.substring(location.lastIndexOf(".") + 1);
					String className = location + file.getPath().substring(file.getPath().lastIndexOf(a) + a.length(), file.getPath().length() - 6).replace("\\", ".");
					try {
						return Class.forName(className, true, classLoader);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param packageName 需要被加载的dao层包名，去扫描dao层bao下所有包含annotation注解的类
	 * @param annotation  只有指定被注解的才能被加载
	 * @return 加载类的数量
	 * @throws IOException            packageName找不到
	 * @throws ClassNotFoundException class类加载不到
	 */
	public static int loadClass(String packageName, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
		int num = 0;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> dirs = classLoader.getResources(packageName.replace('.', '/'));
		while (dirs.hasMoreElements()) {
			URL url = dirs.nextElement();
			if (!"file".equals(url.getProtocol())) {
				continue;
			}
			File dir = new File(url.getFile());
			if (!dir.exists() || !dir.isDirectory()) {
				continue;
			}
			File[] defiles = dir.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".class"));
			if (defiles == null) {
				continue;
			}
			for (File file : defiles) {
				String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
				if (classLoader.loadClass(className).getAnnotation(annotation) == null) {
					continue;
				}
				num++;
				Class.forName(className, true, classLoader);
			}
		}
		return num;
	}

}