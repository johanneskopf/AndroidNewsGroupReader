package com.freeteam01.androidnewsgroupreader.Models;


import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

public class NewsGroupServer
{
    String name_;
    HashSet<NewsGroupEntry> newsgroups_;

    public NewsGroupServer(String name)
    {
        this.name_ = name;
    }

    public void loadNewsGroups() throws IOException {
        NewsGroupService service = new NewsGroupService(name_);
        service.Connect();
        newsgroups_.addAll(service.getAllNewsgroups());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name_);
    }
}
