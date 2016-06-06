package io.lightlink.excel;

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
