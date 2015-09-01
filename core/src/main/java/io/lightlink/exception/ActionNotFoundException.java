package io.lightlink.exception;

public class ActionNotFoundException extends RuntimeException {

    public ActionNotFoundException(String actionName) {
        super(actionName);
    }

}
