package io.lightlink.dao;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
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
import io.lightlink.core.ScriptRunner;
import io.lightlink.dao.mapping.BeanMapper;
import io.lightlink.output.ObjectBufferResponseStream;
import org.apache.commons.collections.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LightLinkDAO {

    public static final Logger LOG = LoggerFactory.getLogger(LightLinkDAO.class);

    private String rootPackage, action;
    private ScriptRunner scriptRunner;
    private ServletContext servletContext;

    public LightLinkDAO(String rootPackage, String action, ServletContext servletContext) {
        this.servletContext = servletContext;
        this.rootPackage = rootPackage;
        this.action = action;
        scriptRunner = new ScriptRunner(rootPackage,servletContext);
    }

    protected LightLinkDAO(String rootPackage, String action) {
        this.rootPackage = rootPackage;
        this.action = action;
        scriptRunner = new ScriptRunner(rootPackage);
    }

    public static LightLinkDAO getInstance(ServletContext servletContext,String rootPackage,String action){
        return new LightLinkDAO(rootPackage, action);
    }

    public static LightLinkDAO getInstance(String rootPackage,String action){
        return new LightLinkDAO(rootPackage, action);
    }

    public static LightLinkDAO getInstance(String action){
        return new LightLinkDAO(ConfigManager.DEFAULT_ROOT_PACKAGE, action);
    }

    public Map<String, Object> execute(Object params) {
        try {
            return  doExecute(params);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public <T> T execute(Object params, Class<T> aClass) {
        try {
            Map<String, Object> data = doExecute(params);

            return (T) new BeanMapper(aClass, getFieldNames(data)).convertObject(data, false);

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    private List<String> getFieldNames(Map<String, Object> data) {
        return new ArrayList<String>(data.keySet());
    }

    private List<String> getFieldNames(List<Map<String, Object>> data) {
        if (data.size()>0)
            return getFieldNames(data.get(0));
        else
            return Collections.emptyList();
    }

    public List<Map<String, Object>> queryForList(Object params) {
        try {
            Map<String, Object> data = doExecute(params);

            return (List<Map<String, Object>>) data.get("resultSet");

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public <T> List<T> queryForList(Object params, Class<T> aClass) {
        try {
            Map<String, Object> data = doExecute(params);

            List<Map<String, Object>> resultSet = (List<Map<String,Object>>) data.get("resultSet");
            if (resultSet != null) {
                return (List<T>) new BeanMapper(aClass, getFieldNames(resultSet)).convert( resultSet);
            } else
                return null;

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public <T> T queryForSingleRow(Object params, Class<T> aClass) {
        try {
            Map<String, Object> data = doExecute(params);

            List<Map<String, Object>> resultSet = (List<Map<String,Object>>) data.get("resultSet");
            if (resultSet != null) {
                return ((List<T>) new BeanMapper(aClass,getFieldNames(resultSet)).convert( resultSet)).get(0);
            } else
                throw new IndexOutOfBoundsException("No resultSet returned");

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public Map<String, Object> queryForSingleRow(Object params) {
        try {
            Map<String, Object> data = doExecute(params);

            Object resultSet = data.get("resultSet");
            if (resultSet != null) {
                return ((List<Map<String, Object>>) data.get("resultSet")).get(0);
            } else
                throw new IndexOutOfBoundsException("No resultSet returned");

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
    }


    protected Map<String, Object> doExecute(Object params) throws IOException {
        Map<String, Object> paramsMap;
        if (params instanceof Map)
            paramsMap = (Map<String, Object>) params;
        else
            paramsMap = new BeanMap(params);
        ObjectBufferResponseStream responseStream = new ObjectBufferResponseStream();
        scriptRunner.execute(action, "",  paramsMap, responseStream);
        return  responseStream.getDataMap();

    }

}
