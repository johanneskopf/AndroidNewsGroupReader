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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

public class AzureService {
    private String mobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
    private MobileServiceClient client;
    private static AzureService instance = null;
    private MobileServiceSyncTable<NewsGroupEntry> newsGroupEntryTable;
    private List<NewsGroupEntry> newsGroupEntries;
    private boolean azureServiceEventFired = false;

    protected Vector _listeners;

    private AzureService(Context context) {
        this.newsGroupEntries = new ArrayList<>();
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

            loadLocalNewsgroups();

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

    public void addAzureServiceEventListener(AzureServiceEvent listener) {
        if (_listeners == null)
            _listeners = new Vector();

        _listeners.addElement(listener);
    }

    protected void fireAzureServiceEvent(List<NewsGroupEntry> newsGroupEntries) {
        Log.d("AzureService", "fireAzureServiceEvent");
        if (_listeners != null && !_listeners.isEmpty()) {
            Enumeration e = _listeners.elements();
            while (e.hasMoreElements()) {
                Log.d("AzureService", "fireAzureServiceEvent for a listener");
                AzureServiceEvent ev = (AzureServiceEvent) e.nextElement();
                ev.OnNewsgroupsLoaded(newsGroupEntries);
            }
        }
        setAzureServiceEventFired(true);
    }

    private void loadLocalNewsgroups() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<NewsGroupEntry> storedEntries = getLocalNewsGroupEntries();
                    newsGroupEntries.clear();
                    newsGroupEntries.addAll(storedEntries);
                    Log.d("AzureService", "loaded newsgroupentries from local storage");
                } catch (final Exception e) {
                    Log.d("AzureService", "loadLocalNewsgroups: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                fireAzureServiceEvent(newsGroupEntries);
                syncLocalWithRemote();
            }
        };

        runAsyncTask(task);
    }

    private void syncLocalWithRemote() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d("AzureService", "sync items with remote");
                    final List<NewsGroupEntry> storedEntries = syncNewsGroupEntries();
                    newsGroupEntries.clear();
                    newsGroupEntries.addAll(storedEntries);
                    Log.d("AzureService", "synced items with remote");
                    fireAzureServiceEvent(newsGroupEntries);
                } catch (final Exception e) {
                    Log.d("AzureService", "syncLocalWithRemote: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                fireAzureServiceEvent(newsGroupEntries);
                syncLocalWithNewsgroup();
            }
        };

        runAsyncTask(task);
    }

    private void syncLocalWithNewsgroup() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Log.d("AzureService", "getting items from newsgroup");
                    final List<NewsGroupEntry> results = new ArrayList<>();
                    NewsGroupService service = new NewsGroupService();
                    service.Connect();
                    results.addAll(service.getAllNewsgroups());
                    service.Disconnect();
                    Collections.sort(results, new NewsGroupEntryComparator());
                    Log.d("AzureService", "loaded items from newsgroup");

                    try {
                        boolean fireAzureEvent = false;
                        for (NewsGroupEntry item : results) {
                            NewsGroupEntry localStored = getEntryWithName(newsGroupEntries, item.getName());
                            if (localStored == null) {
                                NewsGroupEntry added = addItemInTable(item);
                                newsGroupEntries.add(added);
                                Log.d("AzureService", "stored item locally: " + added.getName());
                                fireAzureEvent = true;
                            } else {
                                boolean changed = false;
                                if (item.getArticleCount() != localStored.getArticleCount()) {
                                    localStored.setArticleCount(item.getArticleCount());
                                    changed = true;
                                }
                                if (changed) {
                                    updateItemInTable(localStored);
                                    Log.d("AzureService", "changed item locally: " + localStored.getName());
                                    fireAzureEvent = true;
                                }
                            }
                        }

                        for (int entry = 0; entry < newsGroupEntries.size(); entry++)
                        {
                            NewsGroupEntry local = newsGroupEntries.get(entry);
                            NewsGroupEntry item = getEntryWithName(results, local.getName());
                            if (item == null) {
                                deleteItemFromTable(local);
                                newsGroupEntries.remove(local);
                            }
                        }
                        if (fireAzureEvent)
                            fireAzureServiceEvent(newsGroupEntries);
                    } catch (ExecutionException e) {
                        Log.d("AzureService", e.getMessage());
                    } catch (InterruptedException e) {
                        Log.d("AzureService", e.getMessage());
                    }
                    Log.d("AzureService", "synced items with newsgroup");
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
                }
                return null;
            }

