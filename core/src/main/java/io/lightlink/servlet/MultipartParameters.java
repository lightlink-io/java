package io.lightlink.servlet;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MultipartParameters {
    HttpServletRequest req;
    private FileItemIterator fileItemIterator;
    private FileItemStream fileItemStream;

    private HashMap<String, Object> paramsMap;
    boolean parametersParsed;

    public MultipartParameters(HttpServletRequest req) {
        this.req = req;
    }

    public HashMap<String, Object> getInitialParams() throws IOException, FileUploadException {
        paramsMap = new HashMap<String, Object>();

        ServletFileUpload upload = new ServletFileUpload();

        fileItemIterator = upload.getItemIterator(req);

        parseUpToNextStream();
        parametersParsed = true;

        return paramsMap;
    }

    public boolean hasNextStream() throws IOException, FileUploadException {
        if (!parametersParsed){
            parseUpToNextStream();
            parametersParsed =true;
        }
        return fileItemStream != null;
    }

    public FileItemStream getNextStream() throws IOException, FileUploadException {
        fileItemStream = null;
        if (!parametersParsed)
            parseUpToNextStream();

        parametersParsed = false;   // a stream is returned, so the following parsing is required;

        return fileItemStream;
    }


    private void parseUpToNextStream() throws FileUploadException, IOException {
        while (fileItemIterator.hasNext()) {
            FileItemStream fItemStream = fileItemIterator.next();
            String name = fItemStream.getFieldName();
            if (fItemStream.isFormField()) {
                paramsMap.put(name, Streams.asString(fItemStream.openStream()));
            } else {
                this.fileItemStream = fItemStream;
                break; // returns and waits while the the stream is consumed
            }
        }
    }

    public HashMap<String, Object> getParamsMap() {
        return paramsMap;
    }


}
