package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Models.ToDoItem;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AzureServiceTest {
    @Before
    public void init() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.freeteam01.androidnewsgroupreader", appContext.getPackageName());
        if (!AzureService.isInitialized())
            AzureService.Initialize(appContext);
    }

    @Test
    public void insertData_isCorrect() throws Exception {
        MobileServiceClient client = AzureService.getInstance().getClient();
        MobileServiceTable<ToDoItem> toDoTable = client.getTable(ToDoItem.class);

        int itemsCountBefore = toDoTable.execute().get().size();
        int itemsToInsert = 5;
        List<ToDoItem> insertedItems = new ArrayList<>();

        for (int i = 0; i < itemsToInsert; i++) {
            ToDoItem item = new ToDoItem();
            item.setText("TestItem_" + (i + 1));
            item.setComplete(false);
            ToDoItem entity = toDoTable.insert(item).get();
            insertedItems.add(entity);
        }

        assertEquals(itemsToInsert, insertedItems.size());

        for (ToDoItem item : insertedItems) {
            toDoTable.delete(item);
        }

        int waitTillDeletionCompleted = 1000;
        Thread.sleep(waitTillDeletionCompleted);

        int itemsCountAfter = toDoTable.execute().get().size();
        assertEquals(itemsCountBefore, itemsCountAfter);
    }

    @Test
    public void insertNewsGroupArticle_isCorrect() throws Exception {
        Log.d(TAG, "Started testing");
        MobileServiceClient client = AzureService.getInstance().getClient();

        MobileServiceTable<NewsGroupArticle> articleTable = client.getTable(NewsGroupArticle.class);

        int itemsCountBefore = articleTable.execute().get().size();
        int itemsToInsert = 5;
        List<NewsGroupArticle> insertedItems = new ArrayList<>();

        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");
        NewsGroupService service = new NewsGroupService(ngServer);
        service.Connect();
        List<NewsGroupEntry> newsgroups = service.getAllNewsgroups();

        for (int i = 0; i < itemsToInsert; i++) {
            NewsGroupArticle item = new NewsGroupArticle(newsgroups.get(0),"" + (i + 1), "Math", "01.01.2017", "MaxMustermann");
            NewsGroupArticle entity = articleTable.insert(item).get();
            insertedItems.add(entity);
        }

        assertEquals(itemsToInsert, insertedItems.size());

        for (NewsGroupArticle item : insertedItems) {
            articleTable.delete(item);
        }

        int waitTillDeletionCompleted = 1000;
        Thread.sleep(waitTillDeletionCompleted);

        int itemsCountAfter = articleTable.execute().get().size();
        assertEquals(itemsCountBefore, itemsCountAfter);

        Log.d(TAG, "Finished testing");
    }

    @Test
    public void insertNewsGroupEntry_isCorrect() throws Exception {
        Log.d(TAG, "Started testing");
        MobileServiceClient client = AzureService.getInstance().getClient();

        MobileServiceTable<NewsGroupEntry> entryTable = client.getTable(NewsGroupEntry.class);

        int itemsCountBefore = entryTable.execute().get().size();
        int itemsToInsert = 5;
        List<NewsGroupEntry> insertedItems = new ArrayList<>();
        NewsGroupServer ngServer = new NewsGroupServer("news.tugraz.at");

        for (int i = 0; i < itemsToInsert; i++) {
            NewsGroupEntry item = new NewsGroupEntry(ngServer, i, "Entry_" + (i + 1));
            NewsGroupEntry entity = entryTable.insert(item).get();
            insertedItems.add(entity);
        }

        assertEquals(itemsToInsert, insertedItems.size());

        for (NewsGroupEntry item : insertedItems) {
            entryTable.delete(item);
        }

        int waitTillDeletionCompleted = 1000;
        Thread.sleep(waitTillDeletionCompleted);

        int itemsCountAfter = entryTable.execute().get().size();
        assertEquals(itemsCountBefore, itemsCountAfter);

        Log.d(TAG, "Finished testing");
    }
}
