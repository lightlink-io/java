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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: 303367
 * Date: 13/03/15
 * Time: 17:16
 */
public class AttrNode {

    Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public AttrNode(Map<String, Object> attributes) {
        this.attributes = new LinkedHashMap<String, Object>(attributes);
    }

    public AttrNode(Attributes attrs) {
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                attributes.put(aName, attrs.getValue(i));
            }
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getCoordinates(){
        return (String) getAttributes().get("r");
    }

}
