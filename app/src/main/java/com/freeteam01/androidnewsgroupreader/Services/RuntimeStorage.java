package com.freeteam01.androidnewsgroupreader.Services;

import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RuntimeStorage {
    static RuntimeStorage instance_;
    HashMap<String, NewsGroupServer> servers_ = new HashMap<>();

    RuntimeStorage() {
        //Load OfflineStorage and Sync with Azure!

        //TestCode

        final String tugraz = "news.tugraz.at";
        addNewsgroupServer(tugraz);

        ArrayList<String> subscribed = new ArrayList<>();
        subscribed.add("tu-graz.lv.bs");
        subscribed.add("tu-graz.lv.swp");
        subscribed.add("tu-graz.lv.bwl");
        subscribed.add("tu-graz.test");
//        getNewsgroupServer(tugraz).setSubscribed(subscribed);
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

    void loadNewsgroups(String server) throws IOException {
        servers_.get(server).loadNewsGroups();
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
}
