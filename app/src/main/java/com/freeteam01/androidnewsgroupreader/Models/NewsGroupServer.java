package com.freeteam01.androidnewsgroupreader.Models;


import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class NewsGroupServer {
    String name_;
    HashMap<String, NewsGroupEntry> newsgroups_ = new HashMap<>();
    private TreeSet<String> subscribed_ = new TreeSet<>();

    public NewsGroupServer(String name) {
        this.name_ = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name_);
    }

    public void reload() throws IOException {
        NewsGroupService service = new NewsGroupService(this);
        service.Connect();
        for (NewsGroupEntry ng : service.getAllNewsgroups()) {
            if (!newsgroups_.containsKey(ng.getName())) {
                newsgroups_.put(ng.getName(), ng);
            }
        }
        service.Disconnect();
        for (Map.Entry<String, NewsGroupEntry> entry : newsgroups_.entrySet()) {
            entry.getValue().setSubscribed(false);
        }
        for (String ng : subscribed_) {
            if (newsgroups_.containsKey(ng)) {
                newsgroups_.get(ng).setSubscribed(true);
            }
        }
    }

    public TreeSet<String> getSubscribed() {
        return subscribed_;
    }

    public void setSubscribed(String name) {
        subscribed_.add(name);
    }

    public void reload(String newsgroup) throws IOException {
        if (newsgroups_.containsKey(newsgroup)) {
            newsgroups_.get(newsgroup).loadArticles();
        } else {
            throw new IOException();
        }
    }

    public NewsGroupEntry getNewsgroup(String selected_newsgroup_) {
        return newsgroups_.get(selected_newsgroup_);
    }

    public String getName() {
        return name_;
    }

    public Collection<NewsGroupEntry> getAllNewsgroups() {
        return newsgroups_.values();
    }

    public void clearSubscribed() {
        subscribed_.clear();
    }
}