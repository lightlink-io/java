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


import junit.framework.TestCase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelNamedColumnTest extends TestCase {



    public void test() throws IOException {

        int ROWS_CNT = 100;

        InputStream templateStream = Thread.currentThread().getContextClassLoader().getResource("io/lightlink/excel/ExcelNamedColumnExport.xlsx").openStream();

        HashMap<String, Object> data = new HashMap<String, Object>();
        HashMap<String, Object> p = new HashMap<String, Object>();
        p.put("firstName", "My firstName");
        p.put("lastName", "My lastName");
        p.put("minHireDate", new Date(0).toString());
        p.put("maxHireDate", new Date().toString());
        p.put("minSalary", "1200");
        p.put("maxSalary", "5000");
        data.put("p", p);

        data.put("date", new Date().toString());

        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
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



