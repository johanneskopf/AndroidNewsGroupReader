package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Adapter.PostViewAdapter;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Other.ISpinnableActivity;
import com.freeteam01.androidnewsgroupreader.Other.SpinnerAsyncTask;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class PostActivity extends AppCompatActivity implements ISpinnableActivity {

    PostViewAdapter tree_view_adapter_;
    TextView article_text_text_view_;
    TextView from_text_text_view_;
    TextView date_text_text_view_;
    TextView article_name_text_text_view_;
    ListView tree_list_view_;
    List<NewsGroupArticle> articles_ = new ArrayList<>();
    List<NewsGroupArticle> flat_ = new ArrayList<>();
    private NewsGroupArticle article_;
    private AtomicInteger background_jobs_count = new AtomicInteger();
    private ProgressBar progressBar_;
    private FloatingActionButton articleBtn_;
    String article_text_;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle bundle = getIntent().getExtras();
        final String server = bundle.getString("server");
        final String group = bundle.getString("group");
        final String article = bundle.getString("article");

        article_ = RuntimeStorage.instance().getNewsgroupServer(server).getNewsgroup(group).getArticle(article);

        articles_ = new ArrayList<>(article_.getChildren().values());

        tree_list_view_ = (ListView) findViewById(R.id.tree_view);
        tree_view_adapter_ = new PostViewAdapter(this, tree_list_view_, this, new ArrayList<NewsGroupArticle>());
        tree_list_view_.setAdapter(tree_view_adapter_);

        article_text_text_view_ = (TextView) findViewById(R.id.tv_article);
        progressBar_ = (ProgressBar) findViewById(R.id.progressBar);
        article_text_text_view_.setMovementMethod(new ScrollingMovementMethod());

        from_text_text_view_ = (TextView) findViewById(R.id.tv_from);
        date_text_text_view_ = (TextView) findViewById(R.id.tv_date);
        article_name_text_text_view_ = (TextView) findViewById(R.id.tv_article_name);

        LoadNewsGroupsArticleText loader = new LoadNewsGroupsArticleText(this);
        loader.execute();

        tree_view_adapter_.clear();
        flat_.add(article_);
        tree_view_adapter_.add(article_);
        List<NewsGroupArticle> set_list = new ArrayList<>(article_.getChildren().values());
        setTreeElements(set_list, 1);
        tree_view_adapter_.notifyDataSetChanged();

        articleBtn_ = (FloatingActionButton) findViewById(R.id.btn_answer_article);

        articleBtn_.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v){
                if(article_ != null) {
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
                            Intent launch = new Intent(PostActivity.this, AddArticleActivity.class);
                            Bundle b = new Bundle();
                            b.putString("mode", "answer");
                            b.putString("server", server);
                            b.putString("group", group);
                            b.putString("article", article_.getArticleID());
                            b.putString("article_text", article_text_);
                            b.putString("article_subject", article_.getSubjectString());
                            launch.putExtras(b);
                            startActivityForResult(launch, 0);                        }
                    });

                }
            }
        });

//        et_answer_.setCustomSelectionActionModeCallback(new StyleCallback());
    }

    public void setTreeElements(List<NewsGroupArticle> articles, int depth) {
        for (NewsGroupArticle article : articles) {
            flat_.add(article);
            tree_view_adapter_.add(article);
//            tree_view_adapter_.add(addNTimes(" ", depth) + article.getSubjectString());
            if (article.getChildren().values().size() > 0) {
                List<NewsGroupArticle> set_list = new ArrayList<>(article.getChildren().values());
                setTreeElements(set_list, depth + 1);
            }
        }
    }

    public String addNTimes(String s, int n) {
        String ret = new String();
        for (int i = 0; i < n; i++)
            ret += s;
        return ret;
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

    private class LoadNewsGroupsArticleText extends SpinnerAsyncTask<Void, Void, String> {

        public LoadNewsGroupsArticleText(ISpinnableActivity activity) {
            super(activity);
        }

        @Override
        protected String doInBackground(Void... params) {
            super.doInBackground(params);
            String article_text = null;
            try {
                article_text = article_.getText();
            } catch (Exception e) {
                Log.e("LOAD_TEXT", Log.getStackTraceString(e));
            }
            return article_text;
        }

        protected void onPostExecute(String article_text) {
            super.onPostExecute(article_text);
            article_text_text_view_.setText(article_text);
            article_text_ = article_text;
            from_text_text_view_.setText(article_.getAuthor().getNameString());
            date_text_text_view_.setText(article_.getDate().getDateString());
            article_name_text_text_view_.setText(article_.getSubjectString());
        }
    }

}
