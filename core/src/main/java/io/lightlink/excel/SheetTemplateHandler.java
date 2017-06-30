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



import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public class SheetTemplateHandler extends CopyingTemplateHandler implements RowPrintCallback {

    private RowNode rowNode;
    List<String> sharedStrings;

    int rowNumber = 0;

    ExcelStreamVisitor visitor;


    public SheetTemplateHandler(Writer out, List<String> sharedStrings, ExcelStreamVisitor visitor) {
        super(out);
        this.sharedStrings = sharedStrings;
        this.visitor = visitor;
    }

    public void startElement(String namespaceURI, String sName, // simple name
                             String qName, // qualified name
                             Attributes attrs) throws SAXException {
        echoText();

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }
        if ("row".equals(eName)) {
            rowNode = new RowNode(attrs);
        } else if ("c".equals(eName)) {
            rowNode.getCells().add(new CellNode(attrs));
        } else if ("v".equals(eName)) {
            // do nothing
        } else if ("f".equals(eName)) {
            // do nothing
        } else {
            printElementStart(eName, attrs);
        }
    }

    public void endElement(String namespaceURI, String sName, // simple name
                           String qName // qualified name
    ) throws SAXException {

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }

        if ("row".equals(eName)) {
            processRowNode(rowNode);
        } else if ("v".equals(eName)) {
            CellNode cell = rowNode.getCells().get(rowNode.getCells().size() - 1);
            String text = textBuffer.toString();
            if ("s".equals(cell.getAttributes().get("t"))) {
                cell.setDecodedValue(sharedStrings.get(Integer.parseInt(text)));
            } else {
                cell.setDecodedValue(text);
            }
            cell.setValue(text);
        } else if ("f".equals(eName)) {
            CellNode cell = rowNode.getCells().get(rowNode.getCells().size() - 1);
            String text = textBuffer.toString();
            cell.setFormula(text);
        } else if ("t".equals(eName)) {

            CellNode cell = rowNode.getCells().get(rowNode.getCells().size() - 1);
            String text = textBuffer.toString();
            if (text.startsWith("<![CDATA[") && text.endsWith("]]>"))
                text = text.substring("<![CDATA[".length(), text.length() - "]]>".length());

            cell.setDecodedValue(text);
            cell.setValue(text);

        } else if ("c".equals(eName)) {
            //do nothing
        } else {
            echoText();
            emit("</" + eName + ">\n");
        }

        textBuffer.setLength(0);

    }

    private void processRowNode(RowNode rowNode) throws SAXException {

        visitor.visit(rowNode, this);

    }

    public void printRowNode(RowNode rowNode) {
        rowNumber++;
        printElementStart("row", rowNode.attributes);
        List<CellNode> cells = rowNode.getCells();

        for (int i = 0; i < cells.size(); i++) {
            CellNode cell = cells.get(i);
            Map<String, Object> attributes = cell.getAttributes();
            attributes.put("!x", i);
            String cellValue = cell.getValue();
            String cellFormula = cell.getFormula();
            if (cellValue == null && cellFormula == null) {
                printEmptyElement("c", attributes);
            } else {
                printElementStart("c", attributes);

                if (cell.getFormula() != null) {
                    emit("\n<f><![CDATA[" + cell.getFormula() + "]]></f>");
                }


                if (cellValue == null) {
                    emit("\n</c>");
                } else {
                    if ("inlineStr".equals(attributes.get("t"))) {
                        emit("\n<is><t><![CDATA[" + protectSpecialCharacters(cellValue) + "]]></t></is></c>");
                    } else {
                        emit("\n<v>" + cellValue + "</v></c>");
                    }
                }

            }
        }
        emit("\n</row>");
    }

    private String protectSpecialCharacters(String originalUnprotectedString) {
        if (originalUnprotectedString == null) {
            return null;
        }
        boolean anyCharactersProtected = false;

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < originalUnprotectedString.length(); i++) {
            char ch = originalUnprotectedString.charAt(i);

            boolean controlCharacter = ch < 32;

            if (controlCharacter) {
                if (ch == '\n' || ch == '\r')
                    stringBuffer.append(ch);
                else if (ch == '\t')
                    stringBuffer.append(" ");

                anyCharactersProtected = true;
            } else {
                stringBuffer.append(ch);
            }
        }
        String res = anyCharactersProtected?stringBuffer.toString(): originalUnprotectedString;

        if (res.contains("]]>")) {
            res = StringUtils.replace(originalUnprotectedString, "]]>", "]]]]><![CDATA[>");
        }

        return res;
    }


    protected void printElementNameAndAttributes(String eName, Map<String, Object> attrs) {
        emit("\n<" + eName);
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ("r".equals(key)) {
                value = shiftR((String) value, (Integer) attrs.get("!x"));
            }
            if (!key.startsWith("!"))
                emit(" " + key + "=\"" + value + "\"");
        }
    }


    private String shiftR(String value, Integer forceXPos) {
        String x;
        int y;
        if (Character.isDigit(value.charAt(0))) {
            int row = Integer.parseInt(value);
            if (row > rowNumber)
                rowNumber = row;
            x = "";
        } else {
            x = value.replaceAll("[0-9]*", "");
        }
        if (forceXPos != null) {
            int col = ExcelUtils.toExcelColumnNumber(value.replaceAll("[0-9]*", ""));
            if (col < forceXPos)
                x = ExcelUtils.toExcelColumnName(forceXPos);
        }
        return x + rowNumber;

    }

}