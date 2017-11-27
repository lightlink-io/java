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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsProxyServlet extends RestServlet {


    private String getActionName(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        int pos = req.getRequestURI().indexOf(servletPath);
        return req.getRequestURI().substring(pos + servletPath.length());
    }

    @Override
    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {


        String actionName = getActionName(req);

        Object progressiveParams = inputParams.get("$lightlink-progressive");
        int[] progressiveBlockSizes = getProgressiveBlockSizes(progressiveParams);

        JSONHttpResponseStream responseStream = new JSONHttpResponseStream(resp, progressiveBlockSizes);

        getScriptRunner(req, resp).execute(actionName, "POST", inputParams, responseStream);

    }

    private int[] getProgressiveBlockSizes(Object progressiveParams) {
        int[]progressiveBlockSizes=null;
        if (progressiveParams instanceof List){
            List pp = (List) progressiveParams;
            progressiveBlockSizes = new int[pp.size()];
            for (int i = 0; i < pp.size(); i++) {
                progressiveBlockSizes[i] = ((Number) pp.get(i)).intValue();
            }
        } else if ("true".equals(progressiveParams)
                ||progressiveParams instanceof Boolean && ((Boolean) progressiveParams).booleanValue()){
            progressiveBlockSizes = new int[]{100,100,100,100,100,1000};
        }
        return progressiveBlockSizes;
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
