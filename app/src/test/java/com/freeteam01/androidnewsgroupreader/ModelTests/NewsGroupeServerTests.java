package com.freeteam01.androidnewsgroupreader.ModelTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewsGroupeServerTests {

    @Test
    public void test_hashCode() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.hashCode();
    }

    @Test
    public void test_reload() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.reload();
    }

    @Test
    public void test_getSubscribed() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.getSubscribed();
    }

    @Test
    public void test_getAllNewsgroupe() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.getAllNewsgroups();
    }

    @Ignore @Test
    public void test_reloadString() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.reload();
        server.reload("tu-graz.flames");
    }

    @Test
    public void test_getNewsgroup() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        server.getNewsgroup("tu-graz.flames");
    }


}
