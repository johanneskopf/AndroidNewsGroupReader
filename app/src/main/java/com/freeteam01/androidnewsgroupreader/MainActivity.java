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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_INTERNET = 0;
    private List<String> subscribed_newsgroups_;

    Spinner subscribed_newsgroups_spinner_;
    SubscribedNGSpinnerAdapter spinner_adapter_;

    private String selected_newsgroup_;
    List<NewsGroupArticle> articles_ = new ArrayList<>();

    ListView post_list_view_;
    PostViewAdapter post_view_adapter_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        permissionCheck();

        if(!AzureService.isInitialized())
            AzureService.Initialize(this);
        subscribed_newsgroups_ = AzureService.getInstance().getTestData();

        subscribed_newsgroups_spinner_ = (Spinner) findViewById(R.id.newsgroups_spinner);
        spinner_adapter_ = new SubscribedNGSpinnerAdapter(this, new ArrayList<String>());
        spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscribed_newsgroups_spinner_.setAdapter(spinner_adapter_);
        spinner_adapter_.addAll(subscribed_newsgroups_);
        spinner_adapter_.notifyDataSetChanged();

        post_list_view_ = (ListView) findViewById(R.id.treeList);
        post_view_adapter_ = new PostViewAdapter(this, new ArrayList<String>());
        post_list_view_.setAdapter(post_view_adapter_);


        subscribed_newsgroups_spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_newsgroup_ = subscribed_newsgroups_spinner_.getItemAtPosition(position).toString();
                LoadSubscribedNewsGroupsArticles loader = new LoadSubscribedNewsGroupsArticles();
                loader.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selected_newsgroup_ = null;
            }

        });

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

    public class SubscribedNGSpinnerAdapter extends ArrayAdapter<String> {
        public SubscribedNGSpinnerAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.post, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_post);
            tv_name.setText(newsgroup);
            return convertView;
        }
    }

    private class LoadSubscribedNewsGroupsArticles extends AsyncTask<Object, Object, List<NewsGroupArticle>> {
        ArrayList<NewsGroupEntry> newsgroups = new ArrayList<NewsGroupEntry>();

        @Override
        protected List<NewsGroupArticle> doInBackground(Object... params) {
            //ArrayList<String> article_names = new ArrayList<>();
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                articles_ = service.getAllArticlesFromNewsgroup(selected_newsgroup_);
                service.Disconnect();
            } catch (Exception e) {
                Log.e("LOAD_ARTICLE",Log.getStackTraceString(e));
            }
            return articles_;
        }

        protected void onPostExecute(List<NewsGroupArticle> articles) {
            post_view_adapter_.clear();
            for(NewsGroupArticle article: articles){
                post_view_adapter_.add(article.getSubjectString());
            }
            post_view_adapter_.notifyDataSetChanged();
        }
    }

    public class PostViewAdapter extends ArrayAdapter<String> {
        public PostViewAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String newsgroup_article = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.post, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_post);
            tv_name.setText(newsgroup_article);

            post_list_view_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent launch = new Intent(MainActivity.this, PostActivity.class);
                    String selected = getItem(position);
                    Bundle b = new Bundle();
                    for(NewsGroupArticle article: articles_){
                        if(article.getSubjectString().equals(selected)){
                            b.putParcelable("article", article);
                            launch.putExtras(b);
                        }
                    }
                    startActivityForResult(launch, 0);
                }
            });

            return convertView;
        }
    }
}
