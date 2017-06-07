package com.freeteam01.androidnewsgroupreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.List;


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

        if (RuntimeStorage.instance().getUserSetting() == null ||
                Strings.isNullOrEmpty(RuntimeStorage.instance().getUserSetting().getEmail()) ||
                Strings.isNullOrEmpty(RuntimeStorage.instance().getUserSetting().getForename()) ||
                Strings.isNullOrEmpty(RuntimeStorage.instance().getUserSetting().getSurname())) {
            Intent launch = new Intent(AddArticleActivity.this, SettingsActivity.class);
            startActivityForResult(launch, 0);
            finish();
        }

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getString("mode");
        server_name = bundle.getString("server");
        group = bundle.getString("group");
        et_subject_ = (EditText) findViewById(R.id.et_subject);
        et_post_ = (EditText) findViewById(R.id.et_post);

        if (mode.equals("answer")) {
            article = bundle.getString("article");
            article_subject = bundle.getString("article_subject");
            article_text = bundle.getString("article_text");
            et_subject_.setText("Re: " + article_subject);
            setAnswerText(article_text);
        }

        btn_article_send_ = (ImageButton) findViewById(R.id.btn_article_send);
        btn_article_send_.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group != null && et_post_.getText().length() != 0 && et_subject_.getText().length() != 0) {

                    Animation animation = AnimationUtils.loadAnimation(btn_article_send_.getContext(), R.anim.rotate);
                    btn_article_send_.startAnimation(animation);
                    Log.d("AAA", "send post");
                    Log.d("AAA", server_name);

                    post_text = et_post_.getText().toString();
                    post_subject = et_subject_.getText().toString();

                    if (mode.equals("post"))
                        postArticle();
                    else if (mode.equals("answer"))
                        answerArticle();
                    else
                        throw new IllegalStateException();

                    animation.setAnimationListener(new Animation.AnimationListener() {

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
                        if (service.post(RuntimeStorage.instance().getUserSetting().getForename() + " " + RuntimeStorage.instance().getUserSetting().getSurname()
                                , RuntimeStorage.instance().getUserSetting().getEmail(), post_text, post_subject, group)) {
                            Log.d("AAA", "posted");
                        }
                        service.Disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
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
                        service.answer(RuntimeStorage.instance().getUserSetting().getForename() + " "
                                        + RuntimeStorage.instance().getUserSetting().getSurname(),
                                RuntimeStorage.instance().getUserSetting().getEmail(),
                                post_text, post_subject, group, article, references);
                        service.Disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        task.execute(server);
    }

    private void setAnswerText(String article_text) {
        String[] lines = article_text.split("\n");
        String mod_text = "";
        for (String line : lines) {
            if (!line.equals("")) {
                mod_text += "> " + line + "\n";
            } else {
                mod_text += line + "\n";
            }
        }
        et_post_.setText(mod_text);
    }
}
