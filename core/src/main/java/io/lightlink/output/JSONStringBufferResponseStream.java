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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

public class JSONStringBufferResponseStream extends JSONResponseStream {

    public JSONStringBufferResponseStream() {
        super(new ByteArrayOutputStream());
    }


    public String getBuffer() throws IOException {
        end();
        return ((ByteArrayOutputStream)getOutputStream()).toString("UTF-8");
    }
}