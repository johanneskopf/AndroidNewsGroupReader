package com.freeteam01.androidnewsgroupreader.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;


public class AzureService {
    private String mobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
    private MobileServiceClient client;
    private static AzureService instance = null;
    private MobileServiceSyncTable<NewsGroupEntry> newsGroupEntryTable;

    private AzureService(Context context) {
        try {
            client = new MobileServiceClient(mobileBackendUrl, context);

/*            client.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });*/

//            newsGroupEntryTable = client.getTable(NewsGroupEntry.class);
            newsGroupEntryTable = client.getSyncTable(NewsGroupEntry.class);

            initLocalStore().get();

//            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }
    }

    public static void Initialize(Context context) {
        if (instance == null) {
            instance = new AzureService(context);
        } else {
            throw new IllegalStateException("AzureServiceAdapter is already initialized");
        }
    }

    public static AzureService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AzureServiceAdapter is not initialized");
        }
        return instance;
    }


    public static boolean isInitialized() {
        return instance != null;
    }

    public ArrayList<String> getSubscribedNewsgroupsTestData(){
        ArrayList<String> test_data = new ArrayList<>();
        test_data.add("tu-graz.flames");
        test_data.add("tu-graz.algorithmen");
        test_data.add("tu-graz.lv.cb");
        return test_data;
    }

    public MobileServiceClient getClient() {
        return client;
    }

    public void isNewsgroupStored(NewsGroupEntry item) throws ExecutionException, InterruptedException {
        newsGroupEntryTable.lookUp(item.getId()).get();
    }

    // execute this function - if it throws an error, then the items is already stored.
    public void updateItemInTable(NewsGroupEntry item) throws ExecutionException, InterruptedException {
        newsGroupEntryTable.update(item).get();
    }

    public void deleteItemFromTable(NewsGroupEntry item) throws ExecutionException, InterruptedException {
        newsGroupEntryTable.delete(item).get();
    }

    public NewsGroupEntry addItemInTable(NewsGroupEntry item) throws ExecutionException, InterruptedException {
        NewsGroupEntry entity = newsGroupEntryTable.insert(item).get();
        return entity;
    }

    public List<NewsGroupEntry> getLocalNewsGroupEntries() throws ExecutionException, InterruptedException {
        Query query = QueryOperations.tableName("NewsGroupEntry").orderBy("name", QueryOrder.Ascending);
        return newsGroupEntryTable.read(query).get();
    }

    public List<NewsGroupEntry> syncNewsGroupEntries() throws ExecutionException, InterruptedException {
        sync().get();
//        Query query = QueryOperations.field("selected").eq(val(false));
        Query query = QueryOperations.tableName("NewsGroupEntry").orderBy("name", QueryOrder.Ascending);
        return newsGroupEntryTable.read(query).get();
//        return newsGroupEntryTable.read(null).get();
    }

    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = client.getSyncContext();
                    syncContext.push().get();
                    newsGroupEntryTable.pull(null).get();
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
//                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = client.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(client.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("articleCount", ColumnDataType.Integer);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("selected", ColumnDataType.Boolean);

                    localStore.defineTable("NewsGroupEntry", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
//                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }
}