package com.freeteam01.androidnewsgroupreader.Models;

public class NewsGroupEntry {

    long articleCount = 0;
    String name = null;
    boolean selected = false;

    public NewsGroupEntry(long articleCount, String name, boolean selected) {
        super();
        this.articleCount = articleCount;
        this.name = name;
        this.selected = selected;
    }

    public long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(long articleCount) {
        this.articleCount = articleCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}