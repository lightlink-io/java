package io.lightlink.types;

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

