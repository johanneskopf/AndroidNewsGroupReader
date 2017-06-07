package com.freeteam01.androidnewsgroupreader.Models;

import java.util.ArrayList;
import java.util.List;

public class NewsGroupPostArticle {
    private String from;
    private String subject;
    private String message;
    private List<String> newsgroups = new ArrayList<>();
    private List<String> references = new ArrayList<>();

    public NewsGroupPostArticle(String from, String subject, String message)
    {
        this.from = from;
        this.subject = subject;
        this.message = message;
    }

    public void addNewsgroupToPostTo(String newsgroup)
    {
        newsgroups.add(newsgroup);
    }

    public void addReference(String reference)
    {
        references.add(reference);
    }

    public void addReferences(List<String> references)
    {
        this.references.addAll(references);
    }

    public List<String> getNewsgroups()
    {
        return newsgroups;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getReferences() {
        return references;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }
}
