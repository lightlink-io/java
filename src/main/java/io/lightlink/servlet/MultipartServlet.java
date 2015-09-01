package io.lightlink.servlet;

import io.lightlink.output.JSONHttpResponseStream;
import org.apache.commons.fileupload.FileUploadException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class MultipartServlet extends RestServlet {


    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {

        String actionName = getAction(req);

        getScriptRunner(req, resp).execute(actionName, method, inputParams, new JSONHttpResponseStream(resp));
    }

    protected Map<String, Object> getParams(HttpServletRequest req) throws IOException {

        MultipartParameters multipart = new MultipartParameters(req);

        Map<String, Object> inputParams = null;
        try {
            inputParams = multipart.getInitialParams();
        } catch (FileUploadException e) {
            throw new IOException(e.getMessage(),e);
        }
        inputParams.put("mutipart",multipart);

        return inputParams;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doServide("POST", req, resp, getParams(req));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doServide("PUT", req, resp, getParams(req));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doServide("DELETE", req, resp, getParams(req));
    }


}
