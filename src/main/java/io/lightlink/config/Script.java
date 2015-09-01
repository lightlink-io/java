package io.lightlink.config;

import java.net.URL;

public class Script {
    private String name,content;
    private URL url;

    public Script(String name, URL url, String content) {
        this.name = name;
        this.content = content;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public URL getUrl() {
        return url;
    }
}
