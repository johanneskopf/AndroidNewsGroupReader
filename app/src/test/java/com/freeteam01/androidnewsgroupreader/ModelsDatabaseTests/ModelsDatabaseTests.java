package com.freeteam01.androidnewsgroupreader.ModelsDatabaseTests;

import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.Server;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;

import junit.framework.Assert;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ModelsDatabaseTests {
    @Test
    public void test_readArticle() {
        String id = "12345", userId = "userId", articleId = "articleId";
        String newId = "54321", newUserId = "newUserId", newArticleId = "newArticleId";

        ReadArticle readArticle = new ReadArticle(articleId, userId);
        readArticle.setId(id);

        assertEquals(readArticle.getId(), id);
        readArticle.setId(newId);
        assertEquals(readArticle.getId(), newId);

        assertEquals(readArticle.getUserId(), userId);
        readArticle.setUserId(newUserId);
        assertEquals(readArticle.getUserId(), newUserId);

        assertEquals(readArticle.getArticleId(), articleId);
        readArticle.setArticleId(newArticleId);
        assertEquals(readArticle.getArticleId(), newArticleId);

        ReadArticle first = readArticle;
        ReadArticle second = readArticle;
        ReadArticle third = new ReadArticle(articleId, userId);

        assertTrue(first.equals(first));
        assertTrue(first.equals(second));

        assertFalse(first.equals(null));
        assertFalse(first.equals(5));

        assertFalse(first.equals(third));
        first.setUserId(null);
        assertFalse(first.equals(third));
        first.setUserId(newUserId);
        assertFalse(first.equals(third));
        third.setUserId(null);
        assertFalse(first.equals(third));
        third.setUserId(newUserId);
        assertFalse(first.equals(third));
        first.setArticleId(null);
        assertFalse(first.equals(third));
        first.setArticleId(newArticleId);
        assertFalse(first.equals(third));
        third.setArticleId(null);
        assertFalse(first.equals(third));
    }

    @Test
    public void test_server() {
        String id = "12345", name = "name", url = "url", userId = "userId";
        String newId = "54321", newName = "newName", newUrl = "newUrl", newUserId = "newUserId";

        Server server = new Server(name, url, userId);
        server.setId(id);

        assertEquals(server.getId(), id);
        server.setId(newId);
        assertEquals(server.getId(), newId);

        assertEquals(server.getName(), name);
        server.setName(newName);
        assertEquals(server.getName(), newName);

        assertEquals(server.getUrl(), url);
        server.setUrl(newUrl);
        assertEquals(server.getUrl(), newUrl);

        assertEquals(server.getUserId(), userId);
        server.setUserId(newUserId);
        assertEquals(server.getUserId(), newUserId);
    }

    @Test
    public void test_subscribedNewsgroup() {
        String id = "12345", userId = "userId", serverId = "serverId", name = "name";
        String newId = "54321", newUserId = "newUserId", newServerId = "newServerId", newName = "newName";

        SubscribedNewsgroup subscribedNewsgroup = new SubscribedNewsgroup(userId, serverId, name);
        subscribedNewsgroup.setId(id);

        assertEquals(subscribedNewsgroup.getId(), id);
        subscribedNewsgroup.setId(newId);
        assertEquals(subscribedNewsgroup.getId(), newId);

        assertEquals(subscribedNewsgroup.getUserId(), userId);
        subscribedNewsgroup.setUserId(newUserId);
        assertEquals(subscribedNewsgroup.getUserId(), newUserId);

        assertEquals(subscribedNewsgroup.getServerId(), serverId);
        subscribedNewsgroup.setServerId(newServerId);
        assertEquals(subscribedNewsgroup.getServerId(), newServerId);

        assertEquals(subscribedNewsgroup.getName(), name);
        subscribedNewsgroup.setName(newName);
        assertEquals(subscribedNewsgroup.getName(), newName);

        String output =  "SubscribedNewsgroup{id='54321\', userId='newUserId\', serverId='newServerId\', name='newName\'}";
        assertEquals(subscribedNewsgroup.toString(), output);

        SubscribedNewsgroup first = subscribedNewsgroup;
        SubscribedNewsgroup second = subscribedNewsgroup;
        SubscribedNewsgroup third = new SubscribedNewsgroup(userId, serverId, name);

        assertTrue(first.equals(first));
        assertTrue(first.equals(second));

        assertFalse(first.equals(null));
        assertFalse(first.equals(5));

        assertFalse(first.equals(third));
        third.setServerId(newServerId);
        assertFalse(first.equals(third));
    }

    @Test
    public void test_userSetting() {
        String id = "12345", userId = "userId", email = "email", forename = "forename", surname = "surname";
        String newId = "54321", newUserId = "newUserId", newEmail = "newEmail", newForename = "newForename", newSurname = "newSurname";

        UserSetting userSetting = new UserSetting(userId, email, forename, surname);
        userSetting.setId(id);

        assertEquals(userSetting.getId(), id);
        userSetting.setId(newId);
        assertEquals(userSetting.getId(), newId);

        assertEquals(userSetting.getUserId(), userId);
        userSetting.setUserId(newUserId);
        assertEquals(userSetting.getUserId(), newUserId);

        assertEquals(userSetting.getEmail(), email);
        userSetting.setEmail(newEmail);
        assertEquals(userSetting.getEmail(), newEmail);

        assertEquals(userSetting.getForename(), forename);
        userSetting.setForename(newForename);
        assertEquals(userSetting.getForename(), newForename);

        assertEquals(userSetting.getSurname(), surname);
        userSetting.setSurname(newSurname);
        assertEquals(userSetting.getSurname(), newSurname);

        String output =  "UserSetting{id='54321\', userId='newUserId\', email='newEmail\', forename='newForename\', surname='newSurname\'}";
        assertEquals(userSetting.toString(), output);
    }
}
