package io.lightlink.excel;

import io.lightlink.excel.reader.DataReadingExcelStreamVisitor;
import io.lightlink.excel.reader.DefinitionReadingExcelStreamVisitor;
import io.lightlink.excel.reader.MappingBean;
import junit.framework.TestCase;
import org.apache.commons.io.output.NullOutputStream;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class ExcelImportExportTest extends TestCase {


    public void test() throws IOException {


        int COLUMNS_CNT = 30;
        int ROWS_CNT = 10000;

        List<String> headers = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        for (int i = 0; i < COLUMNS_CNT; i++) {
            columns.add("col_" + i);
            headers.add("Name " + i);
        }


        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream templateStream = cl.getResource("io/lightlink/excel/ExcelImportExportTest.xlsx").openStream();
        InputStream templateStream2 = cl.getResource("io/lightlink/excel/ExcelImportExportTest.xlsx").openStream();

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> p = new HashMap<>();
        p.put("firstName", "My firstName");
        p.put("lastName", "My lastName");
        p.put("minHireDate", new Date(0).toString());
        p.put("maxHireDate", new Date().toString());
        p.put("minSalary", "1200");
        p.put("maxSalary", "5000");
        data.put("p", p);

        data.put("date", new Date().toString());
        data.put("headers", headers);
        data.put("columns", columns);

        List<Map<String, Object>> resultSet = new ArrayList<>();
        for (int i = 0; i < ROWS_CNT; i++) {
            HashMap<String, Object> line = new HashMap<String, Object>();
            for (String column : columns) {
                line.put(column, column + "_" + i);
            }
            resultSet.add(line);
            if (i % 1000 == 0) {
                System.out.println(i + " : " + Runtime.getRuntime().totalMemory() / 1000000 + "Mb used");
            }
        }

        data.put("resultSet", resultSet);


        String fName = "ExcelImportExportTest-OUT.xlsx";
        OutputStream outFile = new FileOutputStream(fName);

        long l = System.currentTimeMillis();

        new StreamingExcelTransformer().doExport(templateStream, outFile,
                new WritingExcelStreamVisitor(data, DateFormat.getDateTimeInstance()));

        outFile.close();

//        System.out.println(COLUMNS_CNT + "x" + ROWS_CNT + " generated in " + (System.currentTimeMillis() - l) / 1000f + "sec");

//        resultSet = null;
//        data = null;

//        for debug
        InputStream fileToRead = new FileInputStream(fName);

        HashMap<String, MappingBean> outData = new HashMap<String, MappingBean>();

        // read definition
        DefinitionReadingExcelStreamVisitor definitionReadingExcelStreamVisitor = new DefinitionReadingExcelStreamVisitor();
        new StreamingExcelTransformer().doExport(templateStream2, new NullOutputStream(), definitionReadingExcelStreamVisitor);
        Map<String, MappingBean> targets = definitionReadingExcelStreamVisitor.getTargets();

        // read data
        DataReadingExcelStreamVisitor dataReadingExcelStreamVisitor = new DataReadingExcelStreamVisitor(targets);
        new StreamingExcelTransformer().doExport(fileToRead, new NullOutputStream(), dataReadingExcelStreamVisitor);

        Map<String, Object> loadedData = dataReadingExcelStreamVisitor.getData();

        compareAssertMap("", data, loadedData);

        fileToRead.close();
        new File(fName).delete();

    }

    private void compareAssertMap(String position, HashMap<String, Object> data, Map<String, Object> loadedData) {
        assertEquals("Sizez must be equal", data.size(), loadedData.size());

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object expected = entry.getValue();
            Object found = loadedData.get(entry.getKey());
            compareAssertObject(position + "." + entry.getKey(), expected, found);
        }
    }

    private void compareAssertObject(String newPosition, Object expected, Object found) {
        if (expected instanceof Map) {
            if (found instanceof List)
                compareAssertObject(newPosition, new ArrayList(((Map) expected).values()), found);
            else
                compareAssertMap(newPosition,
                        (HashMap<String, Object>) expected,
                        (Map<String, Object>) found);
        } else if (expected instanceof List) {
            List expectedList = (List) expected;
            List foundList = (List) found;
            assertEquals(newPosition + ": sizez must be equal", expectedList.size(), foundList.size());

            for (int i = 0; i < expectedList.size(); i++) {
                Object o = expectedList.get(i);
                Object o2 = foundList.get(i);
                compareAssertObject(newPosition, o, o2);
            }
        } else {
            assertEquals(newPosition, expected, found);
        }
    }
}



