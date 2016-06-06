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
import org.xml.sax.SAXException;

import java.io.Writer;

public class WorkbookTemplateHandler extends CopyingTemplateHandler {

    public WorkbookTemplateHandler(Writer out) {
        super(out);
    }

    @Override
    protected void printElementStart(String eName, Attributes attrs) throws SAXException {
        emit("\n<" + eName);
        if (eName.equals("calcPr")) {
            emit(" fullCalcOnLoad=\"1\"");
        }

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                emit(" " + aName + "=\"" + attrs.getValue(i) + "\"");
            }
        }

        emit(">");
    }
}
