package io.lightlink.output;

import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ObjectBufferResponseStream implements ResponseStream{


    private Map<String,Object> data = new LinkedHashMap<String, Object>();
    private Stack stack = new Stack();

    private RunnerContext runnerContext;

    public ObjectBufferResponseStream() {
        stack.push(data);
    }

    @Override
    public void setHints(Hints hints) {
        // do nothing
    }

    @Override public void end() throws IOException { /* do nothing*/}

    @Override
    public void writeProperty(String name, Object value) throws IOException {
        Map currentMap = (Map) stack.peek();
        currentMap.put(name,value);
    }

    @Override
    public void writeFullObjectToArray(Object value) throws IOException {
        List list = (List) stack.peek();
        list.add(value);
    }

    @Override
    public void writePropertyArrayStart(String name) throws IOException {
        Map currentMap = (Map) stack.peek();
        ArrayList<Object> list = new ArrayList<Object>();
        currentMap.put(name, list);
        stack.push(list);
    }

    @Override
    public void writePropertyArrayEnd() throws IOException {
        stack.pop();
    }

    @Override
    public void writeObjectStart() throws IOException {
        List current = (List) stack.peek();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        current.add(map);
        stack.push(map);
    }

    @Override
    public void writeObjectEnd() throws IOException {
        stack.pop();
    }


    @Override
    public void setRunnerContext(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public void writePropertyObjectStart(String name) throws IOException {
        Map currentMap = (Map) stack.peek();
        Map<Object, Object> list = new LinkedHashMap<Object, Object>();
        currentMap.put(name, list);
        stack.push(list);
    }

    @Override
    public void writePropertyObjectEnt() throws IOException {
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
    public void flushBuffer() throws IOException {
        // do nothing
    }
}
