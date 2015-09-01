$SQL="";
function $appendSQL(sql){
    for (var i = 1; i < arguments.length; i=i+2) {
        var name = arguments[i];
        var nameOri = name;
        var value = arguments[i+1];
        var no=1;
        while (typeof $P["__"+name] !="undefined"){
            name = nameOri+(no++);
        }

        $P["__"+name] = value;

        if (nameOri!=name){
            var pos = -1;
            while ((pos=sql.indexOf(":"+nameOri, pos+1))!=-1){
                if (sql.length<=pos+1+nameOri.length
                    || !sql.charAt(pos+1+nameOri.length).match(/\d/) //not an already bind field
                )
                    sql = sql.substring(0,pos+1)+name+sql.substring(pos+1+nameOri.length);
            }
        }

    }
        $SQL+=sql;
}
$P = {};
