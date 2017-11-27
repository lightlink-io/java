

sql.setConnection("oracle.jdbc.OracleDriver","jdbc:oracle:thin:@127.0.0.1:1521/XE","HR","lightlink");
sql.getConnection().setAutoCommit(false);


// todo : register custom types