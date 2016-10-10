package io.lightlink.facades;

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


import io.lightlink.core.RunnerContext;
import io.lightlink.output.ObjectBufferResponseStream;
import io.lightlink.output.ResponseStream;
import io.lightlink.sql.SQLHandler;
import jdk.nashorn.api.scripting.JSObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLFacade {

    private int fetchSize = 100;
    private Integer maxRows = null;
    private Integer queryTimeout = null;

    boolean autoCommit = true;

    DataSource dataSource;
    Connection connection;

    RunnerContext runnerContext;

    private SQLHandler sqlHandler = new SQLHandler(this);

    private List<Integer> updateCount;

    private List<SQLFacade> childInstances = new ArrayList<SQLFacade>();

    public SQLFacade(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public SQLFacade newInstance() throws SQLException {
        SQLFacade newFacade = new SQLFacade(runnerContext);
        newFacade.setAutoCommit(autoCommit);
        childInstances.add(newFacade);
        return newFacade;
    }

    public boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if (connection!=null)
            connection.setAutoCommit(autoCommit);
    }

    public void queryForValue(String propertyName) throws SQLException, IOException, ScriptException {
        ResponseStream resp = runnerContext.getResponseStream();
        List<Map<String,Object>> res = (List<Map<String,Object>>) queryToBuffer();
        if (res.size()>0){
            Map<String, Object> line = res.get(0);
            if (line.size()>0){
                resp.writeProperty(propertyName,line.entrySet().iterator().next().getValue());
            }
        }
    }

    public Object queryToBuffer() throws SQLException, IOException, ScriptException {
        ResponseStream respBackup = runnerContext.getResponseStream();
        try {
            ObjectBufferResponseStream buffer = new ObjectBufferResponseStream();

            runnerContext.setResponseStream(buffer);

            runnerContext.getUsedResultSetNames().remove("buffer");
            query("buffer");

            Map<String,Object> res = buffer.getDataMap();
            return res.get("buffer");

        } finally {
            runnerContext.setResponseStream(respBackup);
        }
    }


    public void query() throws SQLException, IOException, ScriptException {
        query(null);
    }

    public void query(String resultSetName) throws SQLException, IOException, ScriptException {
        query(resultSetName, null);
    }

    public void query(String resultSetName, JSObject rowHandler) throws SQLException, IOException, ScriptException {
        query(false, resultSetName, rowHandler);
    }

    public void addBatch() throws SQLException, IOException, ScriptException {
        addBatch(null);
    }

    public void addBatch(String resultSetName) throws SQLException, IOException, ScriptException {
        addBatch(resultSetName, null);
    }

    public void addBatch(String resultSetName, JSObject rowHandler) throws SQLException, IOException, ScriptException {
        query(true, resultSetName, rowHandler);
    }

    public void query(boolean addBatch, String resultSetName, JSObject rowHandler) throws SQLException, IOException, ScriptException {

        String sql = runnerContext.getAndClearSQLBuffer();

        sqlHandler.query(addBatch, resultSetName, runnerContext, sql, rowHandler);


        runnerContext.clearParams();
    }


    public Connection getConnection() throws SQLException {
        if (connection != null)
            return connection;
        else {
            if (dataSource != null) {
                return connection = dataSource.getConnection();
            } else {
                throw new IllegalArgumentException("Neither Connection nor DataSourceJndi not provided. " +
                        "See config.js, sql.setJDBCConnection() or sql.setDataSourceJndi() ");
            }
        }

    }


    public void setConnection(Connection connection) throws SQLException {
        if (connection != null) {
            releaseConnection();
        }
        this.connection = connection;
    }

    public void setConnection(String className, String url, String login, String password) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Class.forName(className).newInstance();
        Connection connection = DriverManager.getConnection(url, login, password);
        setConnection(connection);
    }

    public void setDataSourceJndi(String jndi) throws NamingException, SQLException {
        if (connection != null) {
            releaseConnection();
        }
        InitialContext initialContext = new InitialContext();
        dataSource = (DataSource) initialContext.lookup(jndi);
        if (dataSource == null) {
            throw new RuntimeException("DataSource " + jndi + " font found in InitialContext");
        }

    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public Integer getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public Integer getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(Integer queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void releaseConnection() {
        try {
            if (connection != null)
                connection.close();
            connection = null;

        } catch (SQLException e) { /* ignore */ }

        // close child instances connections
        for (SQLFacade childInstance : childInstances) {
            childInstance.releaseConnection();
        }
    }

    public void setUpdateCount(List<Integer> updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * @param n - the number of statement if multiple updates were performed (inside SP for some databases, for example)
     * @return the number of updates, -1 if n is higher then the number of updates
     */
    public int getUpdateCount(int n) {
        if (updateCount != null && n < updateCount.size())
            return updateCount.get(n);
        else
            return -1;
    }

    public int getUpdateCount() {
        return getUpdateCount(0);
    }

    public void commit() {
        try {
            if (connection != null && !connection.getAutoCommit())
                connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot commit : " + e.toString(), e);
        }

        // commit  child instances connections
        for (SQLFacade childInstance : childInstances) {
            childInstance.commit();
        }
    }

    public void rollback() {
        try {
            if (connection != null && !connection.getAutoCommit())
                connection.rollback();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot rollback : " + e.toString(), e);
        }

        // rollback child instances connections
        for (SQLFacade childInstance : childInstances) {
            childInstance.rollback();
        }
    }
}
