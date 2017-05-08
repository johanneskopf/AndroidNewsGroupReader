package com.freeteam01.androidnewsgroupreader;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;


public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_INTERNET = 0;
    private TextView postTextView_;
    private String article_id_;
    private EditText et_answer_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);

        article_id_ = (String) getIntent().getExtras().get("article");
        postTextView_ = (TextView) findViewById(R.id.postText);
        et_answer_ = (EditText) findViewById(R.id.et_answer);

        LoadNewsGroupsArticles loader = new LoadNewsGroupsArticles();
        loader.execute();

        postTextView_.setMovementMethod(new ScrollingMovementMethod());


        postTextView_.setCustomSelectionActionModeCallback(new StyleCallback());

        et_answer_.setCustomSelectionActionModeCallback(new StyleCallback());
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
                Log.e("LOAD_TEXT", Log.getStackTraceString(e));
            }
            return article_text;
        }

        protected void onPostExecute(ArrayList<String> article_text) {
            postTextView_.setText(article_text.get(0));
        }
    }

    /*
    *
    * adapted from http://stackoverflow.com/questions/12995439/custom-cut-copy-action-bar-for-edittext-that-shows-text-selection-handles
    *
    */

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
            for(StyleSpan s : ssb.getSpans(start, end, StyleSpan.class))
            {
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
