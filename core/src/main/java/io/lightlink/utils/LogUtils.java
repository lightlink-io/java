package io.lightlink.utils;

public class LogUtils {
    public static void error(Throwable t){
        error(t.getMessage(),t);
    }
    public static void error(String message, Throwable t){

        System.err.println(message);
        t.printStackTrace(System.err);

    }
}
