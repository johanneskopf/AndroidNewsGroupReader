package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.freeteam01.androidnewsgroupreader.Other.ISpinnableActivity;
import com.freeteam01.androidnewsgroupreader.Other.SpinnerAsyncTask;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements AzureServiceEvent, ISpinnableActivity {

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
    String socket_error_msg_ = "";
    private String selected_newsgroup_;
    private String selected_server_;
    private AtomicInteger background_jobs_count = new AtomicInteger();

    @Override
    public void onStart() {
        super.onStart();

        // refresh shown data
/*        subscribed_spinner_adapter_.clear();*/
/*        if(subscribed_newsgroups_ != null)
        {
            subscribed_spinner_adapter_.addAll(subscribed_newsgroups_);
            subscribed_spinner_adapter_.notifyDataSetChanged();
        }*/

        /*if (AzureService.getInstance().isAzureServiceEventFired()) {
            OnNewsgroupsLoaded(AzureService.getInstance().getNewsGroupEntries());
            Log.d("AzureService", "MainActivity loaded entries as AzureEvent was already fired");
        }*/ /*else {
            AzureService.getInstance().addAzureServiceEventListener(this);
            Log.d("AzureService", "MainActivity subscribed to AzureEvent");
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

//        if (!AzureService.isInitialized())
//            AzureService.Initialize(this);

        newsgroupsserver_spinner_ = (Spinner) findViewById(R.id.newsgroupsserver_spinner);
        server_spinner_adapter_ = new NewsgroupServerSpinnerAdapter(this, new ArrayList<String>());
        server_spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newsgroupsserver_spinner_.setAdapter(server_spinner_adapter_);
        progressBar_ = (ProgressBar) findViewById(R.id.progressBar);
        articleBtn_ = (FloatingActionButton) findViewById(R.id.btn_add_article);
        tvError_ = (TextView) findViewById(R.id.tv_errors);
        showNewsgroupServers();

        newsgroupsserver_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_server_ = newsgroupsserver_spinner_.getItemAtPosition(position).toString();
                ShowSubscribedNewsgroups();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected_server_ = null;
                ShowSubscribedNewsgroups();
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
                selected_newsgroup_ = subscribed_newsgroups_spinner_.getItemAtPosition(position).toString();
                showNewGroupArticles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selected_newsgroup_ = null;
                showNewGroupArticles();
            }
        });

        articleBtn_.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v){
                if(selected_newsgroup_ != null) {
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
                            if(isOnline() && socket_error_msg_.length() == 0) {
                                Intent launch = new Intent(MainActivity.this, AddArticleActivity.class);
                                Bundle b = new Bundle();
                                b.putString("mode", "post");
                                b.putString("server", selected_server_);
                                b.putString("group", selected_newsgroup_);
                                launch.putExtras(b);
                                tvError_.setVisibility(View.INVISIBLE);
                                tvError_.setText("");
                                startActivityForResult(launch, 0);
                            }
                            else{
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

//        AzureService.getInstance().addAzureServiceEventListener(this);
        Log.d("AzureService", "MainActivity subscribed to AzureEvent");
//        if (AzureService.getInstance().isAzureServiceEventFired()) {
//            OnNewsgroupsLoaded(AzureService.getInstance().getNewsGroupEntries());
//            Log.d("AzureService", "MainActivity loaded entries as AzureEvent was already fired");
//        }
    }


    private void showNewGroupArticles() {
        final NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(selected_server_);
        AsyncTask<NewsGroupServer, Void, Void> task = new SpinnerAsyncTask<NewsGroupServer, Void, Void>(this) {
            @Override
            protected Void doInBackground(NewsGroupServer... params) {
                super.doInBackground(params);
                for (NewsGroupServer server : params) {
                    try {
                        socket_error_msg_ = "";
                        server.reload();
                        server.reload(selected_newsgroup_);
                    } catch (SocketException e) {
                        Log.d("MA", "Connection to server timed out");
                        socket_error_msg_ = "Connection to server timed out";
                        e.printStackTrace();
                    } catch (UnknownHostException e){
                        Log.d("MA", "Unknown Host");
                        socket_error_msg_ = "Unknown Host";
                        e.printStackTrace();
                    } catch (IOException e){
                        Log.d("MA", "Connection could not be established");
                        socket_error_msg_ = "Connection could not be established";
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                post_view_adapter_.clear();
                NewsGroupEntry ng = RuntimeStorage.instance().getNewsgroupServer(selected_server_).getNewsgroup(selected_newsgroup_);

                if((socket_error_msg_.length() == 0) && isOnline()) {
                    post_view_adapter_.addAll(ng.getArticles());
                    post_view_adapter_.notifyDataSetChanged();
                    tvError_.setVisibility(View.INVISIBLE);
                    tvError_.setText("");;
                }
                else if((socket_error_msg_.length() > 0) || !isOnline()){
                    socket_error_msg_ = isOnline() == false ? "No Internet connection" : socket_error_msg_;
                    Log.d("MA", socket_error_msg_);
                    tvError_.setText(socket_error_msg_);
                    tvError_.setVisibility(View.VISIBLE);
                }
                else{
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
    public void OnNewsgroupsLoaded(List<NewsGroupEntry> newsGroupEntries) {
        ShowSubscribedNewsgroups();
    }


    private void ShowSubscribedNewsgroups() {
        NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(selected_server_);
        final List<String> subscribedNewsGroupEntries = server.getSubscribed();
        subscribed_spinner_adapter_.clear();
        subscribed_spinner_adapter_.addAll(subscribedNewsGroupEntries);
        subscribed_spinner_adapter_.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_subscribe:
                Intent launch = new Intent(MainActivity.this, SubscribeActivity.class);
                startActivityForResult(launch, 0);
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

    public class NewsGroupSubscribedSpinnerAdapter extends ArrayAdapter<String> {
        public NewsGroupSubscribedSpinnerAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsgroup_post_listview, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_post);
            tv_name.setText(newsgroup);
            return convertView;
        }
    }

}
