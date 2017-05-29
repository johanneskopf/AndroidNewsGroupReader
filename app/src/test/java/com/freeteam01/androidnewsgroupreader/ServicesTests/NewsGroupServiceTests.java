package com.freeteam01.androidnewsgroupreader.ServicesTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupPostArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NewsGroupServiceTests {

    @Test
    public void getAllNewsgroups_IsNonEmpty() throws Exception {
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
        service.Connect();

        List<NewsGroupEntry> newsgroups = service.getAllNewsgroups();

        assertFalse(newsgroups.isEmpty());

        service.Disconnect();
    }

    @Test
    public void getAllNewsgroups_ContainsTUGrazFlames() throws Exception {
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
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
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
        service.Connect();
        service.getAllNewsgroups().get(0);
        List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup(service.getAllNewsgroups().get(0));

        assertFalse(articles.isEmpty());

        service.Disconnect();
    }
    
    @Test
    public void getAllTopicsFromSWPNewsgroup() throws Exception {
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
        service.Connect();
        NewsGroupEntry ngEntry = null;
        List<NewsGroupEntry> ngEntries = service.getAllNewsgroups();
        for (NewsGroupEntry entry : ngEntries) {
            if(entry.getName().equals("tu-graz.lv.swp"))
                ngEntry = entry;
        }

        List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup(ngEntry);

        assertFalse(articles.isEmpty());

        service.Disconnect();
    }

    @Ignore("Don't spam the NG") @Test
    public void postToTestNG() throws Exception {
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
        service.Connect();

        NewsGroupPostArticle article = new NewsGroupPostArticle(
                "Test <test@test.com>",
                "Re: Just testing",
                "This is just a test");

        article.addNewsgroupToPostTo("tu-graz.test");
        article.addReference("<ogh046$fdb$1@news.tugraz.at>");

        service.postArticle(article);

        service.Disconnect();
    }}
