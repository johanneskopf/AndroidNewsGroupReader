package com.freeteam01.androidnewsgroupreader.ServicesTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NewsGroupServiceTests {

    @Test
    public void getAllNewsgroups_IsNonEmpty() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<String> newsgrous = service.getAllNewsgroups();

        assertFalse(newsgrous.isEmpty());

        service.Disconnect();
    }

    @Test
    public void getAllNewsgroups_ContainsTUGrazFlames() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<String> newsgrous = service.getAllNewsgroups();

        assertTrue(newsgrous.contains("tu-graz.flames"));

        service.Disconnect();
    }

    @Test
    public void getAllTopicsFromNewsgroup_IsNonEmpty() throws Exception {
        NewsGroupService service = new NewsGroupService();
        service.Connect();
        List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup("tu-graz.flames");

        assertFalse(articles.isEmpty());

        service.Disconnect();
    }
}
