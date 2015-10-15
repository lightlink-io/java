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


import io.lightlink.output.ResponseStream;
import io.lightlink.sql.SQLHandler;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class StreamingMapper implements RowMapper<Object> {

    private RowMapper nestedRowMapper;
    private String resultPropertyName;
    private int colCount;
    private String[] colNames;
    private ResponseStream responseStream;

    public StreamingMapper (ResponseStream responseStream) {
        this( responseStream, null);
    }

    public StreamingMapper (ResponseStream responseStream, RowMapper nestedRowMapper) {
        this.nestedRowMapper = nestedRowMapper;
        this.responseStream = responseStream;
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {

        if (nestedRowMapper != null) {
            Object row;
            row = nestedRowMapper.mapRow(resultSet, i);
            responseStream.writeFullObjectToArray(new BeanMap(row));
        } else {
            if (colCount == 0)
                readMetadata(resultSet);
            responseStream.writeObjectStart();
            for (int j = 0; j < colCount; j++) {
                Object value = resultSet.getObject(j + 1);
                Object converted = SQLHandler.genericConvertFromJdbc(null, value);
                responseStream.writeProperty(colNames[j], converted);
            }
            responseStream.writeObjectEnd();
        }
        return null;
    }

    private void readMetadata(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        colCount = metaData.getColumnCount();
        colNames = new String[colCount];
        for (int i = 0; i < colNames.length; i++) {
            colNames[i] = metaData.getColumnLabel(i + 1);
        }
    }
}
