package com.freeteam01.androidnewsgroupreader.ModelTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.junit.Ignore;
import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NewsGroupeArticleTests {


    @Test
    public void test_getArticleID() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupService service = mock(NewsGroupService.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        assertEquals("ID", article.getArticleID());
    }

    @Test
    public void test_getSubject() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        assertEquals("subject", article.getSubjectString());
    }

    @Test
    public void test_getAuthor() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        assertEquals("Max", article.getAuthor().getNameString());
    }

    @Test
    public void test_getDate() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        assertNotEquals(null, article.getDate());
    }

    @Test
    public void test_getRead() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        assertFalse(article.getRead());
    }

    @Test
    public void test_setDepth() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.setDepth(0);
    }

    @Ignore @Test
    public void test_setRead() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.setRead(true);
    }

    @Test
    public void test_hasUnreadChilds() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.hasUnreadChildren();
    }

    @Test
    public void test_getChildren() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.getChildren();
    }

    @Ignore @Test
    public void test_getText() throws Exception {
        NewsGroupServer server = new NewsGroupServer("news.tugraz.at");
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        when(entry.getServer()).thenReturn(server);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.getText();
    }

    @Test
    public void test_getGroupe() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.getGroup();
    }

    @Test
    public void test_getSubArticle() throws Exception {
        NewsGroupEntry entry = mock(NewsGroupEntry.class);
        NewsGroupArticle article = new NewsGroupArticle(entry, "ID", "subject", "Thu, 2 Feb 2017 21:29:40 +0100", "Max");
        article.getSubArticel("1");
    }
}
