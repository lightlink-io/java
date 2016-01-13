package io.lightlink.servlet;

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


import io.lightlink.output.JSONHttpResponseStream;
import io.lightlink.security.CSRFTokensContainer;
import io.lightlink.spring.LightLinkFilter;
import io.lightlink.spring.StreamingResponseData;
import io.lightlink.utils.Utils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RestServlet extends AbstractLightLinkServlet {

    private boolean noCSRF = false;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String tokenCheck = config.getInitParameter("No-CSRF-token-check");
        noCSRF = tokenCheck != null && (tokenCheck.equalsIgnoreCase("true"));
    }

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
            if (value != null && value.length == 1)
                inputParams.put(entry.getKey(), value[0]);
            else
                inputParams.put(entry.getKey(), value);
        }
        return inputParams;
    }

    protected String getAction(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        int pos = req.getRequestURI().indexOf(servletPath);
        String res = req.getRequestURI().substring(pos + servletPath.length());
        return res.replaceAll("\\.xlsx$", "");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service("GET", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service("POST", req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service("PUT", req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service("DELETE", req, resp);
    }

    protected void service(String method, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        LightLinkFilter.setThreadLocalStreamingData(new StreamingResponseData(req, resp));

        Map<String, Object> params = getParams(req);
        boolean safe = noCSRF || csrfCheck(req, resp, params);
        if (safe) {
            resp.setContentType("application/json; charset=UTF-8");
            doServide(method, req, resp, params);
        }
    }

    protected boolean csrfCheck(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> params) throws IOException {

        CSRFTokensContainer tokensContainer = CSRFTokensContainer.getInstance(req.getSession());
        String token = tokensContainer.validate(params);

        if (token == null) {
            tokensContainer.sendCsrfError(resp);
            return false;
        } else {
            return true;
        }
    }


}
