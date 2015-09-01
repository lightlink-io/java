package io.lightlink.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

public class JSONStringBufferResponseStream extends JSONResponseStream {

    public JSONStringBufferResponseStream() {
        super(new ByteArrayOutputStream());
    }

    public String getBuffer() throws IOException {
        end();
        return ((ByteArrayOutputStream)getOutputStream()).toString("UTF-8");
    }
}
