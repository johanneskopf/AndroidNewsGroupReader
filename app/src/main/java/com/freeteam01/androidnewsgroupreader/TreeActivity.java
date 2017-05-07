package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;
import java.util.Map;

import com.freeteam01.androidnewsgroupreader.Services.AzureService;

public class TreeActivity extends AppCompatActivity{

    private static final int REQUEST_INTERNET = 0;
    TreeViewAdapter tree_view_adapter_;
    private String selected_newsgroup_;
    private NewsGroupArticle article_;
    private String parent_newsgroup_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tree_activity);
        Bundle bundle = getIntent().getExtras();
        article_ = bundle.getParcelable("article");

        ListView tree_view_expandable_list_ = (ListView) findViewById(R.id.tree_view_answers);
        tree_view_adapter_ = new TreeViewAdapter(this, new ArrayList<String>());
        tree_view_expandable_list_.setAdapter(tree_view_adapter_);

        tree_view_adapter_.clear();
        for(NewsGroupArticle article: article_.getChildren().values()) {
            tree_view_adapter_.add(article.getSubjectString());
        }
        tree_view_adapter_.notifyDataSetChanged();

        //LoadNewsGroupsArticles loader = new LoadNewsGroupsArticles();
        //loader.execute();

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

    private class LoadNewsGroupsArticles extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> article_names = new ArrayList<>();
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
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



    public class TreeViewAdapter extends ArrayAdapter<String> {
        public TreeViewAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String newsgroup_article = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_newsgroups, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_subscribed_newsgroup);
            tv_name.setText(newsgroup_article);

            Button button_post = (Button) convertView.findViewById(R.id.post);
            button_post.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Intent launch = new Intent(TreeActivity.this, PostActivity.class);
                    for (Map.Entry<String, NewsGroupArticle> entry: article_.getChildren().entrySet())
                    {
                        if(entry.getValue().getSubjectString().equals(getItem(position))) {
                            launch.putExtra("article", entry.getValue().getArticleID());
                        }
                    }
                    startActivityForResult(launch, 0);
                }
            });

            Button button_answers = (Button) convertView.findViewById(R.id.answers);
            button_answers.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Intent launch = new Intent(TreeActivity.this, TreeActivity.class);
                    Bundle b = new Bundle();
                    for (Map.Entry<String, NewsGroupArticle> entry: article_.getChildren().entrySet())
                    {
                        if(entry.getValue().getSubjectString().equals(getItem(position))) {
                            b.putParcelable("article", entry.getValue());
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
