package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
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

import static android.R.attr.level;


public class PostActivity extends AppCompatActivity implements ISpinnableActivity {

    PostViewAdapter tree_view_adapter_;
    TextView article_text_text_view_;
    TextView from_text_text_view_;
    TextView date_text_text_view_;
    TextView article_name_text_text_view_;
    ListView tree_list_view_;
    List<NewsGroupArticle> articles_ = new ArrayList<>();
    List<NewsGroupArticle> flat_ = new ArrayList<>();
    String article_text_;
    private NewsGroupArticle article_;
    private AtomicInteger background_jobs_count = new AtomicInteger();
    private ProgressBar progressBar_;
    private FloatingActionButton articleBtn_;

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
            public void onClick(View v) {
                if (article_ != null) {
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
                            startActivityForResult(launch, 0);
                        }
                    });

                }
            }
        });

//        et_answer_.setCustomSelectionActionModeCallback(new StyleCallback());
    }

    public void setTreeElements(List<NewsGroupArticle> articles, int depth) {
        for (NewsGroupArticle article : articles) {
            article.setDepth(depth);
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
//            article_text_text_view_.setText(article_text);
            article_text_ = article_text;
            setFormatedArticleText(article_text);
            from_text_text_view_.setText(article_.getAuthor().getNameString());
            date_text_text_view_.setText(article_.getDate().getDateString());
            article_name_text_text_view_.setText(article_.getSubjectString());
        }


        void setFormatedArticleText(String articleText) {

            SpannableStringBuilder finalstring = new SpannableStringBuilder(articleText);

            formatSpanBold(finalstring);
            formatSpanUnderline(finalstring);
            formatSpanItalic(finalstring);
            formatQuote(finalstring);
            formatEmoji(finalstring);


            article_text_text_view_.setText(finalstring);
        }
        //http://apps.timwhitlock.info/emoji/tables/unicode
        private void formatEmoji(SpannableStringBuilder finalstring) {
            emojiFromTo(finalstring, ":) :-)", 0x1F603);
            emojiFromTo(finalstring, ";) ;-)", 0x1F609);
            emojiFromTo(finalstring, ":P :-P", 0x1F61C);
        }

        private void emojiFromTo(SpannableStringBuilder finalstring, String emoji, int unicode)
        {
            for(String e : emoji.split(" "))
            {
                String emojiEscaped = Pattern.quote(e);
                Pattern boldRegex = Pattern.compile("(" +emojiEscaped+")");
                Matcher matcher = boldRegex.matcher(finalstring);
                while (matcher.find()) {
                    finalstring.replace(matcher.start(1), matcher.end(1), getEmojiByUnicode(unicode));
                    matcher = boldRegex.matcher(finalstring);
                }
            }
        }

        private String getEmojiByUnicode(int unicode){
            return new String(Character.toChars(unicode));
        }

        void formatQuote(SpannableStringBuilder finalstring) {
            Pattern boldRegex = Pattern.compile("^(>+)(.*)$", Pattern.MULTILINE);
            Matcher matcher = boldRegex.matcher(finalstring);
            while (matcher.find()) {
                finalstring.setSpan(new BorderedSpan(matcher.end(1) - matcher.start(1)), matcher.start(2), matcher.end(2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
