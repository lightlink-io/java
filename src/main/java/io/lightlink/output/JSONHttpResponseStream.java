package io.lightlink.output;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JSONHttpResponseStream extends JSONResponseStream implements HttpResponseStream{

    protected HttpServletResponse response;

    public JSONHttpResponseStream(HttpServletResponse response) throws IOException {

        super(response.getOutputStream());

        this.response = response;
    }

    @Override
    public void flushBuffer() throws IOException {
        super.flushBuffer();
        response.flushBuffer();
    }

    @Override
    public void setHeader(String header, String value) {
        response.setHeader(header, value);
    }

    @Override
    public void setContentType(String value) {
        response.setContentType(value);
    }
}
