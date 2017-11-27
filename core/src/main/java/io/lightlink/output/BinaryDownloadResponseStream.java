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


import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class BinaryDownloadResponseStream extends JSONHttpResponseStream {

    public BinaryDownloadResponseStream(HttpServletResponse response) throws IOException {
        // avoid calling response.getWriter while response.getOutputStream() might be needed for binary data
        super(null,null);
    }


    @Override
    public synchronized void writeObjectEnd() {
        /*must be ignored*/
    }

    @Override
    public synchronized void writeObjectStart() {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyArrayEnd() {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyArrayStart(String name) {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyObjectEnd() {
        /*must be ignored*/
    }

    @Override
    public synchronized void writePropertyObjectStart(String name) {
        /*must be ignored*/
    }

    @Override
    public void writeString(String valueStr) {
        try {
            write(valueStr.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public void writeUnquoted(Object value) {
        writeString("" + value);
    }

    @Override
    public void writeInputStream(InputStream value) {
        try {
            IOUtils.copy(value, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public boolean checkConnectionAlive() {
        return true; // inserting whitespaces is not tolerated with binary stream.
    }
}
