package io.lightlink.excel;

import io.lightlink.excel.reader.DataReadingExcelStreamVisitor;
import io.lightlink.excel.reader.DefinitionReadingExcelStreamVisitor;
import io.lightlink.excel.reader.MappingBean;
import junit.framework.TestCase;
import org.apache.commons.io.output.NullOutputStream;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelNamedColumnTest extends TestCase {



    public void test() throws IOException {

        int ROWS_CNT = 100;

        InputStream templateStream = Thread.currentThread().getContextClassLoader().getResource("io/lightlink/excel/ExcelNamedColumnExport.xlsx").openStream();

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

        List<Map<String, Object>> resultSet = new ArrayList<>();
        for (int i = 0; i < ROWS_CNT; i++) {
            HashMap<String, Object> line = new HashMap<String, Object>();

            line.put("id",i);
            line.put("name","Name 1");
            line.put("date",new Date());
            line.put("integer",i+System.currentTimeMillis());
            line.put("float",1F/(i+1));
            line.put("link","http://google.com/?q=test"+i);

            resultSet.add(line);
        }

        data.put("resultSet", resultSet);


        String fName = "ExcelNamedColumnExport-OUT.xlsx";
        OutputStream outFile = new FileOutputStream(fName);

        new StreamingExcelTransformer().doExport(templateStream, outFile,
                new WritingExcelStreamVisitor(data, new SimpleDateFormat("dd/MM/yyyy")));

        outFile.close();


    }

}



