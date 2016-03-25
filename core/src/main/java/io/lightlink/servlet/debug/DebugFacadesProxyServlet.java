package io.lightlink.servlet.debug;

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


import io.lightlink.output.JSONHttpResponseStream;
import io.lightlink.output.ResponseStream;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugFacadesProxyServlet extends HttpServlet {
    public static final Logger LOG = LoggerFactory.getLogger(DebugFacadesProxyServlet.class);

    public DebugFacadesProxyServlet() {
        int x = 0;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            Map params = getJsonParamsMap(request);

            ResponseStream json = new JSONHttpResponseStream(response);

            dispatch(request, params, json);
        } catch (Exception e) {
            sendError(response, e);

        }

    }

    void dispatch(HttpServletRequest request, Map params, ResponseStream json) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        if ("create".equals(params.get("action"))) {
            create(params, json);
        } else if ("createClass".equals(params.get("action"))) {
            createClass(params, json);
        } else if ("invoke".equals(params.get("action"))) {
            invoke(request, params, json);
        }
    }

    private void sendError(HttpServletResponse response, Exception e) throws IOException {
        LOG.error(e.toString(), e);

        ResponseStream json = new JSONHttpResponseStream(response);

        json.writeProperty("exception", e.getClass().getName());
        json.writeProperty("message", e.toString());
        StringWriter traceWriter = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(traceWriter);
        e.printStackTrace(pw);
        pw.close();
        json.writeProperty("stackTrace", traceWriter.toString());
        json.end();
    }

    private void invoke(HttpServletRequest request, Map params, ResponseStream json) throws IllegalAccessException, InvocationTargetException, IOException {
        String methodName = (String) params.get("methodName");

        Integer objectId = ((Number) params.get("objectId")).intValue();
        List args = (List) params.get("args");

        replaceStubsByObjects(args);

        ObjectPoolElement element = pool.get(objectId);
        Object instance = element.getObject();

        if (instance instanceof HttpServletRequest)
            instance = request;

        Class cls = (instance instanceof Class) ? (Class) instance : instance.getClass();

        Method[] allMethods = cls.getMethods();
        List<Method> sameName = new ArrayList<Method>();
        for (Method method : allMethods) {
            if (methodName.equals(method.getName()))
                sameName.add(method);
        }
        Method[] methods = sameName.toArray(new Method[sameName.size()]);
        MethodOrConstructorWrapper method = findMethod(args, MethodOrConstructorWrapper.getArray(methods));

        if (method == null)
            throw new RuntimeException("Cannot find method:" + methodName + " for class:" + cls + " wiht parameters: " + args);

        Object resp = invokeMethod(args, instance, method.getMethod());

        returnObject(resp, element.getGeneration(), json);
    }

    private Object invokeMethod(List args, Object instance, Method method) throws IllegalAccessException, InvocationTargetException {


        Object[] argsArray = args.toArray();
        Class<?>[] pTypes = method.getParameterTypes();
        for (int i = 0; i < argsArray.length; i++) {
            Object arg = argsArray[i];
            if (arg instanceof Object[] && pTypes[i].equals(byte[].class)) {
                Object[] argObjArray = (Object[]) arg;
                byte[] bytes = new byte[argObjArray.length];
                for (int j = 0; j < argObjArray.length; j++) {
                    bytes[j] = ((Number) argObjArray[j]).byteValue();
                }
                argsArray[i] = bytes;
            }
            if (arg instanceof Object[] && pTypes[i].equals(Iterable.class)) {
                argsArray[i] = Arrays.asList((Object[]) arg);
            }
        }

        return method.invoke(instance, argsArray);
    }

    private void createClass(Map params, ResponseStream json) throws ClassNotFoundException, IOException {
        purgeGenerations();

        String className = (String) params.get("className");
        Long generation = new Long("" + params.get("generation"));

        Class aClass = Class.forName(className);
        returnObject(aClass, generation, json);

    }

    private void create(Map params, ResponseStream json) throws ClassNotFoundException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, IOException {
        String className = (String) params.get("className");
        Long generation = new Long("" + params.get("generation"));
        List args = (List) params.get("args");

        replaceStubsByObjects(args);

        Constructor[] candidates = Class.forName(className).getConstructors();
        MethodOrConstructorWrapper constructor = findMethod(args, MethodOrConstructorWrapper.getArray(candidates));
        if (constructor == null)
            throw new RuntimeException("Cannot find constructor for class:" + className + " wiht parameters: " + args);

        Object instance = constructor.getConstructor().newInstance(args.toArray());

        returnObject(instance, generation, json);
    }


    private Map getJsonParamsMap(HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();

        Map params;
        try {
            params = (Map) new JSONParser().parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.toString(), e);
        }

        inputStream.close();
        return params;
    }

    // todo : send and receive Collections/Maps

    private void returnObject(Object instance, Long generation, ResponseStream json) throws IOException {

        writeGenericInstance(instance, generation, json);
        json.end();
    }

    private void writeGenericInstance(Object instance, Long generation, ResponseStream json) throws IOException {
        if (instance == null
                || instance instanceof String
                || instance instanceof Number
                || (
                instance.getClass().isArray() &&
                        (instance.getClass().getComponentType().isPrimitive() ||
                                instance.getClass().getComponentType().isInstance(Number.class) ||
                                instance.getClass().getComponentType().isInstance(String.class)
                        ))
                ) {
            writeSimpleType(instance, json);
        } else if (instance instanceof Map) {
            writeArray((Map<Object, Object>) instance, generation, json);
        } else if (instance instanceof Collection) {
            writeObject((Collection) instance, generation, json);
        } else {
            writePooledObject(instance, generation, json);
        }
    }


    private void writeObject(Collection instance, Long generation, ResponseStream json) throws IOException {
        json.writeProperty("type", "array");

        Collection map = (Collection) instance;
        json.writePropertyArrayStart("values");
        for (Object el : map) {
            json.writeObjectStart();
            writeGenericInstance(el, generation, json);
            json.writeObjectEnd();
        }
        json.writePropertyArrayEnd();
    }

    private void writeArray(Map<Object, Object> instance, Long generation, ResponseStream json) throws IOException {
        json.writeProperty("type", "map");

        Map<Object, Object> map = (Map<Object, Object>) instance;
        for (Map.Entry entry : map.entrySet()) {

            json.writePropertyObjectStart("" + entry.getKey());
            writeGenericInstance(entry.getValue(), generation, json);
            json.writePropertyObjectEnd();
        }
    }

    private void writePooledObject(Object instance, Long generation, ResponseStream json) throws IOException {

        int objectId = -1;
        ObjectPoolElement[] elements = pool.values().toArray(new ObjectPoolElement[0]);
        for (ObjectPoolElement element : elements) {
            if (element.getObject() == instance) {
                objectId = element.getObjectId();
                break;
            }
        }
        if (objectId == -1) {
            objectId = nextObjectId.getAndIncrement();
            pool.put(objectId, new ObjectPoolElement(generation, objectId, instance));
        }
        json.writeProperty("type", "pooled");
        json.writeProperty("objectId", objectId);
        boolean isClass = instance instanceof Class;
        if (isClass) {
            json.writeProperty("className", ((Class) instance).getName());
        }
        Set<String> methods = new HashSet<String>();
        for (Method method : (isClass ? (Class) instance : instance.getClass()).getMethods()) {
            if (isClass) {
                if (Modifier.isStatic(method.getModifiers()))
                    methods.add(method.getName()); // only static methods
            } else {
                methods.add(method.getName());
            }
        }

        Map<String, Object> fields = new HashMap<String, Object>();
        for (Field field : (isClass ? (Class) instance : instance.getClass()).getFields()) {
            if (!isClass || Modifier.isStatic(field.getModifiers())) { // only static methods for classes
                try {
                    Object value = field.get(instance);
                    if (value instanceof Number || value instanceof String || value instanceof Boolean)
                        fields.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    /*do nothing */
                }
            }
        }


        json.writeProperty("methods", new ArrayList<String>(methods));
        json.writeProperty("fields", fields);
    }

    private void writeSimpleType(Object instance, ResponseStream json) throws IOException {
        json.writeProperty("type", "simple");
        json.writeProperty("value", instance);
    }

    private MethodOrConstructorWrapper findMethod(List args, MethodOrConstructorWrapper[] methods) {
        List<MethodOrConstructorWrapper> candidates = new ArrayList<MethodOrConstructorWrapper>();
        for (MethodOrConstructorWrapper c : methods) {
            if (c.getParameterTypes().length == args.size())
                candidates.add(c);
        }

        if (candidates.size() == 0)
            return null;

        outer:
        for (MethodOrConstructorWrapper candidate : candidates) {
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Object arg = args.get(i);
                Class<?> t = parameterTypes[i];
                if (arg != null && arg instanceof Long) {
                    arg = getSmallestNumericType((Long) arg);
                    args.set(i, arg);
                    if (t != int.class && t != long.class && t != byte.class && t != short.class
                            && t != Integer.class && t != Long.class && t != Byte.class && t != Short.class
                            && t != Number.class && t != Object.class)
                        continue outer;

                } else if (arg != null && arg instanceof Double) {

                    if (t != double.class && t != Double.class && t != float.class && t != Float.class && t != Object.class)
                        continue outer;

                } else if (arg != null && arg instanceof Boolean) {
                    if (t != Boolean.class && t != boolean.class && t != Object.class)
                        continue outer;
                } else if (arg instanceof JSONArray) {
                    JSONArray arr = (JSONArray) arg;
                    Object[] array = arr.toArray();
                    for (int j = 0; j < array.length; j++) {
                        Object o = array[j];
                        if (o instanceof Long)
                            array[j] = getSmallestNumericType((Long) o);
                    }
                    args.set(i, array);

                } else if (arg != null && !t.isAssignableFrom(arg.getClass())) {
                    continue outer;
                }
            }
            return candidate;
        }

        return null;
    }

    private Object getSmallestNumericType(Long arg) {
        if (arg < Byte.MAX_VALUE) {
            return arg.byteValue();
        } else if (arg < Short.MAX_VALUE) {
            return arg.shortValue();
        } else if (arg < Integer.MAX_VALUE) {
            return arg.intValue();
        }
        return arg;
    }


    AtomicInteger nextObjectId = new AtomicInteger(1);
    HashMap<Integer, ObjectPoolElement> pool = new HashMap<Integer, ObjectPoolElement>();
    TreeSet<Long> generations = new TreeSet<Long>();


    private void replaceStubsByObjects(Map map) {
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object value = entry.getValue();
            if (value instanceof Map) {
                Number objectId = (Number) ((Map) value).get("objectId");
                if (objectId != null) {
                    entry.setValue(pool.get(objectId.intValue()).getObject());
                } else {
                    replaceStubsByObjects((Map) value);
                }
            } else if (value instanceof List) {
                replaceStubsByObjects((List) value);
            }
        }

    }

    private void replaceStubsByObjects(List args) {
        for (int i = 0; i < args.size(); i++) {
            Object value = args.get(i);
            if (value instanceof Map) {
                Number objectId = (Number) ((Map) value).get("objectId");
                if (objectId != null) {
                    args.set(i, pool.get(objectId.intValue()).getObject());
                } else {
                    replaceStubsByObjects((Map) value);
                }
            } else if (value instanceof List) {
                replaceStubsByObjects((List) value);
            }

        }
    }


    /**
     * Removes older generations (generation=browser page refresh) objects from pool (more then 10 generation ago)
     */
    public void purgeGenerations() {
        while (generations.size() > 10) {
            long genToRemove = generations.pollFirst();
            for (Iterator<Map.Entry<Integer, ObjectPoolElement>> iterator = pool.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Integer, ObjectPoolElement> entry = iterator.next();
                if (entry.getValue().getGeneration() == genToRemove)
                    iterator.remove();
            }
        }
    }

}
