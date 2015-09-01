package io.lightlink.output;

import java.io.IOException;

public interface HttpResponseStream {

    public void flushBuffer() throws IOException;
    public void setHeader(String header, String value);
    public void setContentType(String value) ;

}
