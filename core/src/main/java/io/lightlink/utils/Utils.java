package io.lightlink.utils;

import io.lightlink.config.ConfigManager;
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
