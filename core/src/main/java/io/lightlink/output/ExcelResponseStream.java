package io.lightlink.output;


import io.lightlink.config.ConfigManager;
import io.lightlink.excel.StreamingExcelExporter;
import io.lightlink.facades.ServletEnv;
import io.lightlink.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class ExcelResponseStream extends ObjectBufferResponseStream {

    public static final Logger LOG = LoggerFactory.getLogger(ExcelResponseStream.class);

    private ServletEnv env;
    private String outFileName = "excelExport.xlsx";
    private String templatePath;

    public ExcelResponseStream(String outFileName, String templatePath, ServletEnv env) {
        this.outFileName = outFileName;
        this.templatePath = templatePath;
        this.env = env;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    @Override
    public void end() throws IOException {
        super.end();

        HttpServletResponse response = env.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "inline; filename=\"" + outFileName + "\"");

        URL url = Utils.getUrl(ConfigManager.DEFAULT_ROOT_PACKAGE+"/"+templatePath, env.getRequest().getServletContext());
        StreamingExcelExporter exporter = new StreamingExcelExporter(url, this.getData());

        try {
            exporter.doExport(response.getOutputStream());
        } catch (Throwable e) {
            LOG.error(e.toString(), e);
        }


    }
}
