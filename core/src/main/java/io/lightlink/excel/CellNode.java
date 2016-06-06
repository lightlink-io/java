package io.lightlink.excel;

/*
 * #%L
 * LightLink Core
 * %%
 * Copyright (C) 2015 - 2016 Vitaliy Shevchuk
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


import org.xml.sax.Attributes;

import java.util.Map;

public class CellNode extends AttrNode{

    private String formula;

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public CellNode(Attributes attrs) {
        super(attrs);
    }

    public CellNode(String value, String decodedValue, Map<String, Object> attributes) {
        super(attributes);
        this.value = value;
        this.decodedValue = decodedValue;
    }

    String value, decodedValue;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void changeValue(String value) {
        getAttributes().put("t","inlineStr");
        this.value = value;
    }
    public void changeValue(Number value) {
        getAttributes().put("t","n");
        this.value = ""+value;
    }

    public String getDecodedValue() {
        return decodedValue;
    }

    public void setDecodedValue(String decodedValue) {
        this.decodedValue = decodedValue;
    }

    public CellNode clone(){
        return new CellNode(value, decodedValue, attributes);
    }

}
