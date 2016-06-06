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
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class SharedStringsHandler extends DefaultHandler {

    StringBuffer textBuffer = new StringBuffer();
    List<String> sharedStings = new ArrayList<String>();

    StringBuffer currentText = new StringBuffer();

    public void startElement(String namespaceURI, String sName, // simple name
                             String qName, // qualified name
                             Attributes attrs) throws SAXException {
        textBuffer.setLength(0);

        if (qName.equals("si"))
            currentText.setLength(0);

    }

    public void endElement(String namespaceURI, String sName, // simple name
                           String qName // qualified name
    ) throws SAXException {
        if (qName.equals("t"))
            currentText.append(textBuffer.toString());
        if (qName.equals("si"))
            sharedStings.add(currentText.toString());

        textBuffer.setLength(0);
    }

    public void characters(char[] buf, int offset, int len)
            throws SAXException {

        textBuffer.append(buf, offset, len);
    }

    public List<String> getSharedStings() {
        return sharedStings;
    }
}