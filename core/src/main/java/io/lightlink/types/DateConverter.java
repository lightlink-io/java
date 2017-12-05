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
import io.lightlink.facades.TypesFacade;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter extends AbstractConverter {

    public static final String UNIVERSAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String[][] DATE_PATTERNS = new String[][]{

            //FIRST ONE IS UNIVERSAL
            {"^\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d.*$", UNIVERSAL_DATE_FORMAT},


            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d$", "yyyy-MM-dd"},
            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d\\ \\d\\d:\\d\\d$", "yyyy-MM-dd HH:mm"},
            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d\\ \\d\\d:\\d\\d:\\d\\d$", "yyyy-MM-dd HH:mm:ss"},

            {"^\\d\\d\\d\\d/\\d\\d/\\d\\d$", "yyyy/MM/dd"},
            {"^\\d\\d\\d\\d/\\d\\d/\\d\\d\\ \\d\\d:\\d\\d$", "yyyy/MM/dd HH:mm"},
            {"^\\d\\d\\d\\d/\\d\\d/\\d\\d\\ \\d\\d:\\d\\d:\\d\\d$", "yyyy/MM/dd HH:mm:ss"},

            {"^\\d\\d\\d\\d\\d\\d\\d\\d$", "yyyyMMdd"},
            {"^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d$", "yyyyMMddHHmmss"},

    };

    public static final DateConverter instance = new DateConverter();

    public static DateConverter getInstance() {
        return instance;
    }

    private String explicitPattern;

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) {
        if (value == null || (value instanceof String && ((String) value).trim().length() == 0))
            return null;
        else if (value instanceof ScriptObjectMirror && ((ScriptObjectMirror) value).getClassName().equals("Date")) {
            Number time = (Number) ((ScriptObjectMirror) value).callMember("getTime");
            return new java.sql.Date(time.longValue());
        } else
            return tryToConvertToDate(runnerContext.getTypesFacade(), name, value);
    }

    protected Timestamp tryToConvertToDate(TypesFacade typesFacade, String name, Object v) {
        if (v instanceof Date)
            return new Timestamp(((Date) v).getTime());
        else if (v instanceof Number)
            return new Timestamp(((Number) v).longValue());

        String vStr = v.toString();

        if (explicitPattern != null) {
            try {
                return new Timestamp(new SimpleDateFormat(explicitPattern).parse(vStr).getTime());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Cannot parse as date the value:" + vStr + " with given format:" + explicitPattern + " for field:" + name);
            }
        }

        String customPattern = typesFacade.getCustomDatePattern();
        if (customPattern != null) {
            try {
                return new Timestamp(new SimpleDateFormat(customPattern).parse(vStr).getTime());
            } catch (ParseException e) {
                /*try generic formats */
            }
        }

        Timestamp d = null;
        try {
            for (String[] pattern : DATE_PATTERNS) {
                if (vStr.matches(pattern[0])) {
                    d = new Timestamp(new SimpleDateFormat(pattern[1]).parse(vStr).getTime());
                    break;
                }
            }
        } catch (ParseException e) { /**/ }

        if (d == null) {
            throw new IllegalArgumentException("Cannot parse as date the value:" + v + " field:" + name);
        }

        return d;
    }

    public Object convertFromJdbc(Object value) {
        if (value != null && value instanceof Date) {
            if (explicitPattern != null)
                return new SimpleDateFormat(explicitPattern).format(((Date) value).getTime());
            else
                return new Date(((Date) value).getTime());
        } else
            return value;
    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        return convertFromJdbc(
                super.readFromResultSet(resultSet, pos, runnerContext, colName));
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        return convertFromJdbc(
                super.readFromCallableStatement(cs, pos, runnerContext, colName));
    }

    @Override
    public Integer getSQLType() {
        return Types.TIMESTAMP;
    }

    @Override
    public void setConfig(String config) {
        this.explicitPattern = config;
    }
}
