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
    private String outFileName;
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
    public void end() {
        super.end();

        HttpServletResponse response = env.getResponse();
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String templateLowerCase = templatePath.toLowerCase().trim();
        if (templateLowerCase.endsWith(".xlam")) {
            contentType = "application/vnd.ms-excel.addin.macroEnabled.12";
        } else if (templateLowerCase.endsWith(".xlsm")) {
            contentType = "application/vnd.ms-excel.sheet.macroEnabled.12";
        } else if (templateLowerCase.endsWith(".xltm")) {
            contentType = "application/vnd.ms-excel.template.macroEnabled.12";
        } else if (templateLowerCase.endsWith(".xltx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
        } else if (templateLowerCase.endsWith(".xlsb")) {
            contentType = "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + outFileName + "\"");

        URL url = Utils.getUrl(ConfigManager.DEFAULT_ROOT_PACKAGE + "/" + templatePath, env.getRequest().getServletContext());
        Map<String,Object> data = (Map<String, Object>) this.getData();


        if (!"true".equals(""+data.get("success"))){
            LOG.error(""+data.get("error"));
            LOG.error(""+data.get("stackTrace"));
        }

        if (ConfigManager.isInDebugMode()){
            try {
                String debugOutput = url.getFile().replaceAll(".xls.$", ".debug.json");
                FileOutputStream fos = new FileOutputStream(debugOutput);
                fos.write(("//@ sourceURL=" + debugOutput + "\n").getBytes("UTF-8"));
                JSONResponseStream responseStream = new JSONResponseStream(fos, null);
                Map<String, Object> dataMap = getDataMap();
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    responseStream.writeProperty(entry.getKey(), entry.getValue());
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
