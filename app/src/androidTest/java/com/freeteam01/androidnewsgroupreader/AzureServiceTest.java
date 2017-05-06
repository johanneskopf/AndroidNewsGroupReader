package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.ToDoItem;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
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
    public void init()
    {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.freeteam01.androidnewsgroupreader", appContext.getPackageName());
        if(!AzureService.isInitialized())
            AzureService.Initialize(appContext);
    }

    @Test
    public void insertData_isCorrect() throws Exception {
        MobileServiceClient mClient = AzureService.getInstance().getClient();
        MobileServiceTable<ToDoItem> mToDoTable = mClient.getTable(ToDoItem.class);

        int itemsCountBefore = mToDoTable.execute().get().size();
        int itemsToInsert = 5;
        List<ToDoItem> insertedItems = new ArrayList<>();

        for (int i = 0; i < itemsToInsert; i++) {
            ToDoItem item = new ToDoItem();
            item.setText("TestItem_" + (i + 1));
            item.setComplete(false);
            ToDoItem entity = mToDoTable.insert(item).get();
            insertedItems.add(entity);
        }

        assertEquals(itemsToInsert, insertedItems.size());

        for (ToDoItem item : insertedItems) {
            mToDoTable.delete(item);
        }

        int itemsCountAfter = mToDoTable.execute().get().size();
        assertEquals(itemsCountBefore, itemsCountAfter);
    }

    @Test
    public void insertNewsGroupArticle_isCorrect() throws Exception {
        Log.d(TAG, "Started testing");
        MobileServiceClient mClient = AzureService.getInstance().getClient();

        MobileServiceTable<NewsGroupArticle> mToDoTable = mClient.getTable(NewsGroupArticle.class);

        int itemsCountBefore = mToDoTable.execute().get().size();
        int itemsToInsert = 5;
        List<NewsGroupArticle> insertedItems = new ArrayList<>();

        for (int i = 0; i < itemsToInsert; i++) {
            NewsGroupArticle item = new NewsGroupArticle("" + (i + 1), "Math", "01.01.2017", "MaxMustermann");
            NewsGroupArticle entity = mToDoTable.insert(item).get();
            insertedItems.add(entity);
        }

        assertEquals(itemsToInsert, insertedItems.size());

        for (NewsGroupArticle item : insertedItems) {
            mToDoTable.delete(item);
        }

        int waitTillDeletionCompleted = 1000;
        Thread.sleep(waitTillDeletionCompleted);

        int itemsCountAfter = mToDoTable.execute().get().size();
        assertEquals(itemsCountBefore, itemsCountAfter);

        Log.d(TAG, "Finished testing");
    }
}
