package io.lightlink.excel;

import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public void doExport(final ServletOutputStream out) throws IOException {

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

