package com.freeteam01.androidnewsgroupreader.ServicesTests;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.RequiresPermission;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.Server;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isNotNull;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.example.LoggingClass")
@MockPolicy(LogRedirection.class)
public class RuntimeStorageTests {

    @Test
    public void test_getNewsgroupServer() {
        String validName = "news.tugraz.at";
        String notValidName = "Max";
        assertEquals(RuntimeStorage.instance().getNewsgroupServer(notValidName), null);
        RuntimeStorage.instance().addNewsgroupServer(validName);
        assertNotNull(RuntimeStorage.instance().getNewsgroupServer(validName));
        Set<String> servers = RuntimeStorage.instance().getAllNewsgroupServers();
        assertEquals(servers.size(), 1);

        SubscribedNewsgroup subscribedNewsgroup = mock(SubscribedNewsgroup.class);
        when(subscribedNewsgroup.getServerId()).thenReturn(validName);
        when(subscribedNewsgroup.getName()).thenReturn("lv.algorithmen");
        List<SubscribedNewsgroup> subscribedNewsgroups = new ArrayList<>();
        subscribedNewsgroups.add(subscribedNewsgroup);
        RuntimeStorage.instance().setNewsgroups(subscribedNewsgroups);
    }

    @Test
    public void test_getUserSettings() {
        assertNull(RuntimeStorage.instance().getUserSetting());
        UserSetting userSetting = mock(UserSetting.class);
        RuntimeStorage.instance().setUserSetting(userSetting);
        assertNotNull(RuntimeStorage.instance().getUserSetting());

        String articleId = "Article X";
        assertFalse(RuntimeStorage.instance().isRead(articleId));

        ReadArticle readArticle = mock(ReadArticle.class);
        when(readArticle.getArticleId()).thenReturn(articleId);
        List<ReadArticle> readArticles = new ArrayList<>();
        readArticles.add(readArticle);
        RuntimeStorage.instance().setReadArticles(readArticles);

        assertTrue(RuntimeStorage.instance().isRead(articleId));
    }
}
