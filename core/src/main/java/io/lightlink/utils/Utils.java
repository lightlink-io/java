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


import io.lightlink.config.ConfigManager;
import io.lightlink.config.Script;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Utils {

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            sb.append(new String(buffer, 0, n));
        }
        return sb.toString();
    }


    public static String getResourceContent(String resource) throws IOException {
        InputStream fnStream;
        if ((ConfigManager.isInDebugMode())) {
            // without cache
            fnStream = Thread.currentThread().getContextClassLoader().getResource(resource).openStream();
        } else {
            fnStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        }
        String assertFns = streamToString(fnStream);
        fnStream.close();
        return assertFns;
    }

    public static Object tryConvertToJavaCollections(Object value) {
        if (value instanceof Object[]) {
            value = Arrays.asList((Object[]) value);
        } else if (value instanceof int[]) {
            List<Object> res = new ArrayList<Object>();
            for (int i = 0; i < ((int[]) value).length; i++) {
                res.add(((int[]) value)[i]);
            }
            return res;
        } else if (value instanceof double[]) {
            List<Object> res = new ArrayList<Object>();
            for (int i = 0; i < ((double[]) value).length; i++) {
                res.add(((double[]) value)[i]);
            }
            return res;
        } else if (value instanceof float[]) {
            List<Object> res = new ArrayList<Object>();
            for (int i = 0; i < ((float[]) value).length; i++) {
                res.add(((float[]) value)[i]);
            }
            return res;
        } else if (value instanceof ScriptObjectMirror && "Date".equalsIgnoreCase(((ScriptObjectMirror) value).getClassName())){
            Double time = (Double) ((ScriptObjectMirror) value).callMember("getTime");
            return new Date(time.longValue());

        } else if (value instanceof ScriptObject) {
            ScriptObject scriptObject = (ScriptObject) value;
            if (scriptObject.isArray()) {
                String[] ownKeys = scriptObject.getOwnKeys(false);
                List<Object> res = new ArrayList<Object>();
                for (String key : ownKeys) {
                    Object propertyValue = scriptObject.get(key);
                    propertyValue = tryConvertToJavaCollections(propertyValue);
                    res.add(propertyValue);
                }
                return res;

            } else {
                String[] ownKeys = scriptObject.getOwnKeys(true);
                Map<String, Object> res = new LinkedHashMap<String, Object>();
                for (String key : ownKeys) {
                    Object propertyValue = scriptObject.get(key);
                    propertyValue = tryConvertToJavaCollections(propertyValue);
                    res.put(key, propertyValue);
                }
                return res;
            }
        } else if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror ScriptObjectMirror = (ScriptObjectMirror) value;
            if (ScriptObjectMirror.isArray()) {
                String[] ownKeys = ScriptObjectMirror.getOwnKeys(false);
                List<Object> res = new ArrayList<Object>();
                for (String key : ownKeys) {
                    Object propertyValue = ScriptObjectMirror.get(key);
                    propertyValue = tryConvertToJavaCollections(propertyValue);
                    res.add(propertyValue);
                }
                return res;
            } else {
                String[] ownKeys = ScriptObjectMirror.getOwnKeys(true);
                Map<String, Object> res = new LinkedHashMap<String, Object>();
                for (String key : ownKeys) {
                    Object propertyValue = ScriptObjectMirror.get(key);
                    propertyValue = tryConvertToJavaCollections(propertyValue);
                    res.put(key, propertyValue);
                }
                return res;
            }
        }
        return value;
    }

    public static boolean isFirstNonAlphabetic(String name) {
        String[] parts = name.split("[\\./]"); // split by . or /

        for (String part : parts) {
            if (part.length()==0)
                continue;
            char c1 = part.charAt(0);
            if (!Character.isAlphabetic(c1))
                return true;
        }
        return false;
    }

    public static URL getUrl(String name, ServletContext servletContext) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        if (url == null && servletContext != null) {
            try {
                url = servletContext.getResource("/WEB-INF/" + name);
            } catch (MalformedURLException e) {/* keep trying*/ }
        }

        if (url == null && servletContext != null) {
            try {
                url = servletContext.getResource("/" + name);
            } catch (MalformedURLException e) {/* nothing found just return null */ }
        }

        if (url == null)
            return null;
        return url;
    }

//    public static HttpServletResponse createHttpServletResponseMock(final StringWriter sw) {
//        return (HttpServletResponse) Proxy.newProxyInstance(Utils.class.getClassLoader()
//                , new Class[]{HttpServletResponse.class}
//                , new InvocationHandler() {
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                return new PrintWriter(sw);
//            }
//        });
//    }
}
