package com.freeteam01.androidnewsgroupreader.ModelsDatabase;

public class Server {
    private String id;
    private String name;
    private String url;
    private String userId;

    public Server(String name, String url, String userId) {
        this.name = name;
        this.url = url;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
