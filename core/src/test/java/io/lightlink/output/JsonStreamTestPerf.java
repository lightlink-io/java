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


import io.lightlink.config.ConfigManager;
import io.lightlink.core.Hints;
import io.lightlink.spring.LightLinkFilter;
import io.lightlink.spring.StreamingResponseData;
import junit.framework.TestCase;
import org.apache.commons.io.output.NullOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class JsonStreamTestPerf extends TestCase{

    public void test() throws IOException, InterruptedException {
        JSONResponseStream bufferResponseStream;
        HashMap<String, Object> map = getData();

        LightLinkFilter.setThreadLocalStreamingData(new StreamingResponseData(null,null));

        bufferResponseStream = new JSONResponseStream(new NullOutputStream());
        bufferResponseStream.writePropertyArrayStart("resultSet");

        for (int i=0;i<100000;i++) {
            bufferResponseStream.writeFullObjectToArray(map);
        }

        bufferResponseStream = new JSONResponseStream(new NullOutputStream());
        bufferResponseStream.writePropertyArrayStart("resultSet");

        long l = System.currentTimeMillis();

        for (int i=0;i<100000;i++) {
            bufferResponseStream.writeFullObjectToArray(map);
        }

        System.out.println( "time prd:"+       ( System.currentTimeMillis()-l)/1000F);

        System.setProperty("lightlink.debug","true");
        bufferResponseStream = new JSONResponseStream(new NullOutputStream());
        bufferResponseStream.writePropertyArrayStart("resultSet");


        l = System.currentTimeMillis();

        for (int i=0;i<100000;i++) {
            bufferResponseStream.writeFullObjectToArray(map);
        }

        System.out.println( "time debug:"+       ( System.currentTimeMillis()-l)/1000F);
    }

    private HashMap<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("value1",Math.random());
        map.put("value2",""+Math.random());
        map.put("value3",""+Math.random());
        map.put("value5",""+Math.random());
        map.put("value6",""+Math.random());
        map.put("value7",""+Math.random());
        map.put("value8",""+Math.random());
        map.put("value9",""+Math.random());
        map.put("value10",""+Math.random());
        return map;
    }
}
