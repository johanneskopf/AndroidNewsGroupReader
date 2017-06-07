package com.freeteam01.androidnewsgroupreader.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.ReadArticle;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.Server;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
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

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.tableName;

public class AzureService {
    private static final String SHAREDPREFFILE = "temp";
    private static final String USERIDPREF = "uid";
    private static final String TOKENPREF = "tkn";
    public static final int LOGIN_REQUEST_CODE_GOOGLE = 1;
    private static final String URL_SCHEME = "fakenewsapptestapp";

    private static AzureService instance = null;

    private Context context = null;
    private static String mobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
    private MobileServiceClient client;

    private MobileServiceSyncTable<ReadArticle> readArticleTable;
    private MobileServiceSyncTable<Server> serverTable;
    private MobileServiceSyncTable<SubscribedNewsgroup> subscribedNewsgroupTable;
    private MobileServiceSyncTable<UserSetting> userSettingTable;

    private List<ReadArticle> readArticles;
    private List<Server> servers;
    private List<SubscribedNewsgroup> subscribedNewsgroups;
    private List<UserSetting> userSettings;

    private Map<Class<?>, List<AzureServiceEvent>> azureServiceEventListeners;
    private Map<Class<?>, Boolean> azureServiceEventFired;

    private AzureService(Context context, MobileServiceClient client) {
        this.context = context;
        this.client = client;
        azureServiceEventListeners = new HashMap<>();
        azureServiceEventFired = new HashMap<>();

        subscribedNewsgroups = new ArrayList<>();
        readArticles = new ArrayList<>();
        servers = new ArrayList<>();
        userSettings = new ArrayList<>();
    }

