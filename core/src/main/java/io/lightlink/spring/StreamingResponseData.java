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


import io.lightlink.core.Hints;
import io.lightlink.output.HttpResponseStream;
import io.lightlink.output.JSONHttpResponseStream;
import io.lightlink.output.async.AsyncHttpResponseStreamRunnable;
import io.lightlink.utils.LogUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StreamingResponseData {

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    private Hints hints;
    private HttpResponseStream responseStream;
    private int[] progressiveSlices;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    private AsyncHttpResponseStreamRunnable runnable;
    private Future<?> taskFuture;

    public StreamingResponseData(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;

        if (servletRequest != null)
            this.hints = Hints.fromRequest(servletRequest);

    }

    public Hints getHints() {
        return hints;
    }

    public boolean isResponseStreamInitiated() {
        return responseStream != null;
    }

    public HttpResponseStream getResponseStream() {
        if (responseStream == null)
            try {
                runnable = new AsyncHttpResponseStreamRunnable(
                        new JSONHttpResponseStream(servletResponse));

                taskFuture = executorService.submit(runnable);

                responseStream = runnable.getFacade();
            } catch (IOException e) {
                throw new RuntimeException(e.toString(), e);
            }
        return responseStream;
    }


    public int[] getProgressiveSlices() {
        return progressiveSlices;
    }

    public void setProgressiveSlices(int[] progressiveSlices) {
        this.progressiveSlices = progressiveSlices;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void end() {
        if (isResponseStreamInitiated()) {
            responseStream.end();
            try {
                taskFuture.get();
            } catch (InterruptedException e) {
                LogUtils.error(getClass(), e);
            } catch (ExecutionException e) {
                LogUtils.error(getClass(), e);
            }
        }
    }
}
