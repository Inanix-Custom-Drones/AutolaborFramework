package cn.autolabor.util.scan;

import cn.autolabor.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClasspathPackageScanner {

    private String basePackage;
    private ClassLoader cl;

    public ClasspathPackageScanner(String basePackage) {
        this.basePackage = basePackage;
        this.cl = getClass().getClassLoader();
    }

    public ClasspathPackageScanner(String basePackage, ClassLoader classLoader) {
        this.basePackage = basePackage;
        this.cl = classLoader;
    }

    public static void main(String[] args) {
        ClasspathPackageScanner scanner = new ClasspathPackageScanner("org.nutz.aop");
        Set<String> names = scanner.getScanResult();
        for (String name : names) {
            System.out.println(name);
        }
    }

    /**
     * 获取指定包下的所有字节码文件的全类名
     */
    public Set<String> getScanResult() {
        Set<String> result = null;
        try {
            result = doScan(basePackage, new HashSet<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Set<String> doScan(String basePackage, Set<String> nameSet) throws IOException {
        String splashPath = Strings.dotToSplash(basePackage);
        Enumeration<URL> urls = cl.getResources(splashPath);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null) {
                String fileUrl = url.getFile();
                if (fileUrl.startsWith("file:")) { // JAR
                    String jarPath = fileUrl.substring(5, fileUrl.lastIndexOf('!'));
                    addFromJar(jarPath, splashPath, nameSet);
                } else { // class
                    addFromDirectory(fileUrl, splashPath, nameSet);
                }
            }
        }
        return nameSet;
    }

    private void addFromJar(String jarPath, String splashedPackageName, Set<String> nameSet) throws IOException {
        JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
        JarEntry entry = jarIn.getNextJarEntry();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                name = Strings.splashToDot(name);
                nameSet.add(name.substring(0, name.lastIndexOf(".")));
            }
            entry = jarIn.getNextJarEntry();
        }
    }

    private void addFromDirectory(String dirPath, String splashedPackageName, Set<String> nameSet) {
        File file = new File(dirPath);
        String[] names = file.list();
        if (null != names) {
            for (String name : names) {
                if (isClassFile(name)) {
                    nameSet.add(Strings.splashToDot(Strings.trimExtension(String.format("%s.%s", splashedPackageName, name))));
                } else {
                    addFromDirectory(String.format("%s/%s", dirPath, name), String.format("%s/%s", splashedPackageName, name), nameSet);
                }
            }
        }
    }

    private List<String> readFromDirectory(String path) {
        File file = new File(path);
        String[] names = file.list();
        if (null == names) {
            return null;
        }

        return Arrays.asList(names);
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }

    private boolean isJarFile(String name) {
        return name.endsWith(".jar");
    }

}
