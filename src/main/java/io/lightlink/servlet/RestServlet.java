package io.lightlink.servlet;

import io.lightlink.output.JSONHttpResponseStream;
import io.lightlink.security.CSRFTokensContainer;
import io.lightlink.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RestServlet extends AbstractLightLinkServlet {




    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {

        String actionName = getAction(req);
        if (Utils.isFirstNonAlphabetic(actionName))
            return; // resources with non-alpha first char considered private/library and not available for calling

        getScriptRunner(req, resp).execute(actionName, method, inputParams, new JSONHttpResponseStream(resp));
    }

    protected Map<String, Object> getParams(HttpServletRequest req) throws IOException {
        Map<String, Object> inputParams = new HashMap<String, Object>();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] value = entry.getValue();
            if (value !=null && value.length==1)
                inputParams.put(entry.getKey(),value[0]);
            else
                inputParams.put(entry.getKey(),value);
        }
        return inputParams;
    }

    protected String getAction(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        int pos = req.getRequestURI().indexOf(servletPath);
        String res = req.getRequestURI().substring(pos + servletPath.length());
        return res.replaceAll("\\.xlsx$","");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servise("GET", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servise("POST", req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servise("PUT", req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servise("DELETE", req, resp);
    }

    protected void servise(String method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> params =  getParams(req);
        CSRFTokensContainer tokensContainer = CSRFTokensContainer.getInstance(req.getSession());
        String token = tokensContainer.validate(params);
        if (token==null){
            tokensContainer.sendCsrfError(resp);
            return;
        }


        resp.setContentType("application/json; charset=UTF-8");
//        resp.setCharacterEncoding("UTF-8");

        doServide(method, req, resp, params);
    }


}
