package com.freeteam01.androidnewsgroupreader.ServicesTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NewsGroupServiceTests {

    @Test
    public void getAllNewsgroups_IsNonEmpty() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<NewsGroupEntry> newsgroups = service.getAllNewsgroups();

        assertFalse(newsgroups.isEmpty());

        service.Disconnect();
    }

    @Test
    public void getAllNewsgroups_ContainsTUGrazFlames() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<NewsGroupEntry> newsgroups = service.getAllNewsgroups();
        List<String> newsgroup_names = new ArrayList<>();
        for(NewsGroupEntry newsgroup: newsgroups){
            newsgroup_names.add(newsgroup.getName());
        }

        assertTrue(newsgroup_names.contains("tu-graz.flames"));

        service.Disconnect();
    }

    @Test
    public void getAllTopicsFromNewsgroup_IsNonEmpty() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup("tu-graz.algorithmen");

        assertFalse(articles.isEmpty());

        service.Disconnect();
    }
}
