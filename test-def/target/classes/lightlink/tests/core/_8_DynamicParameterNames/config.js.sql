

--%  function equalsOrLike(field, value){
--%      if (value) {
--%        if (value.indexOf("*")!=-1) {
--%            value = value.replace(/\*/g,"%")  // replace '*' with '%' for like expression
            AND <%=field %> LIKE :value
--%        } else {   // if parameter without * the use strict equals for indexed search
            AND <%=field %> = :value
--%        }
--%      }
--%  }

