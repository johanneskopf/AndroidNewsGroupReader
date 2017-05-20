package com.freeteam01.androidnewsgroupreader.Services;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class RuntimeStorage
{
    static RuntimeStorage instance_;
    public static RuntimeStorage instance()
    {
        if(instance_ ==  null)
        {
            instance_ = new RuntimeStorage();
        }
        return instance_;
    }

    HashMap<String, NewsGroupServer> servers_;

    public NewsGroupServer getNewsgroupServer(String name)
    {
        if(servers_.containsKey(name))
        {
            return servers_.get(name);
        }
        return null;
    }

    public void addNewsgroupServer(String name)
    {
        if(!servers_.containsKey(name)) {
            servers_.put(name, new NewsGroupServer(name));
        }
    }


    void loadNewsgroups(String server) throws IOException {
        servers_.get(server).loadNewsGroups();
    }
}
