package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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
    ListView tree_list_view_;
    List<NewsGroupArticle> articles_ = new ArrayList<>();
    List<NewsGroupArticle> flat_ = new ArrayList<>();
    private NewsGroupArticle article_;
    private EditText et_answer_;
    private AtomicInteger background_jobs_count = new AtomicInteger();
    private ProgressBar progressBar_;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle bundle = getIntent().getExtras();
        String server = bundle.getString("server");
        String group = bundle.getString("group");
        String article = bundle.getString("article");

        article_ = RuntimeStorage.instance().getNewsgroupServer(server).getNewsgroup(group).getArticle(article);

        articles_ = new ArrayList<>(article_.getChildren().values());

        tree_list_view_ = (ListView) findViewById(R.id.tree_view);
        tree_view_adapter_ = new PostViewAdapter(this, tree_list_view_, this, new ArrayList<NewsGroupArticle>());
        tree_list_view_.setAdapter(tree_view_adapter_);

        article_text_text_view_ = (TextView) findViewById(R.id.tv_article);
        progressBar_ = (ProgressBar) findViewById(R.id.progressBar);
        article_text_text_view_.setMovementMethod(new ScrollingMovementMethod());

        LoadNewsGroupsArticleText loader = new LoadNewsGroupsArticleText(this);
        loader.execute();

        tree_view_adapter_.clear();
        flat_.add(article_);
        tree_view_adapter_.add(article_);
        List<NewsGroupArticle> set_list = new ArrayList<>(article_.getChildren().values());
        setTreeElements(set_list, 1);
        tree_view_adapter_.notifyDataSetChanged();

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
        }
    }

    private class StyleCallback implements android.view.ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            Log.d("FORMAT", "onCreateActionMode");
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            Log.d("FORMAT", String.format("onActionItemClicked item=%s/%d", item.toString(), item.getItemId()));
            CharacterStyle cs;
            int start = et_answer_.getSelectionStart();
            int end = et_answer_.getSelectionEnd();
            if (start == -1 || end == -1) {
                return false;
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(et_answer_.getText());
            for (StyleSpan s : ssb.getSpans(start, end, StyleSpan.class)) {
                ssb.removeSpan(s);
            }

            switch (item.getItemId()) {

                case R.id.bold:
                    cs = new StyleSpan(Typeface.BOLD);
                    ssb.setSpan(cs, start, end, 1);
                    et_answer_.setText(ssb);
                    return true;

                case R.id.italic:
                    cs = new StyleSpan(Typeface.ITALIC);
                    ssb.setSpan(cs, start, end, 1);
                    et_answer_.setText(ssb);
                    return true;

                case R.id.underline:
                    cs = new UnderlineSpan();
                    ssb.setSpan(cs, start, end, 1);
                    et_answer_.setText(ssb);
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {

        }
    }
}
