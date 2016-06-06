package io.lightlink.excel;

import io.lightlink.config.ConfigManager;
import io.lightlink.facades.ServletEnv;
import io.lightlink.output.JSONResponseStream;
import io.lightlink.output.ObjectBufferResponseStream;
import io.lightlink.utils.LogUtils;
import io.lightlink.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

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
    public void end()  {
        super.end();

        HttpServletResponse response = env.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "inline; filename=\"" + outFileName + "\"");

        URL url = Utils.getUrl(ConfigManager.DEFAULT_ROOT_PACKAGE + "/" + templatePath, env.getRequest().getServletContext());
        Object data = this.getData();

        if (ConfigManager.isInDebugMode()){
            try {
                String debugOutput = url.getFile().replaceAll(".xlsx$", ".debug.json");
                FileOutputStream fos = new FileOutputStream(debugOutput);
                fos.write(("//@ sourceURL="+debugOutput+"\n").getBytes("UTF-8"));
                JSONResponseStream responseStream = new JSONResponseStream(fos);
                Map<String, Object> dataMap = getDataMap();
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    responseStream.writeProperty(entry.getKey(),entry.getValue());
                }
                responseStream.end();
                fos.close();
            } catch (IOException e) {
                LogUtils.warn(getClass(), e);
            }

        }

        StreamingExcelExporter exporter = new StreamingExcelExporter(url, data);

        try {
            exporter.doExport(response.getOutputStream());
        } catch (Throwable e) {
            LOG.error(e.toString(), e);
        }


    }
}
