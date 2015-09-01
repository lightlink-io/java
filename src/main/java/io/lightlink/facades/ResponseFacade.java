package io.lightlink.facades;

import io.lightlink.core.RunnerContext;
import io.lightlink.output.ResponseStream;

import java.io.IOException;


public class ResponseFacade {

    RunnerContext runnerContext;

    public ResponseFacade(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public void writeObject(String property, Object data) throws IOException {
        runnerContext.getResponseStream().writeProperty(property, data);
    }

    public void setFormat(ResponseStream responseStream){
        runnerContext.setResponseStream(responseStream);
    }

    public ResponseStream getFormat(){
        return runnerContext.getResponseStream();
    }



}