    public static void Initialize(Context context, MobileServiceClient client) {
        if (instance == null) {
            instance = new AzureService(context, client);
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

    public static MobileServiceClient createClient(Context context) {
        try {
            return new MobileServiceClient(mobileBackendUrl, context);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
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

    public UserSetting getUserSetting() {
        if (userSettings.size() == 0)
            return null;
        else
            return userSettings.get(0);
    }

    public void OnAuthenticated() {
        // only execute this if the login was successful

        cacheUserToken(client.getCurrentUser());
        readArticleTable = client.getSyncTable("ReadArticle", ReadArticle.class);
        serverTable = client.getSyncTable("Server", Server.class);
        subscribedNewsgroupTable = client.getSyncTable("SubscribedNewsgroup", SubscribedNewsgroup.class);
        userSettingTable = client.getSyncTable("UserSetting", UserSetting.class);
        try {
            initLocalStore(new LocalStoreCallback());
        } catch (InterruptedException | ExecutionException | MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }
    }

    public void onInitLocalStore() {
        loadLocalData();
    }

    public <T> boolean isAzureServiceEventFired(Class<T> classType) {
        if (!azureServiceEventFired.containsKey(classType))
            return false;
        return azureServiceEventFired.get(classType);
    }

    public <T> void setAzureServiceEventFired(Class<T> classType, boolean fired) {
        azureServiceEventFired.put(classType, fired);
    }

    public void authenticate() {
        if (loadUserTokenCache(client)) {
            OnAuthenticated();
        } else {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("access_type", "offline");
            client.login("Google", URL_SCHEME, LOGIN_REQUEST_CODE_GOOGLE, parameters);
        }
    }

    private boolean loadUserTokenCache(MobileServiceClient client) {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, null);
        if (userId == null)
            return false;
        String token = prefs.getString(TOKENPREF, null);
        if (token == null)
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);

        return true;
    }

    private void cacheUserToken(MobileServiceUser user) {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (user == null) {
            editor.putString(USERIDPREF, null);
            editor.putString(TOKENPREF, null);
        } else {
            editor.putString(USERIDPREF, user.getUserId());
            editor.putString(TOKENPREF, user.getAuthenticationToken());
        }
        editor.commit();
    }

    private void loadLocalData() {
        try {
            final List<SubscribedNewsgroup> storedSubscribedNewsgroupEntries = getLocalData(SubscribedNewsgroup.class, subscribedNewsgroupTable);
            subscribedNewsgroups.clear();
            subscribedNewsgroups.addAll(storedSubscribedNewsgroupEntries);

            RuntimeStorage.instance().setNewsgroups(storedSubscribedNewsgroupEntries);

            final List<Server> storedServerEntries = getLocalData(Server.class, serverTable);
            servers.clear();
            servers.addAll(storedServerEntries);
            final List<ReadArticle> storedReadArticleEntries = getLocalData(ReadArticle.class, readArticleTable);
            readArticles.clear();
            readArticles.addAll(storedReadArticleEntries);
            RuntimeStorage.instance().setReadArticles(storedReadArticleEntries);
            final List<UserSetting> storedUserSettingEntries = getLocalData(UserSetting.class, userSettingTable);
            userSettings.clear();
            userSettings.addAll(storedUserSettingEntries);
            if (userSettings.size() > 0)
                RuntimeStorage.instance().setUserSetting(userSettings.get(0));
            Log.d("AzureService", "loaded data from local storage");
        } catch (final Exception e) {
            Log.d("AzureService", "loadLocalData: " + e.getMessage());
        }
        Log.d("AzureService", "loaded data from local storage");


        // TODO: try to solve it with this runOnUiThread
/*        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });*/
        fireAzureServiceEvents();


        syncLocalWithRemote();
    }

    private void fireAzureServiceEvents() {
        fireAzureServiceEvent(SubscribedNewsgroup.class, subscribedNewsgroups);
        fireAzureServiceEvent(Server.class, servers);
        fireAzureServiceEvent(ReadArticle.class, readArticles);
        fireAzureServiceEvent(UserSetting.class, userSettings);
    }

    private void syncLocalWithRemote() {

        try {
            Log.d("AzureService", "START: sync items with remote");
            final List<SubscribedNewsgroup> storedSubscribedNewsgroupEntries = syncData(SubscribedNewsgroup.class, subscribedNewsgroupTable);
            Log.d("AzureServiceItems", "got " + storedSubscribedNewsgroupEntries.size() + " storedSubscribedNewsgroupEntries");
            for (SubscribedNewsgroup sng : storedSubscribedNewsgroupEntries) {
                Log.d("AzureServiceItems", sng.toString());
            }
            subscribedNewsgroups.clear();
            subscribedNewsgroups.addAll(storedSubscribedNewsgroupEntries);
            RuntimeStorage.instance().setNewsgroups(storedSubscribedNewsgroupEntries);
            final List<Server> storedServerEntries = syncData(Server.class, serverTable);
            servers.clear();
            servers.addAll(storedServerEntries);
            final List<ReadArticle> storedReadArticleEntries = syncData(ReadArticle.class, readArticleTable);
            readArticles.clear();
            readArticles.addAll(storedReadArticleEntries);
            RuntimeStorage.instance().setReadArticles(storedReadArticleEntries);
            final List<UserSetting> storedUserSettingEntries = syncData(UserSetting.class, userSettingTable);
            userSettings.clear();
            userSettings.addAll(storedUserSettingEntries);
            Log.d("AzureService", "synced items with remote");
//                    fireAzureServiceEvent(newsGroupEntries);
        } catch (final Exception e) {
            Log.d("AzureService", "syncLocalWithRemote: " + e.getMessage());
        }

        // TODO: try to solve it with this runOnUiThread
/*        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });*/
        fireAzureServiceEvents();
    }

    public <T> void addAzureServiceEventListener(Class<T> classType, AzureServiceEvent listener) {
        if (azureServiceEventListeners.containsKey(classType)) {
            List<AzureServiceEvent> list = azureServiceEventListeners.get(classType);
            list.add(listener);
        } else {
            List<AzureServiceEvent> list = new ArrayList<>();
            list.add(listener);
            azureServiceEventListeners.put(classType, list);
        }
        Log.d("AzureService", "Added listener for type: " + classType.getSimpleName());
    }

    private <T> void fireAzureServiceEvent(Class<T> classType, List<T> entries) {
        Log.d("AzureService", "fireAzureServiceEvent of type: " + classType.getSimpleName() + ", because " + entries.size() + " got updated");
        if (azureServiceEventListeners != null && !azureServiceEventListeners.isEmpty()) {
            List<AzureServiceEvent> list = azureServiceEventListeners.get(classType);
            if (list == null)
                return;
            for (AzureServiceEvent e : list) {
                Log.d("AzureService", " - fired for a listener");
                e.OnLoaded(classType, entries);
            }
        }
        setAzureServiceEventFired(classType, true);
    }

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
        Query query = tableName(classType.getSimpleName()).field("userId").eq(getClient().getCurrentUser().getUserId()).orderBy("name", QueryOrder.Ascending);
        return table.read(query).get();
    }

    public <T> List<T> syncData(Class<T> classType, MobileServiceSyncTable<T> table) throws ExecutionException, InterruptedException {
        MobileServiceSyncContext syncContext = client.getSyncContext();
        syncContext.push().get();
        table.pull(null).get();
        Log.d("AzureService", "syncData: " + classType.getSimpleName());
        Query query = tableName(classType.getSimpleName()).field("userId").eq(getClient().getCurrentUser().getUserId()).orderBy("name", QueryOrder.Ascending);
        return table.read(query).get();
    }

    private void initLocalStore(final ILocalStoreCallback function) throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {
        try {
            MobileServiceSyncContext syncContext = client.getSyncContext();
            if (syncContext.isInitialized())
                return;

            int databaseVersion = 1;
            String databaseName = "OfflineStore";
            SQLiteLocalStore localStore = new SQLiteLocalStore(client.getContext(), databaseName, null, databaseVersion);

            Map<String, ColumnDataType> tableDefinition = new HashMap<>();
            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("userId", ColumnDataType.String);
            tableDefinition.put("articleId", ColumnDataType.String);
            localStore.defineTable("ReadArticle", tableDefinition);

            tableDefinition = new HashMap<>();
            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("userId", ColumnDataType.String);
            tableDefinition.put("name", ColumnDataType.String);
            tableDefinition.put("url", ColumnDataType.String);
            localStore.defineTable("Server", tableDefinition);

            tableDefinition = new HashMap<>();
            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("userId", ColumnDataType.String);
            tableDefinition.put("serverId", ColumnDataType.String);
            tableDefinition.put("name", ColumnDataType.String);
            localStore.defineTable("SubscribedNewsgroup", tableDefinition);

            tableDefinition = new HashMap<>();
            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("userId", ColumnDataType.String);
            tableDefinition.put("email", ColumnDataType.String);
            tableDefinition.put("forename", ColumnDataType.String);
            tableDefinition.put("surname", ColumnDataType.String);
            localStore.defineTable("UserSetting", tableDefinition);

            SimpleSyncHandler handler = new SimpleSyncHandler();
            ListenableFuture<Void> ret = syncContext.initialize(localStore, handler);
            ret.get();

            function.callback();
        } catch (final Exception e) {
//            Log.d("AzureService", "initLocalStore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void persistSubscribedNewsgroups(final List<NewsGroupEntry> subscribedNewsgroupEntries) {
        try {
            for (NewsGroupEntry changedEntry : subscribedNewsgroupEntries) {
                SubscribedNewsgroup subscribedNewsgroup = new SubscribedNewsgroup(getClient().getCurrentUser().getUserId(), changedEntry.getServer().getName(), changedEntry.getName());
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
            }
            subscribedNewsgroupEntries.clear();
            // TODO check if loadLocalData is good here
            loadLocalData();
            fireAzureServiceEvent(SubscribedNewsgroup.class, subscribedNewsgroups);
            Log.d("AzureService", "subscribedNewsgroupEntries successful");
        } catch (ExecutionException | InterruptedException e) {
            Log.d("AzureService", "subscribedNewsgroupEntries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> void persist(final Class<T> classType, final T entry, final List<T> localEntries, final MobileServiceSyncTable<T> table) {
        try {
            if (localEntries.contains(entry)) {
                int index = localEntries.indexOf(entry);
                Log.d("AzureService", "Updating entry: " + entry.toString());
                updateItemInTable(entry, table);
                localEntries.set(index, entry);
            } else {
                Log.d("AzureService", "Adding entry: " + entry.toString());
                T addedEntry = addItemInTable(entry, table);
                localEntries.add(addedEntry);
            }
//                    fireAzureServiceEvent(classType, entries);
            Log.d("AzureService", "Entry of type " + classType + " persisted successfully");
        } catch (ExecutionException | InterruptedException e) {
            Log.d("AzureService", "persist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> void persistSingle(final Class<T> classType, final T entry, final List<T> localEntries, final MobileServiceSyncTable<T> table) {
        try {
            if (localEntries.size() > 0) {
//                        T updateEntry = localEntries.get(0);
                Log.d("AzureService", "Updating entry: " + entry.toString());
                updateItemInTable(entry, table);
                localEntries.set(0, entry);
            } else {
                Log.d("AzureService", "Adding entry: " + entry.toString());
                T addedEntry = addItemInTable(entry, table);
                Log.d("AzureService", "Added entry: " + addedEntry.toString());
                localEntries.add(addedEntry);
            }
//                    fireAzureServiceEvent(classType, entries);
            Log.d("AzureService", "Entry of type " + classType + " persisted successfully");
        } catch (ExecutionException | InterruptedException e) {
            Log.d("AzureService", "persist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void persist(UserSetting entry) {
        String userId = client.getCurrentUser().getUserId();
        entry.setUserId(userId);
        RuntimeStorage.instance().setUserSetting(entry);
        if (userSettings.size() > 0) {
            entry.setId(userSettings.get(0).getId());
            Log.d("AzureService", "Entry values: " + entry.toString());
            Log.d("AzureService", "Loaded entry values: " + userSettings.get(0).toString());
            Log.d("AzureService", "Size: " + userSettings.size());
        }
        persistSingle(UserSetting.class, entry, userSettings, userSettingTable);
    }

    public void persist(ReadArticle entry) {
        String userId = client.getCurrentUser().getUserId();
        entry.setUserId(userId);
        persist(ReadArticle.class, entry, readArticles, readArticleTable);
    }

    public void persist(Server entry) {
        String userId = client.getCurrentUser().getUserId();
        entry.setUserId(userId);
        persist(Server.class, entry, servers, serverTable);
    }

    @SuppressWarnings("deprecation")
    public void logout() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                // a callback which is executed when the cookies have been removed
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                    Log.d("AzureService", "Cookie removed: " + aBoolean);
                }
            });
        } else cookieManager.removeAllCookie();
        cacheUserToken(null);
        client.logout();
    }

    public interface ILocalStoreCallback {
        void callback();
    }

    public class LocalStoreCallback implements ILocalStoreCallback {
        @Override
        public void callback() {
            onInitLocalStore();
        }
    }

    public void readArticleChanged(NewsGroupArticle newsGroupArticle) {
        if (newsGroupArticle.getRead()) {
            persist(new ReadArticle(newsGroupArticle.getArticleID(), getClient().getCurrentUser().getUserId()));
        }
    }
}