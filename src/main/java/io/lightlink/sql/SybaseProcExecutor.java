//package io.lightlink.sql;
//
//import io.lightlink.sql.dbridge.config.ArgDescr;
//import io.lightlink.sql.dbridge.config.ProcDesr;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SybaseProcExecutor extends AbstractProcExecutor {
//
//
//    @Override
//    protected String prepareSQL(ProcDesr proc) {
//        StringBuffer sql = new StringBuffer("{ call ").append(proc.getName() ).append( " ");
//
//        for (ArgDescr argDesc : proc.getArgs()) {
//
//            sql.append("?");
//
//            sql.append(",");
//        }
//
//        sql.setLength(sql.length() - 1);
//        sql.append("}");
//
//        return sql.toString();
//    }
//
//
//
//
//}
