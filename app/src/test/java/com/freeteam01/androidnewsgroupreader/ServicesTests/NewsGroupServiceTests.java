package com.freeteam01.androidnewsgroupreader.ServicesTests;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import org.apache.commons.net.nntp.NNTPClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.repackaged.cglib.util.StringSwitcher;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.example.LoggingClass")
@PrepareForTest(NewsGroupService.class)
@MockPolicy(LogRedirection.class)
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
        for (NewsGroupEntry newsgroup : newsgroups) {
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
            if (entry.getName().equals("tu-graz.lv.swp"))
                ngEntry = entry;
        }

        List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup(ngEntry);

        assertFalse(articles.isEmpty());

        service.Disconnect();
    }

    @Test
    public void checkAnswerMessage() throws Exception {
        String reference_message = "From: TestUsername <TestUsermail>\n" +
                "Newsgroups: tu-graz.flames\n" +
                "Subject: Test subject\n" +
                "References: Previous_id Test_id\n" +
                "\n" +
                "Test message";

        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);

        List<String> refs = new ArrayList<>();
        refs.add("Previous_id");

        String test_answer = service.constructNNTPMessage("TestUsername", "TestUsermail",
                "Test message", "Test subject", "tu-graz.flames", "Test_id", refs);

        assertTrue(test_answer.equals(reference_message));
    }

    @Test
    public void checkPostMessage() throws Exception {
        String reference_message = "From: TestUsername <TestUsermail>\n" +
                "Newsgroups: tu-graz.flames\n" +
                "Subject: Test subject\n" +
                "\n" +
                "Test message";

        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);

        String test_post = service.constructNNTPMessage("TestUsername", "TestUsermail",
                "Test message", "Test subject", "tu-graz.flames", null, null);

        assertTrue(test_post.equals(reference_message));
    }

    @Test
    public void test_answer() {
        try {
            NNTPClient clientMock = mock(NNTPClient.class);
            whenNew(NNTPClient.class).withNoArguments().thenReturn(clientMock);
            NewsGroupServer serverMock = mock(NewsGroupServer.class);
            NewsGroupService service = new NewsGroupService(serverMock);
            service.Connect();

            String user_name = "user_name", user_mail = "user_mail", article_text = "article_text",
                    subject = "subject", group = "group", article_id = "article_id";
            List<String> references = new ArrayList<>();
            references.add("firstReference");

            Writer writerMock = mock(Writer.class);

            when(clientMock.isAllowedToPost()).thenReturn(false).thenReturn(true);
            when(clientMock.postArticle()).thenReturn(null).thenReturn(writerMock);
            when(clientMock.completePendingCommand()).thenReturn(false).thenReturn(true);
            doThrow(new IOException()).doNothing().when(writerMock).write(anyString());

            for (int times = 0; times < 5; times++) {
                service.answer(user_name, user_mail, article_text, subject, group, article_id, references);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void test_getArticleText() {
        try {
            NNTPClient clientMock = mock(NNTPClient.class);
            whenNew(NNTPClient.class).withNoArguments().thenReturn(clientMock);
            NewsGroupServer serverMock = mock(NewsGroupServer.class);
            NewsGroupService service = new NewsGroupService(serverMock);
            service.Connect();
            Reader readerMock = mock(Reader.class);
            when(clientMock.retrieveArticleBody(anyString())).thenReturn(readerMock);
            when(readerMock.read())
                    .thenReturn(84)
                    .thenReturn(101)
                    .thenReturn(115)
                    .thenReturn(116)
                    .thenReturn(-1);
            assertEquals(service.getArticleText("12345id"), "Test");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void test_post() {
        try {
            NNTPClient clientMock = mock(NNTPClient.class);
            whenNew(NNTPClient.class).withNoArguments().thenReturn(clientMock);
            NewsGroupServer serverMock = mock(NewsGroupServer.class);
            NewsGroupService service = new NewsGroupService(serverMock);
            service.Connect();

            String user_name = "user_name", user_mail = "user_mail", article_text = "article_text",
                    subject = "subject", group = "group";

            Writer writerMock = mock(Writer.class);

            when(clientMock.isAllowedToPost()).thenReturn(false).thenReturn(true);
            when(clientMock.postArticle()).thenReturn(null).thenReturn(writerMock);
            when(clientMock.completePendingCommand()).thenReturn(false).thenReturn(true);
            doThrow(new IOException()).doNothing().when(writerMock).write(anyString());

            for (int times = 0; times < 5; times++) {
                service.post(user_name, user_mail, article_text, subject, group);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void test_greaterIntMaximum() {
        try {
            Long greaterMaximumInterger = Long.valueOf(Integer.MAX_VALUE) + 1;
            NewsGroupService.safeLongToInt(greaterMaximumInterger);
            fail("The expected exception was not thrown");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
