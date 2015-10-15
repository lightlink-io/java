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


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JSONHttpResponseStream extends JSONResponseStream implements HttpResponseStream{

    protected HttpServletResponse response;

    public static JSONHttpResponseStream getInstance(HttpServletResponse response) {
        try {
            return new JSONHttpResponseStream(response);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(),e);
        }
    }


    public JSONHttpResponseStream(HttpServletResponse response) throws IOException {

        super(response.getOutputStream());

        this.response = response;
    }

    @Override
    public void flushBuffer() throws IOException {
        super.flushBuffer();
        response.flushBuffer();
    }

    @Override
    public void setHeader(String header, String value) {
        response.setHeader(header, value);
    }

    @Override
    public void setContentType(String value) {
        response.setContentType(value);
    }

}
