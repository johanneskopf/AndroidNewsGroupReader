package com.freeteam01.androidnewsgroupreader.ServicesTests;

import android.content.Context;
import android.content.SharedPreferences;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.Server;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
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
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isNotNull;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.example.LoggingClass")
@PrepareForTest({MobileServiceClient.class}) //, CookieManager.class
@MockPolicy(LogRedirection.class)
public class AzureServiceTests {

    @Mock
    Context context;
    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    MobileServiceClient client;
    @Mock
    MobileServiceSyncContext mobileServiceSyncContext;
    @Mock
    MobileServiceSyncTable<SubscribedNewsgroup> subscribedNewsgroupMobileServiceSyncTable;
    @Mock
    SQLiteLocalStore localStore;
    @Mock
    SimpleSyncHandler syncHandler;
    @Mock
    MobileServiceSyncTable mobileServiceSyncTable;
    @Mock
    MobileServiceUser mobileServiceUser;

/*    @Mock
    CookieManager cookieManager;*/

    private String token = "12345-token";
    private String userId = "userId";
    private String serverId = "serverId";
    private String name = "name";
    private String articleId = "555";
    private String forename = "Max";
    private String surname = "Mustermann";
    private String email = "max@gmx.at";
    private String id = "04393185-943d-48bb-8d2f-b369c2d94117";
    private final String USERIDPREF = "uid";
    private final String TOKENPREF = "tkn";

