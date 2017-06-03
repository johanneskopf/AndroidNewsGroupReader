package com.freeteam01.androidnewsgroupreader.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.PostActivity;
import com.freeteam01.androidnewsgroupreader.R;

import java.util.ArrayList;

public class PostViewAdapter extends ArrayAdapter<NewsGroupArticle> {
    private AppCompatActivity mainActivity;
    private ListView listView;
    private PostViewAdapter adapter;

    public PostViewAdapter(AppCompatActivity mainActivity, ListView article_list_view, Context context, ArrayList<NewsGroupArticle> articles) {
        super(context, 0, articles);
        this.mainActivity = mainActivity;
        this.listView = article_list_view;
        this.adapter = this;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        NewsGroupArticle newsgroup_article = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsgroup_post_listview, parent, false);
        }

        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_post);
        tv_name.setText(newsgroup_article.getSubjectString());
        if (!newsgroup_article.getRead()) {
            tv_name.setTypeface(null, Typeface.BOLD);
        } else if (newsgroup_article.hasUnreadChildren()) {
            tv_name.setTypeface(null, Typeface.ITALIC);
        } else {
            tv_name.setTypeface(null, Typeface.NORMAL);
        }
        TextView tv_from_date = (TextView) convertView.findViewById(R.id.tv_from_date);
        tv_from_date.setText(newsgroup_article.getAuthor().getNameString() + ", " + newsgroup_article.getDate().getDateString());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent launch = new Intent(mainActivity, PostActivity.class);
                NewsGroupArticle selected = getItem(position);
                selected.setRead(true);
                Bundle b = new Bundle();
                b.putString("server", selected.getGroup().getServer().getName());
                b.putString("group", selected.getGroup().getName());
                b.putString("article", selected.getArticleID());
                launch.putExtras(b);
                adapter.notifyDataSetChanged();
                mainActivity.startActivityForResult(launch, 0);
            }
        });

        return convertView;
    }
}
