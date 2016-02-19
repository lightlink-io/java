package io.lightlink.utils;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import org.apache.commons.io.FileUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanUtils {


    public static ArrayList<String> getAllResources(String packageName, ServletContext servletContext) throws IOException {
        ArrayList<String> res = new ArrayList<String>();
        res.addAll(getResourcesFromPackage(packageName));
        res.addAll(getResourcesFromWebInf(packageName, servletContext));
        return res;
    }

    private static List<String> getResourcesFromWebInf(String packageName, ServletContext servletContext) {

        if (servletContext == null)
            return Collections.EMPTY_LIST;

        List<String> res = new ArrayList<String>();

        findFromServletContext(servletContext, "/WEB-INF/" + packageName.replace('.', '/') + "/", "", res);

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

    private static void findFromServletContext(ServletContext servletContext, String initialPath, String currentPath, List<String> res) {
        String totalPath = initialPath + currentPath;
        if (totalPath.endsWith("/")) {
            Set<String> paths = servletContext.getResourcePaths(totalPath);
            if (paths != null)
                for (String p : paths) {
                    findFromServletContext(servletContext, initialPath, p.substring(initialPath.length()), res);
                }
        } else
            res.add(currentPath);
    }


    private static void findFromDirectory(URL packageURL, List<String> names) throws UnsupportedEncodingException {
        if (packageURL == null)
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
