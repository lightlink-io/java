package io.lightlink.excel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class CopyingTemplateHandler extends DefaultHandler {
    protected Writer out;
    StringBuffer textBuffer = new StringBuffer();

    public CopyingTemplateHandler(Writer out) {
        this.out = out;
    }

    public void startDocument() throws SAXException {
        emit("<?xml version='1.0' encoding='UTF-8'?>");
    }

    public void endDocument() throws SAXException {

        try {
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    public void characters(char[] buf, int offset, int len)
            throws SAXException {
        String s = new String(buf, offset, len);

        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }

    protected void emit(String s) {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void startElement(String namespaceURI, String sName, // simple name
                             String qName, // qualified name
                             Attributes attrs) throws SAXException {
        echoText();

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }

        printElementStart(eName, attrs);

    }

    public void endElement(String namespaceURI, String sName, // simple name
                           String qName // qualified name
    ) throws SAXException {

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }


        echoText();
        emit("</" + eName + ">\n");


        textBuffer.setLength(0);

    }

    protected void printElementStart(String eName, Attributes attrs) throws SAXException {
        emit("\n<" + eName);

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

    protected void printEmptyElement(String eName, Map<String, Object> attrs) {
        printElementNameAndAttributes(eName, attrs);
        emit("/>");

    }

    protected void printElementStart(String eName, Map<String, Object> attrs) {
        printElementNameAndAttributes(eName, attrs);
        emit(">");
    }

    protected void printElementNameAndAttributes(String eName, Map<String, Object> attrs) {
        emit("\n<" + eName);
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!key.startsWith("!"))
                emit(" " + key + "=\"" + value + "\"");
        }
    }

    protected void echoText() throws SAXException {
        emit(textBuffer.toString());
        textBuffer.setLength(0);
    }
}
