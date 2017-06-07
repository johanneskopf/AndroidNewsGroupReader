package com.freeteam01.androidnewsgroupreader.Services;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuntimeStorage {
    private static RuntimeStorage instance_;
    private HashMap<String, NewsGroupServer> servers_ = new HashMap<>();
    private UserSetting userSetting = null;
    private Set<String> already_read = new HashSet<>();

    RuntimeStorage() {
        final String tugraz = "news.tugraz.at";
        addNewsgroupServer(tugraz);
    }

    public static RuntimeStorage instance() {
        if (instance_ == null) {
            instance_ = new RuntimeStorage();
        }
        return instance_;
    }

    public NewsGroupServer getNewsgroupServer(String name) {
        if (servers_.containsKey(name)) {
            return servers_.get(name);
        }
        return null;
    }

    public void addNewsgroupServer(String name) {
        if (!servers_.containsKey(name)) {
            servers_.put(name, new NewsGroupServer(name));
        }
    }

    public Set<String> getAllNewsgroupServers() {
        return servers_.keySet();
    }

    public void setNewsgroups(List<SubscribedNewsgroup> newsgroups) {
        for (NewsGroupServer s : servers_.values()) {
            s.clearSubscribed();
        }
        for (SubscribedNewsgroup ng : newsgroups) {
            addNewsgroupServer(ng.getServerId());
            getNewsgroupServer(ng.getServerId()).setSubscribed(ng.getName());
        }
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }

    public void setUserSetting(UserSetting userSetting) {
        this.userSetting = userSetting;
    }

    public void setReadArticles(List<ReadArticle> readArticles) {
        for (ReadArticle article : readArticles) {
            already_read.add(article.getArticleId());
        }
    }

    public boolean isRead(String articleID) {
        return already_read.contains(articleID);
    }
}
