package com.ft.platform.dropwizard;

public class GTGConfig {
    private String okBody = "OK";
    private String contentType = "text/plain";

    public GTGConfig() {
    }

    public GTGConfig(String okBody, String contentType) {
        this.okBody = okBody;
        this.contentType = contentType;
    }

    public String getOkBody() {
        return okBody;
    }

    public void setOkBody(String okBody) {
        this.okBody = okBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
