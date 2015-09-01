package io.lightlink.output;

import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public class BinaryDownloadResponseStream extends JSONHttpResponseStream {

    public BinaryDownloadResponseStream(HttpServletResponse response) throws IOException {
        // avoid calling response.getWriter while response.getOutputStream() might be needed for binary data
        super(null);
    }

    @Override
    public Writer getOut() throws IOException {
        return response.getWriter();
    }

    @Override
    public synchronized void writeObjectEnd() throws IOException {
        /*must be ignored*/
    }

    @Override
    public synchronized void writeObjectStart() throws IOException {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyArrayEnd() throws IOException {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyArrayStart(String name) throws IOException {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyObjectEnt() throws IOException {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyObjectStart(String name) throws IOException {
        /*must be ignored*/
    }

    @Override
    public void writeString(String valueStr) throws IOException {
        getOut().write(valueStr);
    }

    @Override
    public void writeUnquoted(Object value) throws IOException {
        writeString("" + value);
    }

    @Override
    public void writeInputStream(InputStream value) throws IOException {
        IOUtils.copy(value, response.getOutputStream());
        super.writeInputStream(value);
    }

    @Override
    public boolean checkConnectionAlive() {
        return true; // inserting whitespaces is not tolerated with binary stream.
    }
}
