package com.freeteam01.androidnewsgroupreader;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.io.IOException;
import java.util.List;

/**
 * Created by christian on 31.05.17.
 */

public class AddArticleActivity extends AppCompatActivity {

    private EditText et_post_;
    private EditText et_subject_;
    private ImageButton btn_article_send_;
    private String mode;
    private String server_name;
    private String group;
    private String article = "";
    private String article_subject = "";
    private String article_text = "";
    private String post_text;
    private String post_subject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getString("mode");
        server_name = bundle.getString("server");
        group = bundle.getString("group");
        et_subject_ = (EditText) findViewById(R.id.et_subject);
        et_post_ = (EditText) findViewById(R.id.et_post);

        if(mode.equals("answer")) {
            article = bundle.getString("article");
            article_subject = bundle.getString("article_subject");
            article_text = bundle.getString("article_text");
            et_subject_.setText("Re: " + article_subject);
            setAnswerText(article_text);
        }

        btn_article_send_ = (ImageButton) findViewById(R.id.btn_article_send);
        btn_article_send_.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v){
                if(group != null && et_post_.getText().length() != 0 && et_subject_.getText().length() != 0) {

                    Animation ranim = AnimationUtils.loadAnimation(btn_article_send_.getContext(), R.anim.rotate);
                    btn_article_send_.startAnimation(ranim);
                    Log.d("AAA", "send post");
                    Log.d("AAA", server_name);

                    post_text = et_post_.getText().toString();
                    post_subject = et_subject_.getText().toString();

                    if(mode.equals("post"))
                        postArticle();
                    else if(mode.equals("answer"))
                        answerArticle();
                    else
                        throw new IllegalStateException();

                    ranim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Intent launch;

                            if (mode.equals("answer")) {
                                launch = new Intent(AddArticleActivity.this, PostActivity.class);
                                Bundle b = new Bundle();
                                b.putString("server", server_name);
                                b.putString("group", group);
                                b.putString("article", article);
                                launch.putExtras(b);
                            } else {
                                launch = new Intent(AddArticleActivity.this, MainActivity.class);
                            }

                            startActivityForResult(launch, 0);
                        }
                    });

                }
            }
        });

    }

    private void postArticle() {
        final NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(server_name);
        final NewsGroupService service = new NewsGroupService(server);
        AsyncTask<NewsGroupServer, Void, Void> task = new AsyncTask<NewsGroupServer, Void, Void>() {
            @Override
            protected Void doInBackground(NewsGroupServer... params) {
                for (NewsGroupServer server : params) {
                    try {
                        service.Connect();
                        if(service.post("FakeNews", "a@a.com", post_text, post_subject, group)){
                            Log.d("AAA", "posted");
                        }
                        service.Disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                /*post_view_adapter_.clear();
                NewsGroupEntry ng = RuntimeStorage.instance().getNewsgroupServer(selected_server_).getNewsgroup(selected_newsgroup_);
                post_view_adapter_.addAll(ng.getArticles());
                post_view_adapter_.notifyDataSetChanged();
                super.onPostExecute(aVoid);*/
            }
        };
        task.execute(server);
    }

    private void answerArticle() {
        final NewsGroupServer server = RuntimeStorage.instance().getNewsgroupServer(server_name);
        final List<String> references = server.getNewsgroup(group).getArticle(article).getReferences();
        final NewsGroupService service = new NewsGroupService(server);
        AsyncTask<NewsGroupServer, Void, Void> task = new AsyncTask<NewsGroupServer, Void, Void>() {
            @Override
            protected Void doInBackground(NewsGroupServer... params) {
                for (NewsGroupServer server : params) {
                    try {
                        service.Connect();
                        //TODO insert user credentials
                        if(service.answer("FakeNews", "a@a.com", post_text, post_subject, group, article, references)){
                            Log.d("AAA", "answered");
                        }
                        service.Disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                /*post_view_adapter_.clear();
                NewsGroupEntry ng = RuntimeStorage.instance().getNewsgroupServer(selected_server_).getNewsgroup(selected_newsgroup_);
                post_view_adapter_.addAll(ng.getArticles());
                post_view_adapter_.notifyDataSetChanged();
                super.onPostExecute(aVoid);*/
            }
        };
        task.execute(server);
    }

    private void setAnswerText(String article_text){
        String[] lines = article_text.split("\n");
        String mod_text = "";
        for(String line: lines){
            if(!line.equals("")){
                mod_text += "> " + line + "\n";
            }
            else{
                mod_text += line + "\n";
            }
        }
        et_post_.setText(mod_text);
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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d("FORMAT", String.format("onActionItemClicked item=%s/%d", item.toString(), item.getItemId()));
            CharacterStyle cs;
            int start = et_post_.getSelectionStart();
            int end = et_post_.getSelectionEnd();
            if (start == -1 || end == -1) {
                return false;
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(et_post_.getText());
            for (StyleSpan s : ssb.getSpans(start, end, StyleSpan.class)) {
                ssb.removeSpan(s);
            }

            switch (item.getItemId()) {

                case R.id.bold:
                    cs = new StyleSpan(Typeface.BOLD);
                    ssb.setSpan(cs, start, end, 1);
                    et_post_.setText(ssb);
                    return true;

                case R.id.italic:
                    cs = new StyleSpan(Typeface.ITALIC);
                    ssb.setSpan(cs, start, end, 1);
                    et_post_.setText(ssb);
                    return true;

                case R.id.underline:
                    cs = new UnderlineSpan();
                    ssb.setSpan(cs, start, end, 1);
                    et_post_.setText(ssb);
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {

        }
    }
}
