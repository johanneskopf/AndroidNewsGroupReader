package com.freeteam01.androidnewsgroupreader.Models;

import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.apache.commons.net.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsGroupArticle {
    private String id;
    private String articleID;
    private String subject;
    private String from;
    private String subject_string;
    private Author author;
    private Date date;
    private NewsGroupEntry group;
    private String text;
    private boolean isRead;
    private transient List<String> references = new ArrayList<>();
    private transient HashMap<String, NewsGroupArticle> children = new HashMap<>();

    public NewsGroupArticle(NewsGroupEntry group, String articleId, String subject, String date, String from) {
        this.articleID = articleId;
        this.group = group;
        this.subject = subject;
        this.author = new Author(from);
        this.subject_string = convertToEncoding(subject);
        this.date = new Date(date);
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
        return subject_string;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getReferences() {
        return references;
    }

    public Author getAuthor(){
        return author;
    }

    public Date getDate() {
        return date;
    }

    public boolean getRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    private String convertToEncoding(String encoded_subject){
        if (encoded_subject.contains("=?UTF-8?Q?")) {
            String subject_cut = encoded_subject.replace("=?UTF-8?Q?", "");
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
        else if(encoded_subject.contains("=?UTF-8?B?")){
            int start_index = encoded_subject.indexOf("=?UTF-8?B?") + "=?UTF-8?B?".length() - 1;
            String subject_cut = encoded_subject.substring(start_index, encoded_subject.length()-1);
            byte[] valueDecoded= Base64.decodeBase64(subject_cut.getBytes());
            return new String(valueDecoded);
        }
        else
            return encoded_subject;
        return null;
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
            assert (group != null);
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
