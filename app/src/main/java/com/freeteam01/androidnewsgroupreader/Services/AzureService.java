package com.freeteam01.androidnewsgroupreader.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.Server;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AzureService {

    public static final int LOGIN_REQUEST_CODE_GOOGLE = 1;
    private static final String URL_SCHEME = "fakenewsapptestapp";
    private static AzureService instance = null;
    private String mobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
    private MobileServiceClient client;

    private MobileServiceSyncTable<NewsGroupEntry> newsGroupEntryTable;
    private MobileServiceSyncTable<ReadArticle> readArticleTable;
    private MobileServiceSyncTable<Server> serverTable;
    private MobileServiceSyncTable<SubscribedNewsgroup> subscribedNewsgroupTable;

    private List<NewsGroupEntry> newsGroupEntries;
    private List<ReadArticle> readArticles;
    private List<Server> servers;
    private List<SubscribedNewsgroup> subscribedNewsgroups;

    private Map<Class<?>, List<AzureServiceEvent>> azureServiceEventListeners;
    private Map<Class<?>, Boolean> azureServiceEventFired;

    // TODO Hint: MobileServiceSyncTable.java  tells you which functions are performing
    // TODO             local operations, and which remote operations

    private AzureService(Context context) {
        azureServiceEventListeners = new HashMap<>();
        azureServiceEventFired = new HashMap<>();

        newsGroupEntries = new ArrayList<>();
        subscribedNewsgroups = new ArrayList<>();
        readArticles = new ArrayList<>();
        servers = new ArrayList<>();

        try {
            client = new MobileServiceClient(mobileBackendUrl, context);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        authenticate();
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

    public MobileServiceClient getClient() {
        return client;
    }

    public List<ReadArticle> getReadArticles() {
        return readArticles;
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<SubscribedNewsgroup> getSubscribedNewsgroups() {
        return subscribedNewsgroups;
    }

    public void OnAuthenticated() {
        // only execute this if the login was successful

        readArticleTable = client.getSyncTable("ReadArticle", ReadArticle.class);
        serverTable = client.getSyncTable("Server", Server.class);
        subscribedNewsgroupTable = client.getSyncTable("SubscribedNewsgroup", SubscribedNewsgroup.class);
        try {
            initLocalStore().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }
        loadLocalData();
    }

    public <T> boolean isAzureServiceEventFired(Class<T> classType) {
        if(!azureServiceEventFired.containsKey(classType))
            return false;
        return azureServiceEventFired.get(classType);
    }

    public <T> boolean setAzureServiceEventFired(Class<T> classType, boolean fired) {
        return azureServiceEventFired.put(classType, fired);
    }

    private void authenticate() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("access_type", "offline");
        client.login("Google", URL_SCHEME, LOGIN_REQUEST_CODE_GOOGLE, parameters);
    }

    private void loadLocalData() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<SubscribedNewsgroup> storedSubscribedNewsgroupEntries = getLocalData(SubscribedNewsgroup.class, subscribedNewsgroupTable);
                    subscribedNewsgroups.clear();
                    subscribedNewsgroups.addAll(storedSubscribedNewsgroupEntries);
                    final List<Server> storedServerEntries = getLocalData(Server.class, serverTable);
                    servers.clear();
                    servers.addAll(storedServerEntries);
                    final List<ReadArticle> storedReadArticleEntries = getLocalData(ReadArticle.class, readArticleTable);
                    readArticles.clear();
                    readArticles.addAll(storedReadArticleEntries);
                    Log.d("AzureService", "loaded data from local storage");
                } catch (final Exception e) {
                    Log.d("AzureService", "loadLocalData: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                fireAzureServiceEvent(SubscribedNewsgroup.class, subscribedNewsgroups);
                fireAzureServiceEvent(Server.class, servers);
                fireAzureServiceEvent(ReadArticle.class, readArticles);
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
                    Log.d("AzureService", "START: sync items with remote");
                    final List<SubscribedNewsgroup> storedSubscribedNewsgroupEntries = syncData(SubscribedNewsgroup.class, subscribedNewsgroupTable);
                    subscribedNewsgroups.clear();
                    subscribedNewsgroups.addAll(storedSubscribedNewsgroupEntries);
                    final List<Server> storedServerEntries = syncData(Server.class, serverTable);
                    servers.clear();
                    servers.addAll(storedServerEntries);
                    final List<ReadArticle> storedReadArticleEntries = syncData(ReadArticle.class, readArticleTable);
                    readArticles.clear();
                    readArticles.addAll(storedReadArticleEntries);
                    Log.d("AzureService", "synced items with remote");
//                    fireAzureServiceEvent(newsGroupEntries);
                } catch (final Exception e) {
                    Log.d("AzureService", "syncLocalWithRemote: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                fireAzureServiceEvent(SubscribedNewsgroup.class, subscribedNewsgroups);
                fireAzureServiceEvent(Server.class, servers);
                fireAzureServiceEvent(ReadArticle.class, readArticles);
            }
        };

        runAsyncTask(task);
    }

    public <T> void addAzureServiceEventListener(Class<T> classType, AzureServiceEvent listener) {
        if(azureServiceEventListeners.containsKey(classType))
        {
            List<AzureServiceEvent> list =azureServiceEventListeners.get(classType);
            list.add(listener);
        }
        else
        {
            List<AzureServiceEvent> list = new ArrayList<>();
            list.add(listener);
            azureServiceEventListeners.put(classType, list);
        }
        Log.d("AzureService", "Added listener for type: " + classType.getSimpleName());
    }

    protected <T> void fireAzureServiceEvent(Class<T> classType, List<T> entries) {
        Log.d("AzureService", "fireAzureServiceEvent of type: " + classType.getSimpleName() + ", because " + entries.size() + " got updated");
        if (azureServiceEventListeners != null && !azureServiceEventListeners.isEmpty()) {
            List<AzureServiceEvent> list = azureServiceEventListeners.get(classType);
            for (AzureServiceEvent e : list) {
                Log.d("AzureService", " - fired for a listener");
                e.OnLoaded(classType, entries);
            }
        }
        setAzureServiceEventFired(classType, true);
    }

    // execute this function - if it throws an error, then the items is already stored.
    public <T> void updateItemInTable(T item, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        table.update(item).get();
    }

    public <T> void deleteItemFromTable(T item, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        table.delete(item).get();
    }

    public <T> T addItemInTable(T item, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        T entity = table.insert(item).get();
        return entity;
    }

    public <T> List<T> getLocalData(Class<T> classType, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        Query query = QueryOperations.tableName(classType.getSimpleName()).orderBy("name", QueryOrder.Ascending);
        return table.read(query).get();
    }

    public List<NewsGroupEntry> syncNewsGroupEntries() throws ExecutionException, InterruptedException {
        sync().get();
//        Query query = QueryOperations.field("selected").eq(val(false));
        Query query = QueryOperations.tableName("NewsGroupEntry").orderBy("name", QueryOrder.Ascending);
        return newsGroupEntryTable.read(query).get();
//        return newsGroupEntryTable.read(null).get();
    }

    public <T> List<T> syncData(Class<T> classType, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        MobileServiceSyncContext syncContext = client.getSyncContext();
        syncContext.push().get();
        table.pull(null).get();
        Log.d("AzureService", "syncData: " + classType.getSimpleName());
//        Query query = QueryOperations.field("selected").eq(val(false));
        Query query = QueryOperations.tableName(classType.getSimpleName()).orderBy("name", QueryOrder.Ascending);
        return table.read(query).get();
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

                    Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("articleId", ColumnDataType.String);
                    localStore.defineTable("ReadArticle", tableDefinition);

                    tableDefinition = new HashMap<>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("url", ColumnDataType.String);
                    tableDefinition.put("userId", ColumnDataType.String);
                    localStore.defineTable("Server", tableDefinition);

                    tableDefinition = new HashMap<>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("userId", ColumnDataType.String);
                    tableDefinition.put("serverId", ColumnDataType.String);
                    tableDefinition.put("name", ColumnDataType.String);
                    localStore.defineTable("SubscribedNewsgroup", tableDefinition);

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

    public void persistSubscribedNewsgroups(final List<NewsGroupEntry> subscribedNewsgroupEntries) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    for (NewsGroupEntry changedEntry : subscribedNewsgroupEntries) {
                        // TODO change "testUserId" to real user id
                        SubscribedNewsgroup subscribedNewsgroup = new SubscribedNewsgroup("testUserId", changedEntry.getServer().getName(), changedEntry.getName());
//                        Log.d("AzureService", "Persisting subscribedNewsgroup: " + subscribedNewsgroup);
/*                        NewsGroupEntry set = newsGroupEntries.get(newsGroupEntries.indexOf(changedEntry));
                        set.setSubscribed(changedEntry.isSubscribed());
                        updateItemInTable(changedEntry);*/
                        if (subscribedNewsgroups.contains(subscribedNewsgroup)) {
                            int index = subscribedNewsgroups.indexOf(subscribedNewsgroup);
                            subscribedNewsgroup = subscribedNewsgroups.get(index);
                            if (changedEntry.isSubscribed()) {
                                Log.d("AzureService", "Updating subscribedNewsgroup: " + subscribedNewsgroup);
                                updateItemInTable(subscribedNewsgroup, subscribedNewsgroupTable);
                                subscribedNewsgroups.set(index, subscribedNewsgroup);
                            } else {
                                Log.d("AzureService", "Deleting subscribedNewsgroup: " + subscribedNewsgroup);
                                deleteItemFromTable(subscribedNewsgroup, subscribedNewsgroupTable);
                                subscribedNewsgroups.remove(index);
                            }
                        } else {
                            if (changedEntry.isSubscribed()) {
                                Log.d("AzureService", "Adding subscribedNewsgroup: " + subscribedNewsgroup);
                                subscribedNewsgroup = addItemInTable(subscribedNewsgroup, subscribedNewsgroupTable);
                                subscribedNewsgroups.add(subscribedNewsgroup);
                            }
                        }
//                        Log.d("AzureService", "Persisted subscribedNewsgroup: " + subscribedNewsgroup);
                    }
/*                    if (subscribedNewsgroups.size() > 0)
                        fireAzureServiceEvent(newsGroupEntries);*/
                    Log.d("AzureService", "subscribedNewsgroupEntries successful");
                } catch (ExecutionException | InterruptedException e) {
                    Log.d("AzureService", "subscribedNewsgroupEntries: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };

        runAsyncTask(task);
    }


    /*    private void loadLocalNewsgroups() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<NewsGroupEntry> storedEntries = getLocalData(NewsGroupEntry.class, newsGroupEntryTable);
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
                fireAzureServiceEvent(NewsGroupEntry.class, newsGroupEntries);
                syncLocalWithRemote();
            }
        };

        runAsyncTask(task);
    }*/

/*    public List<NewsGroupEntry> getNewsGroupEntries() {
        return newsGroupEntries;
    }*/

/*    public void setSelectedNewsGroupEntries(List<NewsGroupEntry> selectedNewsGroupEntries) {
        for (NewsGroupEntry changedGroupEntry : selectedNewsGroupEntries) {
            NewsGroupEntry set = newsGroupEntries.get(newsGroupEntries.indexOf(changedGroupEntry));
            set.setSubscribed(changedGroupEntry.isSubscribed());
        }
    }*/

    /*    public void isNewsgroupStored(NewsGroupEntry item) throws ExecutionException, InterruptedException {
        newsGroupEntryTable.lookUp(item.getId()).get();
    }*/

/*    public SubscribedNewsgroup getSubscribedNewsgroupByName(Collection<SubscribedNewsgroup> c, String name) {
        for (SubscribedNewsgroup o : c) {
            if (o != null && o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    public NewsGroupEntry getEntryWithName(Collection<NewsGroupEntry> c, String name) {
        for (NewsGroupEntry o : c) {
            if (o != null && o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    public class NewsGroupEntryComparator implements Comparator<NewsGroupEntry> {
        @Override
        public int compare(NewsGroupEntry o1, NewsGroupEntry o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private void syncLocalWithNewsgroup() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Log.d("AzureService", "getting items from newsgroup");
                    final List<NewsGroupEntry> results = new ArrayList<>();
                    NewsGroupService service = new NewsGroupService(null);
//                    NewsGroupService service = new NewsGroupService("news.tugraz.at");
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
                                NewsGroupEntry added = addItemInTable(item, newsGroupEntryTable);
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
                                    updateItemInTable(localStored, newsGroupEntryTable);
                                    Log.d("AzureService", "changed item locally: " + localStored.getName());
                                    fireAzureEvent = true;
                                }
                            }
                        }

                        for (int entry = 0; entry < newsGroupEntries.size(); entry++) {
                            NewsGroupEntry local = newsGroupEntries.get(entry);
                            NewsGroupEntry item = getEntryWithName(results, local.getName());
                            if (item == null) {
                                deleteItemFromTable(local, newsGroupEntryTable);
                                newsGroupEntries.remove(local);
                            }
                        }
                        if (fireAzureEvent)
                            fireAzureServiceEvent(NewsGroupEntry.class, newsGroupEntries);
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
    }*/
}