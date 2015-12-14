package io.lightlink.output;

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


import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ObjectBufferResponseStream implements ResponseStream{


    private Object data = new LinkedHashMap<String, Object>();
    private Stack<Object> stack = new Stack<Object>();

    private RunnerContext runnerContext;

    public ObjectBufferResponseStream() {
        stack.push(data);
    }


    @Override public void end() { /* do nothing*/}

    @Override
    public void writeProperty(String name, Object value) {
        Map currentMap = (Map) stack.peek();
        currentMap.put(name,value);
    }

    @Override
    public void writeFullObjectToArray(Object value) {
        List list = (List) stack.peek();
        list.add(value);
    }

    @Override
    public void writePropertyArrayStart(String name) {
        Map currentMap = (Map) stack.peek();
        ArrayList<Object> list = new ArrayList<Object>();
        currentMap.put(name, list);
        stack.push(list);
    }

    @Override
    public void writePropertyArrayEnd() {
        stack.pop();
    }

    @Override
    public void writeObjectStart() {
        List current = (List) stack.peek();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        current.add(map);
        stack.push(map);
    }

    @Override
    public void writeObjectEnd() {
        stack.pop();
    }

//    public void writeArrayStart() {
//        if (stack.size()==1 && stack.peek() instanceof Map){
//            stack.clear();
//            data = new ArrayList<Object>();
//            stack.push(data);
//        }
//        List<Object> current = (List<Object>) stack.peek();
//        Map<String, Object> map = new LinkedHashMap<String, Object>();
//        current.add(map);
//        stack.push(map);
//    }
//
//    public void writeArrayEnd() {
//        stack.pop();
//    }

    @Override
    public void setRunnerContext(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public Object getData() {
        return data;
    }

    public Map<String,Object> getDataMap() {
        return data instanceof Map
                ?(Map<String, Object>) data
                :Collections.singletonMap("resultSet",data);
    }

    @Override
    public void writePropertyObjectStart(String name) {
        Map currentMap = (Map) stack.peek();
        Map<Object, Object> list = new LinkedHashMap<Object, Object>();
        currentMap.put(name, list);
        stack.push(list);
    }

    @Override
    public void writePropertyObjectEnd() {
        stack.pop();
    }

    @Override
    public boolean checkConnectionAlive() {
        return true;
    }

    @Override
    public void setContentType(String value) {
        // do nothing
    }

    @Override
    public void setHeader(String header, String value) {
        // do nothing
    }

    @Override
    public void flushBuffer() {
        // do nothing
    }
}