    @Before
    public void before() throws Exception {
        // mock all the statics
//        PowerMockito.mockStatic(CookieManager.class);

//        this.sharedPreferences = Mockito.mock(SharedPreferences.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences).thenReturn(sharedPreferences);
        when(sharedPreferences.getString(anyString(), (String) isNull())).thenReturn(userId).thenReturn(token);
        when(sharedPreferences.edit()).thenReturn(editor);
//        Mockito.when(new MobileServiceClient(any(String.class), any(Context.class)));
//        Mockito.when(new MobileServiceClient(anyString(), context)).thenReturn(client);
        when(client.getSyncContext()).thenReturn(mobileServiceSyncContext);
        final SettableFuture<Void> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(null);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncContext.initialize((MobileServiceLocalStore) isNotNull(), (MobileServiceSyncHandler) isNotNull())).thenReturn(result);
        String mobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
        PowerMockito.whenNew(MobileServiceClient.class)
                .withArguments(mobileBackendUrl, context)
                .thenReturn(client);
        when(client.getSyncTable(anyString(), any(Class.class))).thenReturn(mobileServiceSyncTable);
        when(client.getCurrentUser()).thenReturn(mobileServiceUser);
        when(mobileServiceUser.getUserId()).thenReturn(userId);
        final SettableFuture<MobileServiceList<SubscribedNewsgroup>> queryResultSubscribedNewsgroup = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<SubscribedNewsgroup> list = new ArrayList<>();
                    list.add(new SubscribedNewsgroup(userId, serverId, name));
                    int count = 1;
                    queryResultSubscribedNewsgroup.set(new MobileServiceList<>(list, count));
                } catch (Throwable throwable) {
                    queryResultSubscribedNewsgroup.setException(throwable);
                }
            }
        }).start();
        final SettableFuture<MobileServiceList<Server>> queryResultServer = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<Server> list = new ArrayList<>();
                    list.add(new Server(userId, serverId, name));
                    int count = 1;
                    queryResultServer.set(new MobileServiceList<>(list, count));
                } catch (Throwable throwable) {
                    queryResultServer.setException(throwable);
                }
            }
        }).start();
        final SettableFuture<MobileServiceList<ReadArticle>> queryResultReadArticle = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<ReadArticle> list = new ArrayList<>();
                    list.add(new ReadArticle(articleId, userId));
                    int count = 1;
                    queryResultReadArticle.set(new MobileServiceList<>(list, count));
                } catch (Throwable throwable) {
                    queryResultReadArticle.setException(throwable);
                }
            }
        }).start();
        final SettableFuture<MobileServiceList<UserSetting>> queryResultUserSetting = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<UserSetting> list = new ArrayList<>();
                    list.add(new UserSetting(userId, email, forename, surname));
                    int count = 1;
                    queryResultUserSetting.set(new MobileServiceList<>(list, count));
                } catch (Throwable throwable) {
                    queryResultUserSetting.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.read((Query) isNotNull()))
                .thenReturn(queryResultSubscribedNewsgroup)
                .thenReturn(queryResultServer)
                .thenReturn(queryResultReadArticle)
                .thenReturn(queryResultUserSetting)
                .thenReturn(queryResultSubscribedNewsgroup)
                .thenReturn(queryResultServer)
                .thenReturn(queryResultReadArticle)
                .thenReturn(queryResultUserSetting);

        when(mobileServiceSyncContext.push()).thenReturn(result);
        when(mobileServiceSyncTable.pull((Query) isNull())).thenReturn(result);

        try {
            AzureService azureService = AzureService.getInstance();
        }
        catch (IllegalStateException e){
        }
        if (!AzureService.isInitialized())
            AzureService.Initialize(context, client);
    }

    @Test
    public void test_addItemInTable() {
        SubscribedNewsgroup subscribedNewsgroup = new SubscribedNewsgroup(userId, serverId, name);
        final SubscribedNewsgroup subscribedNewsgroupWithId = new SubscribedNewsgroup(userId, serverId, name);
        subscribedNewsgroupWithId.setId(id);
        final SettableFuture<SubscribedNewsgroup> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(subscribedNewsgroupWithId);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.insert(any(SubscribedNewsgroup.class))).thenReturn(result);
        try {
            assertThat(AzureService.getInstance().addItemInTable(subscribedNewsgroup, mobileServiceSyncTable), Matchers.<Object>is(subscribedNewsgroupWithId));
            assertEquals(AzureService.getInstance().addItemInTable(subscribedNewsgroup, mobileServiceSyncTable), subscribedNewsgroupWithId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_deleteItemInTable() {
        final SubscribedNewsgroup subscribedNewsgroupWithId = new SubscribedNewsgroup(userId, serverId, name);
        subscribedNewsgroupWithId.setId(id);
        final SettableFuture<Void> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(null);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.delete((SubscribedNewsgroup) isNotNull())).thenReturn(result);
        try {
            AzureService.getInstance().deleteItemFromTable(subscribedNewsgroupWithId, mobileServiceSyncTable);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_updateItemInTable() {
        final SubscribedNewsgroup subscribedNewsgroupWithId = new SubscribedNewsgroup(userId, serverId, name);
        subscribedNewsgroupWithId.setId(id);
        final SettableFuture<Void> result = SettableFuture.create();
        subscribedNewsgroupWithId.setName("new name");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(null);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.update((SubscribedNewsgroup) isNotNull())).thenReturn(result);
        try {
            AzureService.getInstance().updateItemInTable(subscribedNewsgroupWithId, mobileServiceSyncTable);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_doubleInitialize() {
        if (!AzureService.isInitialized())
            AzureService.Initialize(context, client);
        try {
            AzureService.Initialize(context, client);
            Assert.fail("Double init should fail");
        }
        catch(IllegalStateException e){
        }
    }

    @Test
    public void test_readArticleChanged() {
//        String serverName = "news.tugraz.at";
//        NewsGroupServer newsGroupServer = new NewsGroupServer(serverName);
//        int articleCount = 1;
//        String name = "lv.algorithmen";
//        NewsGroupEntry newsGroupEntry = new NewsGroupEntry(newsGroupServer, articleCount, name);
//        String articleId = "12345", subject = "BubbleSort", date = "Wednesday 01 Jan 2017 10:10:10", from ="Miriam Musterfrau";
        NewsGroupArticle newsGroupArticle = mock(NewsGroupArticle.class); //new NewsGroupArticle(newsGroupEntry, articleId, subject, date, from);
        when(newsGroupArticle.getRead()).thenReturn(true);
        when(newsGroupArticle.getArticleID()).thenReturn("12345");
        final ReadArticle result = mock(ReadArticle.class);
//        final ReadArticle readArticle = new ReadArticle(articleId, userId);
//        final ReadArticle readArticleWithId = new ReadArticle(articleId, userId);
//        readArticleWithId.setId(id);
        final SettableFuture<ReadArticle> resultReadArticle = SettableFuture.create();
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    resultReadArticle.set(readArticle);
//                } catch (Throwable throwable) {
//                    resultReadArticle.setException(throwable);
//                }
//            }
//        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    resultReadArticle.set(result);
                } catch (Throwable throwable) {
                    resultReadArticle.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.insert(any(ReadArticle.class))).thenReturn(resultReadArticle);
//        try {
//            assertThat(AzureService.getInstance().addItemInTable(readArticle, mobileServiceSyncTable), Matchers.<Object>is(readArticleWithId));
//            assertEquals(AzureService.getInstance().addItemInTable(readArticle, mobileServiceSyncTable), readArticleWithId);
            AzureService.getInstance().readArticleChanged(newsGroupArticle);
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        newsGroupArticle.setRead(true);

    }

    @Test
    public void test_logout() {
//        CookieManager cookieManager = mock(CookieManager.class);
//        when(CookieManager.get)
//        Mockito.when(CookieManager.getInstance()).thenReturn(singleMock);
        AzureService.getInstance().logout();
    }

    @Test
    public void test_createClient()
    {
        MobileServiceClient client = AzureService.createClient(context);
    }

    @Test
    public void test_getter()
    {
        List<ReadArticle> readArticles = AzureService.getInstance().getReadArticles();
        List<Server> servers= AzureService.getInstance().getServers();
        List<SubscribedNewsgroup> subscribedNewsgroups = AzureService.getInstance().getSubscribedNewsgroups();
        UserSetting userSetting = AzureService.getInstance().getUserSetting();
    }

    @Test
    public void test_events()
    {
        assertTrue(AzureService.getInstance().isAzureServiceEventFired(SubscribedNewsgroup.class));
        assertFalse(AzureService.getInstance().isAzureServiceEventFired(String.class));

        AzureServiceEvent event = mock(AzureServiceEvent.class);
        AzureService.getInstance().addAzureServiceEventListener(SubscribedNewsgroup.class, event);
        AzureService.getInstance().addAzureServiceEventListener(String.class, event);
    }

    @Test
    public void test_persistServer()
    {
        String url = "news.tugraz.at";
//        final Server server = mock(Server.class);
        final Server server = new Server(name, url, userId);

        final SettableFuture<Server> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(server);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.insert(any(Server.class))).thenReturn(result);
        AzureService.getInstance().persist(server);
    }

    @Test
    public void test_persistUserSetting()
    {
        final UserSetting userSetting = mock(UserSetting.class);

        final SettableFuture<UserSetting> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(userSetting);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.insert(any(UserSetting.class))).thenReturn(result);
        AzureService.getInstance().persist(userSetting);
    }

    @Test
    public void test_persistSubscribedNewsgroups()
    {
        final NewsGroupEntry newsGroupEntry = mock(NewsGroupEntry.class);
        final List<NewsGroupEntry> newsGroupEntries = new ArrayList<>();
        newsGroupEntries.add(newsGroupEntry);

        final SettableFuture<NewsGroupEntry> result = SettableFuture.create();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result.set(newsGroupEntry);
                } catch (Throwable throwable) {
                    result.setException(throwable);
                }
            }
        }).start();
        when(mobileServiceSyncTable.insert(any(NewsGroupEntry.class))).thenReturn(result);
        AzureService.getInstance().persistSubscribedNewsgroups(newsGroupEntries);
    }

    /*@Test
    public void test_verify(){
        MainActivity activity = Mockito.mock(MainActivity.class);
        when(activity.getName()).thenReturn("MainActivity");
        when(activity.getNumber(anyInt())).thenReturn(0);
        //verify if getName() is never called
        verify(activity,never()).getName();
        //now call it one time
        activity.getName();
        //verify if it is called once
        verify(activity,atLeastOnce()).getName();
        //call getNumber method with a parameter
        activity.getNumber(1);
        //verify if getNumber was called with parameter 1
        verify(activity).getNumber(1);
    }*/
}
