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


import io.lightlink.autostop.AutoStopQuery;
import io.lightlink.output.JSONHttpResponseStream;
import io.lightlink.security.CSRFTokensContainer;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class JsProxyServlet extends RestServlet {

    protected void service(String method, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if ("/csrfTokenRenew".equals(getActionName(req))) {
            // renew csrf token before checking if the existing one, because it's invalid anyway
            JSONHttpResponseStream responseStream = new JSONHttpResponseStream(resp);
            responseStream.writeProperty("newToken", CSRFTokensContainer.getInstance(req.getSession()).createNewToken());
            responseStream.end();
        } else {
            super.service(method, req, resp);
        }
    }

    private String getActionName(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        int pos = req.getRequestURI().indexOf(servletPath);
        return req.getRequestURI().substring(pos + servletPath.length());
    }

    @Override
    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {


        String csrfToken = CSRFTokensContainer.getToken(inputParams);


        String actionName = getActionName(req);

        JSONHttpResponseStream responseStream = new JSONHttpResponseStream(resp);

        if ("/cancelQueries".equals(actionName)) {
            int canceled = AutoStopQuery.getInstance().cancelAllForWindow(csrfToken);
            responseStream.writeProperty("canceledQueries", canceled);
            responseStream.end();
        } else {
            getScriptRunner(req,resp).execute(actionName, "POST", inputParams, responseStream);
        }

    }

    @Override
    protected Map<String, Object> getParams(HttpServletRequest req) throws IOException {

        ServletInputStream inputStream = req.getInputStream();

        Map<String, Object> inputParams;
        try {
            inputParams = (Map<String, Object>) new JSONParser().parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
        inputStream.close();
        return inputParams;
    }
}
