package com.freeteam01.androidnewsgroupreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;
import java.util.List;


public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_INTERNET = 0;
    private TextView postTextView_;
    private String article_id_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);

        article_id_ = (String)getIntent().getExtras().get("article");
        postTextView_ = (TextView) findViewById(R.id.postText);

        LoadNewsGroupsArticles loader = new LoadNewsGroupsArticles();
        loader.execute();

        permissionCheck();
    }

    private void permissionCheck() {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadNewsGroupsArticles extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> article_text = new ArrayList<>();
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                article_text.add(service.getArticleText(article_id_));
                service.Disconnect();
            } catch (Exception e) {
                Log.e("LOAD_TEXT",Log.getStackTraceString(e));
            }
            return article_text;
        }

        protected void onPostExecute(ArrayList<String> article_text) {
            postTextView_.setText(article_text.get(0));
        }
    }

}
