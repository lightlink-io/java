package io.lightlink.spring;

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


import io.lightlink.config.ConfigManager;
import io.lightlink.output.ResponseStream;
import io.lightlink.utils.LogUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LightLinkFilter implements Filter {


    private static ThreadLocal<StreamingResponseData> threadLocalInstance = new ThreadLocal<StreamingResponseData>();


    public static boolean isThreadLocalStreamingDataSet() {
        return threadLocalInstance.get() != null;
    }

    public static StreamingResponseData getThreadLocalStreamingData() {
        StreamingResponseData res = threadLocalInstance.get();
        if (res == null)
            throw new IllegalStateException("LightLinkFilter must be mapped to this URL.");

        return res;
    }

    public static void setThreadLocalStreamingData(StreamingResponseData streamingData) {
        threadLocalInstance.set(streamingData);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        StreamingResponseData responseData = new StreamingResponseData((HttpServletRequest) request, (HttpServletResponse) response);

        try {
            threadLocalInstance.set(responseData);

            responseData.setProgressiveSlices(null); // todo

            try {

                chain.doFilter(request, response);

            } catch (RuntimeException e) {
                reportError(responseData, e);
                throw e;
            } catch (IOException e) {
                reportError(responseData, e);
                throw e;
            } catch (ServletException e) {
                reportError(responseData, e);
                throw e;
            }

            if (responseData.isResponseStreamInitiated()) {

                ResponseStream responseStream = responseData.getResponseStream();

                responseStream.writeProperty("success", true);
                responseStream.end();
            }

        } finally {
            responseData.end();
            threadLocalInstance.remove();
        }


    }

    public static ResponseStream initResponseStream(int[] progressiveBlockSizes) {
        return threadLocalInstance.get().initResponseStream(progressiveBlockSizes);
    }


    private void reportError(StreamingResponseData responseData, Throwable e) {
        ResponseStream stream = responseData.getResponseStream();

        stream.writeProperty("success", false);
        if (e != null) {
            LogUtils.error(this.getClass(), e);

            stream.writeProperty("exception", e.toString());

            if (ConfigManager.isInDebugMode()) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                stream.writeProperty("stackTrace", sw.toString());
            }

        }

        stream.end();

    }

}
