

//sql.setConnection("oracle.jdbc.OracleDriver","jdbc:oracle:thin:@127.0.0.1:1521/XE","system","password");
sql.setConnection("com.mysql.jdbc.Driver","jdbc:mysql://localhost/lightlink?","root","mysql");
sql.getConnection().setAutoCommit(false);


