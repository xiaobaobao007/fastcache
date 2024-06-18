package pers.xiaobaobao.fastcache.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    public static Class<?> getDaoToPo(String location, Class<?> daoClass) throws IOException, ClassNotFoundException {
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
            Enumeration<URL> dirs = classLoader.getResources(location.replace(".", "/"));
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    return getDaoToPoByFile(new File(url.getFile()), daoToPoName, location, classLoader);
                } else if ("jar".equals(protocol)) {
                    return getDaoToPoByJar(url, classLoader, daoToPoName);
                }
            }
        }
        return null;
    }

    private static Class<?> getDaoToPoByFile(File dir, String daoToPoName, String location, ClassLoader classLoader) throws ClassNotFoundException {
        if (dir.exists()) {
            File[] defiles = dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".class"));
            if (defiles == null) {
                return null;
            }
            for (File file : defiles) {
                if (file.isDirectory()) {
                    Class<?> classz = getDaoToPoByFile(file, daoToPoName, location, classLoader);
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
                    return Class.forName(className, true, classLoader);
                }
            }
        }
        return null;
    }

    private static Class<?> getDaoToPoByJar(URL url, ClassLoader classLoader, String daoToPoName) throws ClassNotFoundException, IOException {
        String[] path = url.getPath().split("!/");
        JarFile jarFile = new JarFile(path[0].substring(5));

        String className = "/" + daoToPoName + ".class";

        Enumeration<JarEntry> ee = jarFile.entries();
        while (ee.hasMoreElements()) {
            JarEntry entry = ee.nextElement();
            if (entry.getName().startsWith(path[1]) && entry.getName().endsWith(className)) {
                className = entry.getName().replace("/", ".");
                className = className.substring(0, className.length() - 6);
                return Class.forName(className, true, classLoader);
            }
        }

        return null;
    }

    /**
     * 可以代替：Reflections.getTypesAnnotatedWith,目前只测试到dev和jar
     *
     * @param packageName 需要被加载的dao层包名，去扫描dao层bao下所有包含annotation注解的类
     * @param annotation  只有指定被注解的才能被加载
     * @return 加载类的数量
     */
    public static List<Class<?>> loadClassByAnnotation(String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> classList = new ArrayList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> dirs;
        try {
            String dirPath = packageName.replace(".", "/");
            dirs = classLoader.getResources(dirPath);
        } catch (IOException e) {
            return classList;
        }
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            if ("file".equals(url.getProtocol())) {
                List<Class<?>> list = loadClassByFile(classLoader, url, packageName, annotation);
                if (list.isEmpty()) {
                    continue;
                }
                classList.addAll(list);
            } else if ("jar".equals(url.getProtocol())) {
                List<Class<?>> list = loadClassByJarFile(classLoader, url, annotation);
                if (list == null || list.isEmpty()) {
                    continue;
                }
                classList.addAll(list);
            }
        }
        return classList;
    }

    private static List<Class<?>> loadClassByFile(ClassLoader classLoader, URL url, String packageName, Class<? extends Annotation> annotation) {
        String filePath = URLDecoder.decode(url.getFile());
        File dir = new File(filePath);
        List<Class<?>> classList = new ArrayList<>();
        loadClassByFile(classLoader, dir, packageName, annotation, classList);
        return classList;
    }

    private static void loadClassByFile(ClassLoader classLoader, File dir, String packageName, Class<? extends Annotation> annotation, List<Class<?>> classList) {
        if (!dir.exists()) {
            return;
        }

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (!packageName.endsWith("." + dir.getName())) {
                    packageName += "." + dir.getName();
                }
                loadClassByFile(classLoader, file, packageName, annotation, classList);
            }
        } else if (dir.getName().endsWith(".class")) {
            String className = packageName + '.' + dir.getName().substring(0, dir.getName().length() - 6);
            try {
                if (classLoader.loadClass(className).getAnnotation(annotation) == null) {
                    return;
                }
                classList.add(Class.forName(className, true, classLoader));
            } catch (ClassNotFoundException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private static List<Class<?>> loadClassByJarFile(ClassLoader classLoader, URL url, Class<? extends Annotation> annotation) {
        String[] path = url.getPath().split("!/");
        JarFile jarFile;
        try {
            jarFile = new JarFile(path[0].substring(5));
        } catch (IOException e) {
            return null;
        }

        LinkedList<JarEntry> jarEntryList = new LinkedList<>();
        Enumeration<JarEntry> ee = jarFile.entries();
        while (ee.hasMoreElements()) {
            JarEntry entry = ee.nextElement();
            // 过滤我们出满足我们需求的东西
            if (entry.getName().startsWith(path[1]) && entry.getName().endsWith(".class")) {
                jarEntryList.add(entry);
            }
        }

        if (jarEntryList.isEmpty()) {
            return null;
        }

        List<Class<?>> list = new ArrayList<>();
        for (JarEntry entry : jarEntryList) {
            String className = entry.getName().replace("/", ".");
            className = className.substring(0, className.length() - 6);
            try {
                if (classLoader.loadClass(className).getAnnotation(annotation) == null) {
                    continue;
                }
                list.add(Class.forName(className, true, classLoader));
            } catch (ClassNotFoundException ignored) {
            }
        }
        return list;
    }

}