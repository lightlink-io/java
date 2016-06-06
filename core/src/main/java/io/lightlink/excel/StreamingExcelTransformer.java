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


import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class StreamingExcelTransformer {

    private List<String> sharedStrings;

    public static void main(String[] args) throws Exception {

        System.out.println("StreamingExcelTransformer.main");
        InputStream template = Thread.currentThread().getContextClassLoader().getResourceAsStream("template.xlsx");
        FileOutputStream out = new FileOutputStream("result.xlsx");

        new StreamingExcelTransformer().doExport(template, out, new ExcelStreamVisitor() {
            @Override
            public void visit(RowNode rowNode, RowPrintCallback rowPrintCallback) {

                rowNode.getCells().addAll(rowNode.getCells());

                rowPrintCallback.printRowNode(rowNode);
                rowPrintCallback.printRowNode(rowNode);
                rowPrintCallback.printRowNode(rowNode);
                rowPrintCallback.printRowNode(rowNode);

            }
        });

    }

    public void doExport(InputStream template, OutputStream out, ExcelStreamVisitor visitor) throws IOException {
        try {
            ZipInputStream zipIn = new ZipInputStream(template);
            ZipOutputStream zipOut = new ZipOutputStream(out);

            ZipEntry entry;

            Map<String, byte[]> sheets = new HashMap<String, byte[]>();

            while ((entry = zipIn.getNextEntry()) != null) {

                String name = entry.getName();


                if (name.startsWith("xl/sharedStrings.xml")) {

                    byte[] bytes = IOUtils.toByteArray(zipIn);
                    zipOut.putNextEntry(new ZipEntry(name));
                    zipOut.write(bytes);

                    sharedStrings = processSharedStrings(bytes);

                } else if (name.startsWith("xl/worksheets/sheet")) {
                    byte[] bytes = IOUtils.toByteArray(zipIn);
                    sheets.put(name, bytes);
                } else if (name.equals("xl/workbook.xml")) {
                    zipOut.putNextEntry(new ZipEntry(name));

                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    Writer writer = new OutputStreamWriter(zipOut, "UTF-8");

                    byte[] bytes = IOUtils.toByteArray(zipIn);
                    saxParser.parse(new ByteArrayInputStream(bytes), new WorkbookTemplateHandler(writer));

                    writer.flush();
//                    IOUtils.copy(zipIn, zipOut); // todo set <calcPr fullCalcOnLoad="1"/>
                } else {
                    zipOut.putNextEntry(new ZipEntry(name));
                    IOUtils.copy(zipIn, zipOut);
                }

            }

            for (Map.Entry<String, byte[]> sheetEntry : sheets.entrySet()) {
                String name = sheetEntry.getKey();

                byte[] bytes = sheetEntry.getValue();
                zipOut.putNextEntry(new ZipEntry(name));
                processSheet(bytes, zipOut, visitor);
            }

            zipIn.close();
            template.close();

            zipOut.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString(), e);
        }
    }

    private List<String> processSharedStrings(byte[] bytes) throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        SharedStringsHandler handler = new SharedStringsHandler();
        saxParser.parse(new ByteArrayInputStream(bytes), handler);

        return handler.getSharedStings();

    }

    private void processSheet(byte[] bytes, OutputStream out, ExcelStreamVisitor visitor) throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        Writer printWriter = new OutputStreamWriter(out, "UTF-8");

        saxParser.parse(new ByteArrayInputStream(bytes), new SheetTemplateHandler(printWriter, sharedStrings, visitor));

        printWriter.flush();

    }

}
