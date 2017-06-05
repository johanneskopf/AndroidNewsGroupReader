package com.freeteam01.androidnewsgroupreader.ModelsDatabase;

public class ReadArticle {
    private String id;
    private String userId;
    private String articleId;

    public ReadArticle(String articleId, String userId) {
        this.articleId = articleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
}
