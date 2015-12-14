package io.lightlink.output.async;

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


import io.lightlink.output.HttpResponseStream;
import io.lightlink.utils.LogUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncHttpResponseStreamRunnable implements Runnable {


    BlockingQueue<QueueElement> queue = new LinkedBlockingQueue<QueueElement>(10000);
    HttpResponseStream facade;
    boolean syncMode = false;
    Object target;

    class AsyncInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (shouldSwitchToSyncMode(args)) {
                syncMode = true;
                ArrayList<QueueElement> elements = new ArrayList<QueueElement>();
                queue.drainTo(elements);
                BlockingQueue<QueueElement> syncQueue = new LinkedBlockingQueue<QueueElement>(1);
                syncQueue.addAll(syncQueue);
                BlockingQueue<QueueElement> oldQueue = queue;
                queue = syncQueue;
                oldQueue.put(new QueueElement(null, null));
            }

            queue.put(new QueueElement(method, args));


            return null;
        }

    }

    /**
     *  Detects the need to switch to sync mode in InputStream or a Reader is received from DB.
     *  Those objects might not be available when ResultSet moves to next record.
     * @param args
     * @return
     */
    private boolean shouldSwitchToSyncMode(Object[] args) {
        boolean asyncImpossible = false;
        if (args != null)
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof InputStream || arg instanceof Reader) {
                    asyncImpossible = true;
                }
            }
        return asyncImpossible;
    }

    public AsyncHttpResponseStreamRunnable(HttpResponseStream target) {
        this.target = target;
        this.facade = getAsyncProxy(HttpResponseStream.class);
    }

    public HttpResponseStream getFacade() {
        return facade;
    }

    private <T> T getAsyncProxy(Class<T> aClass) {
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{aClass},
                new AsyncInvocationHandler());
    }

    @Override
    public void run() {
        QueueElement queueElement;
        try {
            while (true) {
                queueElement = queue.take();

                if (queueElement == null)
                    break;

                if (queueElement.getMethod() == null) // NOP, used to unblock old queue when switching to sync Queue
                    continue;

                try {
                    queueElement.getMethod().invoke(target, queueElement.getArgs());
                } catch (IllegalAccessException e) {
                    LogUtils.error(this.getClass(), e);
                } catch (InvocationTargetException e) {
                    LogUtils.warn(this.getClass(), e);
                    queue = new NoOpBlockingQueue();
                    break;
                }

                if (queueElement.getMethod().getName().equals("end")) {
                    queue = new NoOpBlockingQueue();
                    break; // method=end -> Exit signal
                }
            }
        } catch (InterruptedException e) {
            /*do nothing*/
        } catch (Throwable t) {
            LogUtils.warn(this.getClass(), t);
        }
    }

    public Object getTarget() {
        return target;
    }
}
