package com.freeteam01.androidnewsgroupreader.ModelsDatabase;

public class ReadArticle {
    private String id;
    private String articleId;

    public ReadArticle(String articleId) {
        this.articleId = articleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
}
