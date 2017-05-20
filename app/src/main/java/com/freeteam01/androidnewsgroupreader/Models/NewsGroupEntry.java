package com.freeteam01.androidnewsgroupreader.Models;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class NewsGroupEntry {

    HashMap<String, NewsGroupArticle> articles_ = new HashMap<>();
    private String id;
    private int articleCount = 0;
    private String name = null;
    private boolean selected = false;
    private boolean subscribed_ = false;
    private NewsGroupServer parent_ = null;

    public NewsGroupEntry(NewsGroupServer server, int articleCount, String name, boolean selected) {
        super();
        this.parent_ = server;
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
        return "Name: '" + this.name + "', articleCount: '" + this.articleCount + "', selected: '" + this.selected + "'" + "', id: '" + this.id + "'";
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed_ = subscribed;
    }

    public void loadArticles() throws IOException {
        NewsGroupService service = new NewsGroupService(parent_);
        service.Connect();
        for (NewsGroupArticle article : service.getAllArticlesFromNewsgroup(getName())) {
            if (!articles_.containsKey(article.getArticleID())) {
                articles_.put(article.getArticleID(), article);
            } else {
                //TODO Update logic
            }
        }
        service.Disconnect();
    }

    public Collection<NewsGroupArticle> getArticles() {
        return articles_.values();
    }
}