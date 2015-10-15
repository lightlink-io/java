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


public class ArgInfo {


    private boolean in=true, out=false;
    private String name;
    private AbstractConverter converter;
    private Object value;

    public ArgInfo(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractConverter getConverter() {
        return converter;
    }

    public void setConverter(AbstractConverter converter) {
        this.converter = converter;
    }

    public Integer findOutSqlType() {

        if (getConverter()!=null)
            return getConverter().getSQLType();
        else
            return null;

    }


    public String findOutSqlTypeName() {
        if (getConverter()!=null)
            return getConverter().getCustomSQLTypeName();
        else
            return null;


    }

    @Override
    public String toString() {
        return "ArgInfo{" +
                "in=" + in +
                ", out=" + out +
                ", name='" + name + '\'' +
                ", converter=" + converter +
                ", value=" + value +
                '}';
    }
}
