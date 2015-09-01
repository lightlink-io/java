package io.lightlink.servlet;

import io.lightlink.excel.StreamingExcelExporter;
import io.lightlink.output.BinaryDownloadResponseStream;
import io.lightlink.output.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class BinaryDownloadServlet extends RestServlet {
    public static final Logger LOG = LoggerFactory.getLogger(StreamingExcelExporter.class);

    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {

        String actionName = getAction(req);

        ResponseStream responseStream = new BinaryDownloadResponseStream(resp);

        getScriptRunner(req, resp).execute(actionName, method, inputParams, responseStream);

    }


}
