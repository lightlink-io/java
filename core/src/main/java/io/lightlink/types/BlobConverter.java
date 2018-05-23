package io.lightlink.types;

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


import io.lightlink.core.RunnerContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;

public class BlobConverter extends AbstractConverter {


    public static AbstractConverter getInstance() {
        return new BlobConverter();
    }

    private String encoding;

    @Override
    public void setConfig(String config) {
        encoding = config;
    }

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException {
        if (value == null)
            return null;


        if (value instanceof InputStream && StringUtils.isBlank(encoding)) {
            return value;
        } else {

            byte[] array;
            if (value instanceof byte[]) {
                array = (byte[]) value;
            } else if (value instanceof InputStream) {
                array = IOUtils.toByteArray((InputStream) value);
            } else if (value instanceof String) {
                String str = (String) value;
                if (encoding.equalsIgnoreCase("base64"))
                    array = DatatypeConverter.parseBase64Binary(str);
                else
                    try {
                        array = str.getBytes(encoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException("Cannot convert value:" + value + " to Blob. Specified encoding:" + encoding + " not recognised. (note:default encoding for String is base64)");
                    }

            } else {
                throw new IllegalArgumentException("Cannot convert value:" + value + " of type:" + value.getClass().getName()
                        + " to Blob. int[] or String expected. (note:default encoding for String is base64)");
            }

            return new ByteArrayInputStream(array);
        }
    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        InputStream stream = resultSet.getBinaryStream(pos);
        return stream == null ? null : inputStreamToReturnValue(stream);

    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        InputStream stream = cs.getBlob(pos).getBinaryStream();
        return stream == null ? null : inputStreamToReturnValue(stream);

    }

    private Object inputStreamToReturnValue(InputStream stream) throws IOException {

        if (encoding.equalsIgnoreCase("base64")) {
            String res = DatatypeConverter.printBase64Binary(IOUtils.toByteArray(stream));
            stream.close();
            return res;
        } else if (StringUtils.isNotBlank(encoding)) {
            String res = new String(IOUtils.toByteArray(stream), encoding);
            stream.close();
            return res;
        } else
            return stream;
    }

    @Override
    public Integer getSQLType() {
        return Types.BLOB;
    }

}
