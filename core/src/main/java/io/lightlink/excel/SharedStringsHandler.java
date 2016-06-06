package io.lightlink.excel;


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