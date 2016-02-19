package io.lightlink.output;

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


import io.lightlink.spring.LightLinkFilter;
import io.lightlink.spring.StreamingResponseData;
import junit.framework.TestCase;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProgressiveLoadTest extends TestCase {

    public void test() throws IOException, ParseException {


        HttpServletRequest servletRequestMock = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpServletRequest.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (args[0].equals("lightlink-progressive"))
                    return "100, 1000, 5000";
                else
                    return null;
            }
        });

        LightLinkFilter.
                setThreadLocalStreamingData(new StreamingResponseData(servletRequestMock, null));

        JSONStringBufferResponseStream bufferResponseStream = new JSONStringBufferResponseStream();

        bufferResponseStream.writePropertyArrayStart("resultSet");
        for (int i = 0; i < 11500; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("value1", Math.random());
            map.put("value2", Math.random());
            map.put("value3", Math.random());

            bufferResponseStream.writeFullObjectToArray(map);
        }
        bufferResponseStream.writePropertyArrayEnd();

        String responseText = bufferResponseStream.getBuffer();
        int lastIndex = responseText.lastIndexOf(JSONResponseStream.PROGRESSIVE_KEY_STR);

        responseText = responseText.substring(0, lastIndex);
        lastIndex--;

        responseText += "]";

        while (lastIndex > 0 && responseText.charAt(lastIndex) == '\t') {
            lastIndex--;
            responseText += "}";
        }

        Map res = (Map) new JSONParser().parse(responseText);
        Collection resultSet = (Collection) res.get("resultSet");
        assertEquals(resultSet.size(), 6100);

    }
}
