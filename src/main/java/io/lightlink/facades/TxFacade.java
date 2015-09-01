package io.lightlink.facades;

import io.lightlink.core.RunnerContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;

public class TxFacade {

    private RunnerContext runnerContext;
    private TransactionManager tm;
    private boolean rollbackOnly;

    public TxFacade(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public void useTxJee(String jndi) throws NamingException, NotSupportedException {
        InitialContext context = new InitialContext();
        try {
            tm = (TransactionManager) context.lookup(jndi);
        } catch (NamingException e) {/*let's try another name*/ }
        if (tm == null) {
            throw new IllegalArgumentException("Called useYxJee('" + jndi + "') but no TransactionManager found under this name ");
        }

        try {
            tm.begin();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(),e);
        }
    }

    public void useTxJee() throws NamingException, NotSupportedException {
        InitialContext context = new InitialContext();
        try {
            tm = (TransactionManager) context.lookup("java:/comp/TransactionManager");
        } catch (NamingException e) {/*let's try another name*/ }
        if (tm == null) {
            try {
                tm = (TransactionManager) context.lookup("java:/TransactionManager");
            } catch (NamingException e) { /*ok, no TransactionManager in jndi */ }
        }
        if (tm == null) {
            throw new IllegalArgumentException("No TransactionManager in JNDI. Tried both java:/TransactionManager and java:/comp/TransactionManager");
        }

        try {
            tm.begin();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(),e);
        }
    }

    public void setTxTimeout(int timeout)  {
        if (tm != null)
            try {
                tm.setTransactionTimeout(timeout);
            } catch (Exception e) {
                throw new RuntimeException(e.toString(),e);
            }
    }

    public void setTxRollbackOnly()   {
        if (tm != null)
            try {
                tm.setRollbackOnly();
            } catch (Exception e) {
                throw new RuntimeException(e.toString(),e);
            }
        else
            rollbackOnly = true;
    }

    public void success() throws HeuristicRollbackException, RollbackException, HeuristicMixedException {
        if (tm != null){
            try {
                tm.commit();
            } catch (Exception e) {
                throw new RuntimeException(e.toString(), e);
            }
        }else {
            if (rollbackOnly)
                runnerContext.getRootSQLFacade().rollback();
            else
                runnerContext.getRootSQLFacade().commit();
        }
    }

    public void failure() {
        if (tm != null) {
            try {
                tm.rollback();
            } catch (Exception e) {
                throw new RuntimeException(e.toString(), e);
            }
        } else {
            runnerContext.getRootSQLFacade().rollback();
        }
    }
}
