package com.example.pd4thapp;

public class AllRSSFeed {
    private String title, xmlUrl;

    public AllRSSFeed(String title, String xmlUrl) {
        this.title = title;
        this.xmlUrl = xmlUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }
}
