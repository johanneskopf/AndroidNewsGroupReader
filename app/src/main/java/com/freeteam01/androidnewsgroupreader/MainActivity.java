package com.freeteam01.androidnewsgroupreader;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Adapter.NewsgroupServerSpinnerAdapter;
import com.freeteam01.androidnewsgroupreader.Adapter.PostViewAdapter;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.ModelsDatabase.SubscribedNewsgroup;
import com.freeteam01.androidnewsgroupreader.Other.ArticleSorter;
import com.freeteam01.androidnewsgroupreader.Other.ISpinnableActivity;
import com.freeteam01.androidnewsgroupreader.Other.NewsGroupSortType;
import com.freeteam01.androidnewsgroupreader.Other.SpinnerAsyncTask;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;
import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements AzureServiceEvent, ISpinnableActivity, SearchView.OnQueryTextListener {

    private static final int REQUEST_INTERNET = 0;

    Spinner subscribed_newsgroups_spinner_;
    Spinner newsgroupsserver_spinner_;
    NewsGroupSubscribedSpinnerAdapter subscribed_spinner_adapter_;
    NewsgroupServerSpinnerAdapter server_spinner_adapter_;
    ListView post_list_view_;
    PostViewAdapter post_view_adapter_;
    ProgressBar progressBar_;
    FloatingActionButton articleBtn_;
    TextView tvError_;
    Spinner sort_by_spinner_;
    SortBySpinnerAdapter sort_by_spinner_adapter_;
    String socket_error_msg_ = "";
    SearchView search_view_;
    private String selected_newsgroup_;
    private String selected_server_;
    private AtomicInteger background_jobs_count = new AtomicInteger();

    private NewsGroupSortType sortType = NewsGroupSortType.DATE;

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private Menu menu;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When request completes
        if (resultCode == RESULT_OK) {
            // Check the request code matches the one we send in the login request
            if (requestCode == AzureService.LOGIN_REQUEST_CODE_GOOGLE) {
                MobileServiceActivityResult result = AzureService.getInstance().getClient().onActivityResult(data);
                if (result.isLoggedIn()) {
                    // login succeeded
                    Log.d("AzureService", "LoginActivity - login succeeded");
                    createAndShowDialog(String.format("You are now logged in - %1$2s", AzureService.getInstance().getClient().getCurrentUser().getUserId()), "Success");
                    AzureService.getInstance().OnAuthenticated();

                    Log.d("AzureService", "MainActivity - AzureService.getInstance()");
                    AzureService.getInstance().addAzureServiceEventListener(SubscribedNewsgroup.class, this);
                    Log.d("AzureService", "MainActivity subscribed to AzureEvent");
                    if (AzureService.getInstance().isAzureServiceEventFired(SubscribedNewsgroup.class)) {
                        OnLoaded(SubscribedNewsgroup.class, AzureService.getInstance().getSubscribedNewsgroups());
                        Log.d("AzureService", "MainActivity loaded entries as AzureEvent was already fired");
                    }

                    if(menu != null)
                    {
                        showOption(R.id.action_settings);
                        showOption(R.id.action_subscribe);
                        showOption(R.id.action_logout);
                        hideOption(R.id.action_login);
                    }

//                    finish();
                } else {
                    // login failed, check the error message
                    Log.d("AzureService", "LoginActivity - login failed");
                    String errorMessage = result.getErrorMessage();
                    createAndShowDialog(errorMessage, "Error");
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        newsgroupsserver_spinner_ = (Spinner) findViewById(R.id.newsgroupsserver_spinner);
        server_spinner_adapter_ = new NewsgroupServerSpinnerAdapter(this, new ArrayList<String>());
        server_spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newsgroupsserver_spinner_.setAdapter(server_spinner_adapter_);
        progressBar_ = (ProgressBar) findViewById(R.id.progressBar);
        articleBtn_ = (FloatingActionButton) findViewById(R.id.btn_add_article);
        tvError_ = (TextView) findViewById(R.id.tv_errors);
        sort_by_spinner_ = (Spinner) findViewById(R.id.spinner_sort);
        ArrayList<String> sort = new ArrayList<>();
        sort.add("Sort by Most Recent");
        sort.add("Sort by Subject");
        sort.add("Sort by Author");
        sort_by_spinner_adapter_ = new SortBySpinnerAdapter(this, sort);
        sort_by_spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_by_spinner_.setAdapter(sort_by_spinner_adapter_);


        //        AzureService.getInstance().addAzureServiceEventListener(this);
        Log.d("AzureService", "MainActivity subscribed to AzureEvent");
//        if (AzureService.getInstance().isAzureServiceEventFired()) {
//            OnNewsgroupsLoaded(AzureService.getInstance().getNewsGroupEntries());
//            Log.d("AzureService", "MainActivity loaded entries as AzureEvent was already fired");
//        }

        if (!AzureService.isInitialized()) {
            Log.d("AzureService", "MainActivity - AzureService.Initialize(this)");
            AzureService.Initialize(this);
        }


        showNewsgroupServers();

        newsgroupsserver_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selected_server_ = newsgroupsserver_spinner_.getItemAtPosition(position).toString();
                showSubscribedNewsgroupsAndArticles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected_server_ = null;
                showSubscribedNewsgroupsAndArticles();
            }
        });

        subscribed_newsgroups_spinner_ = (Spinner) findViewById(R.id.newsgroups_spinner);
        subscribed_spinner_adapter_ = new NewsGroupSubscribedSpinnerAdapter(this, new ArrayList<String>());
        subscribed_spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscribed_newsgroups_spinner_.setAdapter(subscribed_spinner_adapter_);

        post_list_view_ = (ListView) findViewById(R.id.treeList);
        post_view_adapter_ = new PostViewAdapter(this, post_list_view_, this, new ArrayList<NewsGroupArticle>());
        post_list_view_.setAdapter(post_view_adapter_);

        subscribed_newsgroups_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d("AzureService", "MainActivity - onItemSelected - showNewsGroupArticles");
                selected_newsgroup_ = subscribed_newsgroups_spinner_.getSelectedItem().toString();
                Log.d("Article", "MainActivity - onItemSelected: " + selected_newsgroup_);
                showNewsGroupArticles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d("AzureService", "MainActivity - onNothingSelected - showNewsGroupArticles");
                selected_newsgroup_ = null;
                Log.d("Article", "MainActivity - onItemSelected: none");
                showNewsGroupArticles();
            }
        });

        sort_by_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (post_view_adapter_ != null && selected_server_ != null && selected_newsgroup_ != null) {
                    String sort_by = sort_by_spinner_.getItemAtPosition(position).toString();
                    switch (sort_by) {
                        case "Sort by Subject":
                            sortType = NewsGroupSortType.SUBJECT;
                            break;
                        case "Sort by Author":
                            sortType = NewsGroupSortType.AUTHOR;
                            break;
                        case "Sort by Most Recent":
                            sortType = NewsGroupSortType.DATE;
                            break;
                    }
                }
                post_view_adapter_.sort(new ArticleSorter(sortType));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        articleBtn_.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_newsgroup_ != null) {
                    Animation ranim = AnimationUtils.loadAnimation(articleBtn_.getContext(), R.anim.scale);
                    articleBtn_.startAnimation(ranim);

                    ranim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (isOnline() && socket_error_msg_.length() == 0) {
                                Intent launch = new Intent(MainActivity.this, AddArticleActivity.class);
                                Bundle b = new Bundle();
                                b.putString("mode", "post");
                                b.putString("server", selected_server_);
                                b.putString("group", selected_newsgroup_);
                                launch.putExtras(b);
                                tvError_.setVisibility(View.GONE);
                                tvError_.setText("");
                                startActivityForResult(launch, 0);
                            } else {
                                String error_msg = isOnline() ? socket_error_msg_ : "No Internet connection";
                                Log.d("MA animation", error_msg);
                                tvError_.setText(error_msg);
                                tvError_.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

        if (!AzureService.isInitialized()) {
            Log.d("AzureService", "MainActivity - AzureService.Initialize(this)");
            AzureService.Initialize(this);
        }

    }

    private void showNewsGroupArticles() {
        if(selected_server_ == null)
            return;
        final NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(selected_server_);
        AsyncTask<NewsGroupServer, Void, Void> task = new SpinnerAsyncTask<NewsGroupServer, Void, Void>(this) {
            @Override
            protected Void doInBackground(NewsGroupServer... params) {
                super.doInBackground(params);
                for (NewsGroupServer server : params) {
                    try {
                        socket_error_msg_ = "";
                        if (server == null)
                            return null;
                        server.reload();
                        server.reload(selected_newsgroup_);
                    } catch (SocketException e) {
                        Log.d("MA", "Connection to server timed out");
                        socket_error_msg_ = "Connection to server timed out";
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        Log.d("MA", "Unknown Host");
                        socket_error_msg_ = "Unknown Host";
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("MA", "Connection could not be established");
                        socket_error_msg_ = "Connection could not be established";
                        e.printStackTrace();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void aVoid) {
                if (selected_server_ != null && selected_newsgroup_ != null && (socket_error_msg_.length() == 0) && isOnline()) {
                    NewsGroupEntry ng = RuntimeStorage.instance().getNewsgroupServer(selected_server_).getNewsgroup(selected_newsgroup_);
                    post_view_adapter_.clear();
                    post_view_adapter_.addAll(ng.getArticles());
                    post_view_adapter_.sort(new ArticleSorter(sortType));
                    post_view_adapter_.notifyDataSetChanged();
                    tvError_.setVisibility(View.GONE);
                    tvError_.setText("");
                } else if ((socket_error_msg_.length() > 0) || !isOnline()) {
                    socket_error_msg_ = isOnline() == false ? "No Internet connection" : socket_error_msg_;
                    Log.d("MA", socket_error_msg_);
                    tvError_.setText(socket_error_msg_);
                    tvError_.setVisibility(View.VISIBLE);
                } else {
                    throw new IllegalStateException();
                }
                super.onPostExecute(aVoid);
            }
        };
        task.execute(server);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void showNewsgroupServers() {
        server_spinner_adapter_.clear();
        server_spinner_adapter_.addAll(RuntimeStorage.instance().getAllNewsgroupServers());
        server_spinner_adapter_.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        search_view_ = null;
        if (searchItem != null) {
            search_view_ = (SearchView) searchItem.getActionView();
        }
        if (search_view_ != null) {
            search_view_.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        search_view_.setSubmitButtonEnabled(true);
        search_view_.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        post_view_adapter_.getFilter().filter(newText);
        return true;
    }

    private void disableOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setEnabled(false);
    }

    private void enableOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setEnabled(true);
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    private void setOptionTitle(int id, String title) {
        MenuItem item = menu.findItem(id);
        item.setTitle(title);
    }

    private void setOptionIcon(int id, int iconRes) {
        MenuItem item = menu.findItem(id);
        item.setIcon(iconRes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(settings, 0);
                return true;
            case R.id.action_subscribe:
                Intent launch = new Intent(MainActivity.this, SubscribeActivity.class);
                startActivityForResult(launch, 0);
                return true;
            case R.id.action_logout:
                AzureService.getInstance().logout();
                hideOption(R.id.action_settings);
                hideOption(R.id.action_subscribe);
                hideOption(R.id.action_logout);
                showOption(R.id.action_login);
                createAndShowDialog("Successfully logged out", "Success");
                finish();
                System.exit(0);
                return true;
            case R.id.action_login:
                AzureService.getInstance().authenticate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void addedBackgroundJob() {
        background_jobs_count.getAndIncrement();
        setSpinnerVisibility();
    }

    @Override
    public void finishedBackgroundJob() {
        background_jobs_count.getAndDecrement();
        setSpinnerVisibility();
    }

    void setSpinnerVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (background_jobs_count.get() == 0) {
                    progressBar_.setVisibility(View.GONE);
                } else {
                    progressBar_.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public <T> void OnLoaded(Class<T> classType, List<T> entries) {
        Log.d("AzureService", "MainActivity.OnLoaded: " + classType.getSimpleName());
        if (classType == SubscribedNewsgroup.class) {
            showSubscribedNewsgroupsAndArticles();
        }
    }

    private void showSubscribedNewsgroupsAndArticles() {
        final NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(selected_server_);
        final TreeSet<String> subscribedNewsGroupEntries;
        if (server != null)
            subscribedNewsGroupEntries = server.getSubscribed();
        else
            subscribedNewsGroupEntries = null;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String copy = selected_newsgroup_;
                Log.d("Article", "MainActivity - selected_newsgroup_runOnUiThread - before: " + selected_newsgroup_);
                subscribed_spinner_adapter_.clear();
                if (subscribedNewsGroupEntries != null)
                    subscribed_spinner_adapter_.addAll(subscribedNewsGroupEntries);
                subscribed_spinner_adapter_.notifyDataSetChanged();

                if (subscribed_newsgroups_spinner_.getSelectedItem() == null)
                    selected_newsgroup_ = null;
                else
                    selected_newsgroup_ = subscribed_newsgroups_spinner_.getSelectedItem().toString();

                if (subscribedNewsGroupEntries != null) {
                    if (!subscribedNewsGroupEntries.isEmpty() && (copy != null && !subscribedNewsGroupEntries.contains(copy))) {
//                        subscribed_newsgroups_spinner_.setSelection(Adapter.NO_SELECTION);
//                        subscribed_newsgroups_spinner_.setSelection(0);
//                        selected_newsgroup_ = subscribed_newsgroups_spinner_.getSelectedItem().toString();
                        showNewsGroupArticles();
                        Log.d("Article", "MainActivity - set selected newsgroup to " + selected_newsgroup_);
                    } else if (copy == null || !subscribedNewsGroupEntries.contains(copy)) {
//                        subscribed_newsgroups_spinner_.setSelection(Adapter.NO_SELECTION);
//                        selected_newsgroup_ = null;
                        showNewsGroupArticles();
                        Log.d("Article", "MainActivity - set selected newsgroup to none");
                    }
                }
                Log.d("Article", "MainActivity - selected_newsgroup_runOnUiThread - after: " + selected_newsgroup_);
            }
        });
    }

    @Override
    protected void onResume() {
        showNewsGroupArticles();
        showSubscribedNewsgroupsAndArticles();
        showNewsgroupServers();
        super.onResume();
    }

    public class NewsGroupSubscribedSpinnerAdapter extends ArrayAdapter<String> {
        public NewsGroupSubscribedSpinnerAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_element, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_newsgroup);
            tv_name.setText(newsgroup);
            return convertView;
        }
    }


    public class SortBySpinnerAdapter extends ArrayAdapter<String> {
        public SortBySpinnerAdapter(Context context, ArrayList<String> sort) {
            super(context, 0, sort);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String sort_by = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_element, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_newsgroup);
            tv_name.setText(sort_by);
            return convertView;
        }
    }

}
