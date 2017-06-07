package com.freeteam01.androidnewsgroupreader;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Adapter.PostViewAdapter;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Other.ISpinnableActivity;
import com.freeteam01.androidnewsgroupreader.Other.SpinnerAsyncTask;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PostActivity extends AppCompatActivity implements ISpinnableActivity {

    PostViewAdapter treeViewAdapter;
    TextView articleTextTextView;
    TextView fromTextTextView;
    TextView dateTextTextView;
    TextView articleNameTextTextView;
    ListView treeListView;
    List<NewsGroupArticle> articles = new ArrayList<>();
    List<NewsGroupArticle> flat = new ArrayList<>();
    String articleText;
    private NewsGroupArticle article;
    private AtomicInteger backgroundJobsCount = new AtomicInteger();
    private ProgressBar progressBar;
    private FloatingActionButton articleButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle bundle = getIntent().getExtras();
        final String server = bundle.getString("server");
        final String group = bundle.getString("group");
        final String article = bundle.getString("article");

        this.article = RuntimeStorage.instance().getNewsgroupServer(server).getNewsgroup(group).getArticle(article);

        articles = new ArrayList<>(this.article.getChildren().values());

        treeListView = (ListView) findViewById(R.id.tree_view);
        treeViewAdapter = new PostViewAdapter(this, treeListView, this, new ArrayList<NewsGroupArticle>());
        treeListView.setAdapter(treeViewAdapter);

        articleTextTextView = (TextView) findViewById(R.id.tv_article);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        articleTextTextView.setMovementMethod(new ScrollingMovementMethod());

        fromTextTextView = (TextView) findViewById(R.id.tv_from);
        dateTextTextView = (TextView) findViewById(R.id.tv_date);
        articleNameTextTextView = (TextView) findViewById(R.id.tv_article_name);

        LoadNewsGroupsArticleText loader = new LoadNewsGroupsArticleText(this);
        loader.execute();

        treeViewAdapter.clear();
        flat.add(this.article);
        treeViewAdapter.add(this.article);
        List<NewsGroupArticle> set_list = new ArrayList<>(this.article.getChildren().values());
        setTreeElements(set_list, 1);
        treeViewAdapter.notifyDataSetChanged();

        articleButton = (FloatingActionButton) findViewById(R.id.btn_answer_article);

        articleButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PostActivity.this.article != null) {
                    Animation ranim = AnimationUtils.loadAnimation(articleButton.getContext(), R.anim.scale);
                    articleButton.startAnimation(ranim);

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
                            b.putString("article", PostActivity.this.article.getArticleID());
                            b.putString("article_text", articleText);
                            b.putString("article_subject", PostActivity.this.article.getSubjectString());
                            launch.putExtras(b);
                            startActivityForResult(launch, 0);
                        }
                    });

                }
            }
        });
    }

    public void setTreeElements(List<NewsGroupArticle> articles, int depth) {
        for (NewsGroupArticle article : articles) {
            article.setDepth(depth);
            flat.add(article);
            treeViewAdapter.add(article);
            if (article.getChildren().values().size() > 0) {
                List<NewsGroupArticle> set_list = new ArrayList<>(article.getChildren().values());
                setTreeElements(set_list, depth + 1);
            }
        }
    }

    @Override
    public void addedBackgroundJob() {
        backgroundJobsCount.getAndIncrement();
        setSpinnerVisibility();
    }

    @Override
    public void finishedBackgroundJob() {
        backgroundJobsCount.getAndDecrement();
        setSpinnerVisibility();
    }

    void setSpinnerVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (backgroundJobsCount.get() == 0) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
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
                article_text = article.getText();
            } catch (Exception e) {
                Log.e("LOAD_TEXT", Log.getStackTraceString(e));
            }
            return article_text;
        }

        protected void onPostExecute(String article_text) {
            super.onPostExecute(article_text);
            articleText = article_text;
            setFormatedArticleText(article_text);
            fromTextTextView.setText(article.getAuthor().getNameString());
            dateTextTextView.setText(article.getDate().getDateString());
            articleNameTextTextView.setText(article.getSubjectString());
        }


        void setFormatedArticleText(String articleText) {

            SpannableStringBuilder finalstring = new SpannableStringBuilder(articleText);

            formatSpanBold(finalstring);
            formatSpanUnderline(finalstring);
            formatSpanItalic(finalstring);
            formatQuote(finalstring);
            formatEmoji(finalstring);

            articleTextTextView.setText(finalstring);
        }

        // see http://apps.timwhitlock.info/emoji/tables/unicode
        private void formatEmoji(SpannableStringBuilder finalstring) {
            emojiFromTo(finalstring, ":) :-)", 0x1F603);
            emojiFromTo(finalstring, ";) ;-)", 0x1F609);
            emojiFromTo(finalstring, ":P :-P", 0x1F61C);
        }

        private void emojiFromTo(SpannableStringBuilder finalstring, String emoji, int unicode) {
            for (String e : emoji.split(" ")) {
                String emojiEscaped = Pattern.quote(e);
                Pattern boldRegex = Pattern.compile("(" + emojiEscaped + ")");
                Matcher matcher = boldRegex.matcher(finalstring);
                while (matcher.find()) {
                    finalstring.replace(matcher.start(1), matcher.end(1), getEmojiByUnicode(unicode));
                    matcher = boldRegex.matcher(finalstring);
                }
            }
        }

        private String getEmojiByUnicode(int unicode) {
            return new String(Character.toChars(unicode));
        }

        void formatQuote(SpannableStringBuilder finalstring) {
            Pattern boldRegex = Pattern.compile("^([ *>]+)(.*)$", Pattern.MULTILINE);
            Matcher matcher = boldRegex.matcher(finalstring);
            while (matcher.find()) {
                String quoteString = finalstring.subSequence(matcher.start(1), matcher.end(1)).toString();
                int counter = 0;
                for (int i = 0; i < quoteString.length(); i++) {
                    if (quoteString.charAt(i) == '>') {
                        counter++;
                    }
                }
                finalstring.setSpan(new BorderedSpan(counter), matcher.start(2), matcher.end(2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            matcher.reset();
            while (matcher.find()) {
                finalstring.delete(matcher.start(1), matcher.end(1));
                matcher = boldRegex.matcher(finalstring);
            }
        }

        void formatSpanItalic(SpannableStringBuilder finalstring) {
            Pattern italicRegex = Pattern.compile("\\/(.*?)\\/");
            Matcher matcher = italicRegex.matcher(finalstring);
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    finalstring.setSpan(new StyleSpan(Typeface.ITALIC), matcher.start(i), matcher.end(i), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        void formatSpanUnderline(SpannableStringBuilder finalstring) {
            Pattern underlineRegex = Pattern.compile("\\_(.*?)\\_");
            Matcher matcher = underlineRegex.matcher(finalstring);
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    finalstring.setSpan(new UnderlineSpan(), matcher.start(i), matcher.end(i), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        void formatSpanBold(SpannableStringBuilder finalstring) {
            Pattern boldRegex = Pattern.compile("\\*(.*?)\\*");
            Matcher matcher = boldRegex.matcher(finalstring);
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    finalstring.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(i), matcher.end(i), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        public class BorderedSpan extends ReplacementSpan {
            final Paint mPaintBorder;
            private final int levels;
            int mWidth;
            int borderWidth = 5;
            int distance = 15;

            public BorderedSpan(int levels) {
                mPaintBorder = new Paint();
                mPaintBorder.setStyle(Paint.Style.STROKE);
                mPaintBorder.setStrokeWidth(borderWidth);
                mPaintBorder.setAntiAlias(true);
                this.levels = levels;
            }


            @Override
            public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
                mWidth = (int) paint.measureText(text, start, end);
                return mWidth;
            }

            @Override
            public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
                for (int j = 0; j < levels; j++) {
                    int color = Color.RED;
                    switch (j % 5) {
                        case 0:
                            color = Color.BLUE;
                            break;
                        case 1:
                            color = Color.RED;
                            break;
                        case 2:
                            color = Color.GREEN;
                            break;
                        case 3:
                            color = Color.CYAN;
                            break;
                        case 4:
                            color = Color.MAGENTA;
                            break;
                    }
                    mPaintBorder.setColor(color);
                    canvas.drawLine(borderWidth + x + distance * j, top, borderWidth + x + distance * j, bottom, mPaintBorder);
                }
                canvas.drawText(text, start, end, x + distance * levels, y, paint);
            }
        }
    }
}
