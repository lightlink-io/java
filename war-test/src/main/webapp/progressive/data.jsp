<%

    for (int i=0;i<3000;i++){


        out.write(""+Math.random());
        out.write("\n");

        if (i%100==99)
            response.flushBuffer();

        Thread.sleep(1);
    }

%>