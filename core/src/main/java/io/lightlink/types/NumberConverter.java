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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Types;

public class NumberConverter extends AbstractConverter {

    public static final NumberConverter instance = new NumberConverter();

    public static NumberConverter getInstance() {
        return instance;
    }

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) {
        try {
            if (value == null)
                return null;
            else if (value instanceof String && ((String) value).length() == 0)
                return null;
            else
                return new BigDecimal(value.toString());

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert field:"+name+" value :"+value+" to numeric");
        }
    }

    @Override
    public Integer getSQLType() {
        return Types.NUMERIC;
    }

}

