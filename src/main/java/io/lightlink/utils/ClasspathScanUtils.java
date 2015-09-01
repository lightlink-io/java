package io.lightlink.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanUtils {


    public static ArrayList<String> getAllResources(String packageName, ServletContext servletContext) throws IOException {
        ArrayList<String> res = new ArrayList<>();
        res.addAll(getResourcesFromPackage(packageName));
        res.addAll(getResourcesFromWebInf(packageName, servletContext));
        return res;
    }

    private static List<String> getResourcesFromWebInf(String packageName, ServletContext servletContext) {


        List<String> res = new ArrayList<>();

        try {
            URL url = servletContext.getResource('/' + packageName.replace('.', '/'));
            findFromDirectory(url, res);
        } catch (MalformedURLException e) { /* skip and keep searching */
        } catch (UnsupportedEncodingException e) { /* */ }

        try {
            URL url = servletContext.getResource("/WEB-INF/" + packageName.replace('.', '/'));
            findFromDirectory(url, res);

        } catch (MalformedURLException e) { /* skip and keep searching */
        } catch (UnsupportedEncodingException e) { /* */ }

        return res;
    }

    public static List<String> getResourcesFromPackage(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> packageURLs;
        ArrayList<String> names = new ArrayList<String>();

        packageName = packageName.replace(".", "/");
        packageURLs = classLoader.getResources(packageName);

        while (packageURLs.hasMoreElements()) {
            URL packageURL = packageURLs.nextElement();
            // loop through files in classpath


            if (packageURL.getProtocol().equals("jar")) {
                String jarFileName;
                JarFile jf;
                Enumeration<JarEntry> jarEntries;
                String entryName;

                // build jar file name, then loop through zipped entries
                jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
                jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
                jf = new JarFile(jarFileName);
                jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    entryName = jarEntries.nextElement().getName();
                    if (entryName.startsWith(packageName) && entryName.length() > packageName.length() + 5) {
                        entryName = entryName.substring(packageName.length());
                        names.add(entryName);
                    }
                }

            } else {

                findFromDirectory(packageURL, names);
            }
        }

        return names;
    }

    private static void findFromDirectory(URL packageURL, List<String> names) throws UnsupportedEncodingException {
        if (packageURL==null)
            return;
        File folder = new File(URLDecoder.decode(packageURL.getPath(), "UTF-8"));

        Collection<File> files = FileUtils.listFiles(folder, null, true);
        int length = folder.getAbsolutePath().length();

        String entryName;
        if (files != null)
            for (File actual : files) {
                entryName = actual.getAbsolutePath().substring(length);
                names.add(entryName);
            }
    }

    public static File getFileFromResource(String rootPackage, String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String resourceName = rootPackage.replace('.', '/') + resource;
        URL url = classLoader.getResource(resourceName);
        if (url == null)
            throw new IllegalArgumentException("Cannot find :" + resourceName + "   " + resource);
        return new File(URLDecoder.decode(url.getPath()));
    }

//    public static String getContentFromResource(String rootPackage, String resource) throws IOException {
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        return IOUtils.toString(classLoader.getResource(rootPackage.replace('.', '/') + '/' + resource));
//    }
//
//    public static ArrayList<String> getResourcesFromPackage(String packageName, String... regexps) throws IOException {
//
//        ArrayList<String> resources = getResourcesFromPackage(packageName);
//        for (Iterator<String> iterator = resources.iterator(); iterator.hasNext(); ) {
//            String res = iterator.next();
//            for (String expr : regexps) {
//                if (!res.matches(expr)) {
//                    iterator.remove();
//                    break;
//                }
//            }
//        }
//        return resources;
//    }
}