//            @Override
//            protected void onPostExecute(Void result) {
//                fireAzureServiceEvent(newsGroupEntries);
//            }
        };

        runAsyncTask(task);
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

/*    public ArrayList<String> getSubscribedNewsgroupsTestData() {
        ArrayList<String> test_data = new ArrayList<>();
        test_data.add("tu-graz.flames");
        test_data.add("tu-graz.algorithmen");
        test_data.add("tu-graz.lv.cb");
        return test_data;
    }*/

/*    public ArrayList<String> getSubscribedNewsgroups() {
        ArrayList<String> data = new ArrayList<>();
        for (NewsGroupEntry newsGroupEntry : newsGroupEntries) {
            if (newsGroupEntry.isSelected())
                data.add(newsGroupEntry.getName());
        }
        return data;
    }*/

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
                    Log.d("AzureService", "sync: " + e.getMessage());
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

                    int databaseVersion = 1;
                    String databaseName = "OfflineStore";
                    SQLiteLocalStore localStore = new SQLiteLocalStore(client.getContext(), databaseName, null, databaseVersion);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("articleCount", ColumnDataType.Integer);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("selected", ColumnDataType.Boolean);

                    localStore.defineTable("NewsGroupEntry", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    Log.d("AzureService", "initLocalStore: " + e.getMessage());
                    e.printStackTrace();
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    public List<NewsGroupEntry> getNewsGroupEntries() {
        return newsGroupEntries;
    }

    public void setSelectedNewsGroupEntries(List<NewsGroupEntry> selectedNewsGroupEntries) {
        for (NewsGroupEntry changedGroupEntry : selectedNewsGroupEntries) {
            NewsGroupEntry set = newsGroupEntries.get(newsGroupEntries.indexOf(changedGroupEntry));
            set.setSelected(changedGroupEntry.isSelected());
        }
    }

    public void persistSelectedNewsGroupEntries(final List<NewsGroupEntry> selectedNewsGroupEntries) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    for (NewsGroupEntry changedEntry : selectedNewsGroupEntries) {
                        Log.d("AzureService", "Persisting NewsGroupEntry: " + changedEntry);
                        NewsGroupEntry set = newsGroupEntries.get(newsGroupEntries.indexOf(changedEntry));
                        set.setSelected(changedEntry.isSelected());
                        updateItemInTable(changedEntry);
                        Log.d("AzureService", "Persisted NewsGroupEntry: " + set);
                    }
                    if(selectedNewsGroupEntries.size() > 0)
                        fireAzureServiceEvent(newsGroupEntries);
                    Log.d("AzureService", "updated selected items in local database");
                } catch (ExecutionException | InterruptedException e) {
                    Log.d("AzureService", "persistSelectedNewsGroupEntries: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    public boolean isAzureServiceEventFired() {
        return azureServiceEventFired;
    }

    public void setAzureServiceEventFired(boolean azureServiceEventFired) {
        this.azureServiceEventFired = azureServiceEventFired;
    }

    public class NewsGroupEntryComparator implements Comparator<NewsGroupEntry> {
        @Override
        public int compare(NewsGroupEntry o1, NewsGroupEntry o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public NewsGroupEntry getEntryWithName(Collection<NewsGroupEntry> c, String name) {
        for (NewsGroupEntry o : c) {
            if (o != null && o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    private void showEntriesFromTestData() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    newsGroupEntries.clear();
                    newsGroupEntries.add(new NewsGroupEntry(0, "tu-graz.algo", false));
                    newsGroupEntries.add(new NewsGroupEntry(1, "tu-graz.datenbanken", true));
                    newsGroupEntries.add(new NewsGroupEntry(2, "tu-graz.diverses", false));
                    newsGroupEntries.add(new NewsGroupEntry(3, "tu-graz.skripten", true));
                    newsGroupEntries.add(new NewsGroupEntry(4, "tu-graz.telekom", true));
                    newsGroupEntries.add(new NewsGroupEntry(5, "tu-graz.veranstaltungen", false));
                    newsGroupEntries.add(new NewsGroupEntry(6, "tu-graz.wohnungsmarkt", false));

                    fireAzureServiceEvent(newsGroupEntries);
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
                }

                return null;
            }
        };

        runAsyncTask(task);
    }
}