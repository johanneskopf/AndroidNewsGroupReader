package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christian on 08.05.17.
 */

public class PostActivity extends AppCompatActivity {

    private NewsGroupArticle article_;
    PostViewAdapter tree_view_adapter_;

    TextView article_text_text_view_;

    ListView tree_list_view_;
    List<NewsGroupArticle> articles_ = new ArrayList<>();

    List<NewsGroupArticle> flat_ = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle bundle = getIntent().getExtras();
        article_ = bundle.getParcelable("article");
        articles_ = new ArrayList<>(article_.getChildren().values());

        tree_list_view_ = (ListView) findViewById(R.id.tree_view);
        tree_view_adapter_ = new PostViewAdapter(this, new ArrayList<String>());
        tree_list_view_.setAdapter(tree_view_adapter_);

        article_text_text_view_ = (TextView) findViewById(R.id.tv_article);
        article_text_text_view_.setMovementMethod(new ScrollingMovementMethod());

        LoadNewsGroupsArticleText loader = new LoadNewsGroupsArticleText();
        loader.execute();

        tree_view_adapter_.clear();
        flat_.add(article_);
        tree_view_adapter_.add(article_.getSubjectString());
        List<NewsGroupArticle> set_list = new ArrayList<>(article_.getChildren().values());
        setTreeElements(set_list, 1);
        tree_view_adapter_.notifyDataSetChanged();

    }

    public void setTreeElements(List<NewsGroupArticle> articles, int depth){
        for (NewsGroupArticle article : articles) {
            flat_.add(article);
            tree_view_adapter_.add(addNTimes(" ", depth) + article.getSubjectString());
            if(article.getChildren().values().size() > 0) {
                List<NewsGroupArticle> set_list = new ArrayList<>(article.getChildren().values());
                setTreeElements(set_list, depth + 1);
            }
        }
    }

    public String addNTimes(String s, int n){
        String ret = new String();
        for(int i = 0; i < n; i++)
            ret += s;
        return ret;
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

            tree_list_view_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("TREEVIE", "item clicked");
                    for(NewsGroupArticle article: articles_){
                        if(flat_.get(position) != null){
                            article_ = flat_.get(position);
                        }
                    }

                    LoadNewsGroupsArticleText loader = new LoadNewsGroupsArticleText();
                    loader.execute();
                }
            });

            return convertView;
        }
    }

    private class LoadNewsGroupsArticleText extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> article_text = new ArrayList<>();
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                if(article_.getArticleID() != null)
                    article_text.add(service.getArticleText(article_.getArticleID()));
                service.Disconnect();
            } catch (Exception e) {
                Log.e("LOAD_TEXT",Log.getStackTraceString(e));
            }
            return article_text;
        }

        protected void onPostExecute(ArrayList<String> article_text) {
            article_text_text_view_.setText(article_text.get(0));
        }
    }
}
