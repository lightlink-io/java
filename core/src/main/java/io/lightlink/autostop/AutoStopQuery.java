package io.lightlink.autostop;

import io.lightlink.core.RunnerContext;
import io.lightlink.output.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class AutoStopQuery {

    public static final Logger LOG = LoggerFactory.getLogger(AutoStopQuery.class);

    public static final int PING_INTERVAL = 300;
    private Timer timer = new Timer("Timer of " + AutoStopQuery.class.getName());

    class Record {
        private WeakReference<RunnerContext> runnerContext;
        private WeakReference<Statement> statement;
        private boolean active;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        Record(RunnerContext runnerContext, Statement statement) {
            this.runnerContext = new WeakReference<RunnerContext>(runnerContext);
            this.statement = new WeakReference<Statement>(statement);
        }

        public RunnerContext getRunnerContext() {
            return runnerContext.get();
        }

        public Statement getStatement() {
            return statement.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Record record = (Record) o;
            return statement.get() == record.statement.get();
        }

        @Override
        public int hashCode() {
            return statement.get() != null ? System.identityHashCode(statement.get()) : 0;
        }
    }

    private Set<Record> records = new HashSet<Record>();

    private static AutoStopQuery ourInstance = new AutoStopQuery();

    public static AutoStopQuery getInstance() {
        return ourInstance;
    }

    private AutoStopQuery() {
    }

    public synchronized void register(RunnerContext runnerContext, PreparedStatement ps) {
        final Record record = new Record(runnerContext, ps);
        records.add(record);
//        todo : better check if finished
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    RunnerContext context = record.getRunnerContext();
//                    if (context != null) {
//                        ResponseStream stream = context.getResponseStream();
//                        boolean alive = stream.checkConnectionAlive();
//                        if (!alive) {
//                            cancelQuery(record);
//                        }
//                    } else {
//                        unregister(record);
//                    }
//                } catch (Throwable e) {
//                    LOG.error(e.toString(),e);
//                }
//            }
//        }, PING_INTERVAL);
    }

    public void cancelQuery(Record record) {
        Statement statement = record.getStatement();
        if (statement != null )
            try {
                unregister(record);
                statement.cancel();
            } catch (SQLException e) {
                LOG.debug(e.toString(), e); // low importance
            }
    }
    
    public int cancelAllForWindow(String csrfToken){
        int counter =0;
        Record[] array = records.toArray(new Record[0]);// prevents ConcurrentModificationException
        for (Record record : array) {
            RunnerContext runnerContext = record.getRunnerContext();
            if (runnerContext != null && runnerContext.getCsrfToken() != null && runnerContext.getCsrfToken().equals(csrfToken)) {
                cancelQuery(record);
                counter++;
            }
        }
        return counter;
    }

    private synchronized void unregister(Record record) {
        records.remove(record);
    }

    public synchronized void unregister(RunnerContext runnerContext, PreparedStatement ps) {
        records.remove(new Record(runnerContext, ps));
    }

}
