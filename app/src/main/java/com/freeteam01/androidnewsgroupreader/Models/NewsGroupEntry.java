package com.freeteam01.androidnewsgroupreader.Models;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class NewsGroupEntry {

    HashMap<String, NewsGroupArticle> articles = new HashMap<>();
    private String id;
    private int articleCount = 0;
    private String name = null;
    private boolean subscribed = false;
    private NewsGroupServer server = null;

    public NewsGroupEntry(NewsGroupServer server, int articleCount, String name) {
        super();
        this.server = server;
        this.articleCount = articleCount;
        this.name = name;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    public String toString() {
        return "Name: '" + this.name + "', articleCount: '" + this.articleCount + "', selected: '" + this.subscribed + "'" + "', id: '" + this.id + "'";
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void loadArticles() throws IOException {
        NewsGroupService service = new NewsGroupService(server);
        service.Connect();
        for (NewsGroupArticle article : service.getAllArticlesFromNewsgroup(this)) {
            if (!articles.containsKey(article.getArticleID())) {
                articles.put(article.getArticleID(), article);
            }
            article.setRead(RuntimeStorage.instance().isRead(article.getArticleID()));
        }
        service.Disconnect();
    }

    public Collection<NewsGroupArticle> getArticles() {
        return articles.values();
    }

    public NewsGroupServer getServer() {
        return server;
    }

    public NewsGroupArticle getArticle(String article) {
        NewsGroupArticle newsGroupArticle = articles.get(article);
        if (newsGroupArticle == null) {
            for (HashMap.Entry<String, NewsGroupArticle> ng : articles.entrySet()) {
                newsGroupArticle = ng.getValue().getSubArticle(article);
                if(newsGroupArticle != null)
                    break;
            }
        }
        return newsGroupArticle;
    }
}