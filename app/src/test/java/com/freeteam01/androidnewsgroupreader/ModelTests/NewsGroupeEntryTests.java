package com.freeteam01.androidnewsgroupreader.ModelTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NewsGroupeEntryTests {


    @Test
    public void test_getArticleCount() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        assertEquals(entry.getArticleCount(), 10);
    }

    @Test
    public void test_setName() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.setName("abc");
    }

    @Test
    public void test_isSubscribed() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.isSubscribed();
    }

    @Test
    public void test_hashCode() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.hashCode();
    }

    @Test
    public void test_toString() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.toString();
    }

    @Test
    public void test_setSubscribed() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.setSubscribed(true);
    }

    @Test
    public void test_getServer() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.getServer();
    }

    @Test
    public void test_getArticle() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.getArticle("1");
    }

    @Test
    public void test_getArticles() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("abc"), 10, "NG1");
        entry.getArticles();
    }

    @Ignore @Test
    public void test_loadArticles() throws Exception {
        NewsGroupEntry entry = new NewsGroupEntry(new NewsGroupServer("news.tugraz.at"), 10, "tu-graz.flames");
        entry.loadArticles();
    }
}
