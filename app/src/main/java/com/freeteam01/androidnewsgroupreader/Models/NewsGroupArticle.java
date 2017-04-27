package com.freeteam01.androidnewsgroupreader.Models;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by marti on 4/19/17.
 */

public class NewsGroupArticle {
    private String articleID;
    private String subject;
    private String date;
    private String from;

    private List<String> references = new ArrayList<>();
    private HashMap<String, NewsGroupArticle> children = new HashMap<>();

    public NewsGroupArticle(String articleId, String subject, String date, String from) {
        this.articleID = articleId;
        this.subject = subject;
        this.date = date;
        this.from = from;
    }

    public String getArticleID() {
        return articleID;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectString(){
        if(subject.startsWith("=?UTF-8?Q?")) {
            String subject_cut = subject.replace("=?UTF-8?Q?", "");
            subject_cut = subject_cut.replace("?=", "");
            Log.d("NGART", subject_cut);
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

    public String getFrom() {
        return from;
    }

    public List<String> getReferences() {
        return references;
    }

    public void addReferences(String[] references) {
        this.references.addAll(Arrays.asList(references));
    }

    public void addArticle(NewsGroupArticle ngArticle) {
        addArticle(ngArticle, 0);
    }

    private void addArticle(NewsGroupArticle ngArticle, int depth) {
        if (ngArticle.getReferences().get(depth).equals(articleID)) {
            if (ngArticle.getReferences().size() == depth + 1)
                children.put(ngArticle.getArticleID(), ngArticle);
            else {
                if (children.containsKey(ngArticle.getReferences().get(depth + 1)))
                    children.get(ngArticle.getReferences().get(depth + 1)).addArticle(ngArticle, depth + 1);
                else
                    throw new IllegalArgumentException("A intermediat node is missing");
            }
        } else {
            throw new IllegalArgumentException("The Reference is not the one it should be");
        }
    }
}
