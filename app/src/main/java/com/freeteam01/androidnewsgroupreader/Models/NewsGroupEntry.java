package com.freeteam01.androidnewsgroupreader.Models;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class NewsGroupEntry {

    HashMap<String, NewsGroupArticle> articles_ = new HashMap<>();
    private String id;
    private int articleCount = 0;
    private String name = null;
    private boolean subscribed_ = false;
    private NewsGroupServer server_ = null;

    public NewsGroupEntry(NewsGroupServer server, int articleCount, String name) {
        super();
        this.server_ = server;
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
        return subscribed_;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    public String toString() {
        return "Name: '" + this.name + "', articleCount: '" + this.articleCount + "', selected: '" + this.subscribed_+ "'" + "', id: '" + this.id + "'";
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed_ = subscribed;
    }

    public void loadArticles() throws IOException {
        NewsGroupService service = new NewsGroupService(server_);
        service.Connect();
        for (NewsGroupArticle article : service.getAllArticlesFromNewsgroup(this)) {
            if (!articles_.containsKey(article.getArticleID())) {
                articles_.put(article.getArticleID(), article);
            } else {
                //TODO Update logic
            }

            article.setRead(RuntimeStorage.instance().isRead(article.getArticleID()));
        }
        service.Disconnect();
    }

    public Collection<NewsGroupArticle> getArticles() {
        return articles_.values();
    }

    public NewsGroupServer getServer() {
        return server_;
    }

    public NewsGroupArticle getArticle(String article) {
        NewsGroupArticle newsGroupArticle = articles_.get(article);
        if (newsGroupArticle == null) {
            for (HashMap.Entry<String, NewsGroupArticle> ng : articles_.entrySet()) {
                newsGroupArticle = ng.getValue().getSubArticel(article);
                if(newsGroupArticle != null)
                    break;
//                if (ng.getValue().getSubArticel(articel).getChildren().containsKey(article)) {
//                    newsGroupArticle = ng.getValue().getChildren().get(article);
//                    break;
//                }
            }
        }
        return newsGroupArticle;
    }
}