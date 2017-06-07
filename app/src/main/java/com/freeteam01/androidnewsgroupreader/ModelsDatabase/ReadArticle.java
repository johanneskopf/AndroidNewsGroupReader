package com.freeteam01.androidnewsgroupreader.ModelsDatabase;

public class ReadArticle {
    private String id;
    private String userId;
    private String articleId;

    public ReadArticle(String articleId, String userId) {
        this.articleId = articleId;
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadArticle that = (ReadArticle) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        return articleId != null ? articleId.equals(that.articleId) : that.articleId == null;
    }
}
