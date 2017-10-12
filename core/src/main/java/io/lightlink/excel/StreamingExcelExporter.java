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


import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class StreamingExcelExporter {

    public static final Logger LOG = LoggerFactory.getLogger(StreamingExcelExporter.class);

    private Map data = Collections.EMPTY_MAP;
    private URL template;

    public StreamingExcelExporter(URL template, Object data) {
        this.data = data instanceof Map?(Map) data:new BeanMap(data);
        this.template = template;
    }

    public URL getTemplate() {
        return template;
    }

    public void setTemplate(URL template) {
        this.template = template;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    public void doExport(OutputStream out) throws IOException {

        InputStream templateStream = template.openStream();
        ExcelStreamVisitor visitor = new WritingExcelStreamVisitor(getData(), getExportDateTimeFormat());
        new StreamingExcelTransformer().doExport(templateStream, out, visitor);

    }

    /**
     * Override to redefine datetime format (second line of the exported document). Default value is "dd/MM/yyyy HH:mm"
     *
     * @return
     */
    protected DateFormat getExportDateTimeFormat() {
        String dateFormat = (String) data.get("dateFormat"); // todo getDate from typesFacade
        if (dateFormat == null) {
            return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        } else
            return new SimpleDateFormat(dateFormat);
    }

}

