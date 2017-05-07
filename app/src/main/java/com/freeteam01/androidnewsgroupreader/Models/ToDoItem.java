package com.freeteam01.androidnewsgroupreader.Models;

public class ToDoItem {
    private String id;
    private String text;
    private Boolean complete;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    @Override
    public String toString()
    {
        return id + "; " + text + "; " + (complete ? "complete" : "not complete");
    }
}
