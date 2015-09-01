package io.lightlink.output;

import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public interface ResponseStream {

    void end() throws IOException;

    void writeProperty(String name, Object value) throws IOException;

    void writeFullObjectToArray(Object value) throws IOException;

    void writePropertyObjectStart(String name) throws IOException;

    void writePropertyObjectEnt() throws IOException;

    void writePropertyArrayStart(String name) throws IOException;

    void writePropertyArrayEnd() throws IOException;

    void writeObjectStart() throws IOException;

    void writeObjectEnd() throws IOException;

    public void setHints(Hints hints);

    public void setRunnerContext(RunnerContext runnerContext) ;

    boolean checkConnectionAlive();

    public void setContentType(String value);
    public void setHeader(String header, String value);
    public void flushBuffer() throws IOException;
}
