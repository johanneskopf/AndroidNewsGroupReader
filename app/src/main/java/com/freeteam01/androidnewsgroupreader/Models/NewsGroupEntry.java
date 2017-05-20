package com.freeteam01.androidnewsgroupreader.Models;

import java.util.Objects;

public class NewsGroupEntry {

    private String id;
    private int articleCount = 0;
    private String name = null;
    private boolean selected = false;

    public NewsGroupEntry(int articleCount, String name, boolean selected) {
        super();
        this.articleCount = articleCount;
        this.name = name;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    public String toString() {
        return "Name: '" + this.name + "', articleCount: '" + this.articleCount + "', selected: '" + this.selected + "'"+ "', id: '" + this.id + "'";
    }
}