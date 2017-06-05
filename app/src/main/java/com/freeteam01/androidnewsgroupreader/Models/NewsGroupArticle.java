package com.freeteam01.androidnewsgroupreader.Models;

import com.freeteam01.androidnewsgroupreader.BuildConfig;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsGroupArticle {
    private String id;
    private String articleID;
    private String subject;
    private String date;
    private String from;
    private NewsGroupEntry group;
    private String text;
    private boolean isRead;
    private transient List<String> references = new ArrayList<>();
    private transient HashMap<String, NewsGroupArticle> children = new HashMap<>();

    public NewsGroupArticle(NewsGroupEntry group, String articleId, String subject, String date, String from) {
        this.articleID = articleId;
        this.group = group;
        this.subject = subject;
        this.date = date;
        this.from = from;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public String getArticleID() {
        return articleID;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectString() {
        if (subject.startsWith("=?UTF-8?Q?")) {
            String subject_cut = subject.replace("=?UTF-8?Q?", "");
            subject_cut = subject_cut.replace("?=", "");
            ByteArrayOutputStream subject_bytes = new ByteArrayOutputStream();
            for (int i = 0; i < subject_cut.length(); i++) {
                char c = subject_cut.charAt(i);
                if (c == '=') {
                    String first_value = String.valueOf(subject_cut.charAt(i + 1));
                    String second_value = String.valueOf(subject_cut.charAt(i + 2));
                    Integer converted = Integer.valueOf((first_value + second_value).toLowerCase(), 16);
                    int converted_int = (int) converted;
                    subject_bytes.write((byte) converted_int);
                    i += 2;
                } else {
                    subject_bytes.write((byte) c);
                }
            }
            try {
                return subject_bytes.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return subject;
    }

    public String getDate() {
        return date;
    }

    public boolean getRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public boolean hasUnreadChildren() {
        for (Map.Entry<String, NewsGroupArticle> article :
                getChildren().entrySet()) {
            if (!article.getValue().getRead() || article.getValue().hasUnreadChildren()) {
                return true;
            }
        }
        return false;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getReferences() {
        return references;
    }

    public HashMap<String, NewsGroupArticle> getChildren() {
        return children;
    }

    public void addReferences(String[] references) {
        this.references.addAll(Arrays.asList(references));
    }

    public void addArticle(NewsGroupArticle ngArticle) {
        String lastRef = null;
        for (String ref : ngArticle.getReferences()) {
            lastRef = ref;
        }

        if(lastRef.equals(this.articleID))
            children.put(ngArticle.articleID, ngArticle);
        else
            for (NewsGroupArticle childArticle : children.values()) {
                childArticle.addArticle(ngArticle);
            }
    }

    public String getText() throws IOException {
        if (text == null) {
            if (BuildConfig.DEBUG && group == null) throw new AssertionError("getText(): group should never be null");
            NewsGroupService service = new NewsGroupService(group.getServer());
            try {
                service.Connect();
                text = service.getArticleText(getArticleID());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                service.Disconnect();
            }
        }
        return text;
    }

    public NewsGroupEntry getGroup() {
        return group;
    }

    public NewsGroupArticle getSubArticel(String article) {
        NewsGroupArticle ng_article = children.get(article);
        if (ng_article == null) {
            for (NewsGroupArticle ng : children.values()) {
                ng_article = ng.getSubArticel(article);
                if (ng_article != null) {
                    break;
                }
            }
        }
        return ng_article;
    }
}
