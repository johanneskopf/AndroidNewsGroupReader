package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_INTERNET = 0;
    SubscribedNGSpinnerAdapter spinner_adapter_;
    Spinner subscribed_newsgroups_spinner_;
    TreeViewAdapter tree_view_adapter_;
    private String selected_newsgroup_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        subscribed_newsgroups_spinner_ = (Spinner) findViewById(R.id.subscribed_newsgroups);
        spinner_adapter_ = new SubscribedNGSpinnerAdapter(this, new ArrayList<String>());
        spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscribed_newsgroups_spinner_.setAdapter(spinner_adapter_);

        ListView tree_view_expandable_list_ = (ListView) findViewById(R.id.tree_view);
        tree_view_adapter_ = new TreeViewAdapter(this, new ArrayList<String>());
        tree_view_expandable_list_.setAdapter(tree_view_adapter_);

        LoadNewsGroupsTask loader = new LoadNewsGroupsTask();
        loader.execute();

        subscribed_newsgroups_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_newsgroup_ = subscribed_newsgroups_spinner_.getItemAtPosition(position).toString();
                LoadNewsGroupsArticles article_loader = new LoadNewsGroupsArticles();
                article_loader.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selected_newsgroup_ = null;
            }

        });

        setSupportActionBar(myToolbar);
        permissionCheck();
    }

    private void permissionCheck() {
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

    private class LoadNewsGroupsTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> newsgroups = null;
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                newsgroups = new ArrayList<>(service.getAllNewsgroups());
                service.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newsgroups;
        }

        protected void onPostExecute(ArrayList<String> newsgroups) {
            spinner_adapter_.addAll(newsgroups);
            spinner_adapter_.notifyDataSetChanged();
        }
    }

    private class LoadNewsGroupsArticles extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> article_names = new ArrayList<>();
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                List<NewsGroupArticle> articles = service.getAllArticlesFromNewsgroup(selected_newsgroup_);
                for(NewsGroupArticle article: articles){
                    article_names.add(article.getSubject());
                }
                service.Disconnect();
            } catch (Exception e) {
                Log.e("LOAD_ARTICLE",Log.getStackTraceString(e));
            }
            return article_names;
        }

        protected void onPostExecute(ArrayList<String> article_names) {
            tree_view_adapter_.clear();
            tree_view_adapter_.addAll(article_names);
            tree_view_adapter_.notifyDataSetChanged();
        }
    }

    public class SubscribedNGSpinnerAdapter extends ArrayAdapter<String> {
        public SubscribedNGSpinnerAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_newsgroups, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_subscribed_newsgroup);
            tv_name.setText(newsgroup);
            return convertView;
        }
    }


    public class TreeViewAdapter extends ArrayAdapter<String> {
        public TreeViewAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup_article = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_newsgroups, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_subscribed_newsgroup);
            tv_name.setText(newsgroup_article);
            return convertView;
        }
    }

}
