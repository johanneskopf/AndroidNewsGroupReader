package com.freeteam01.androidnewsgroupreader.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.PostActivity;
import com.freeteam01.androidnewsgroupreader.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class PostViewAdapter extends ArrayAdapter<NewsGroupArticle> implements Filterable {
    private AppCompatActivity mainActivity;
    private ListView listView;
    private PostViewAdapter adapter;
    private FriendFilter friendFilter;
    private ArrayList<NewsGroupArticle> items;
    private ArrayList<NewsGroupArticle> filtered;
    private Comparator<? super NewsGroupArticle> currentComperator;

    public PostViewAdapter(AppCompatActivity mainActivity, ListView article_list_view, Context context, ArrayList<NewsGroupArticle> articles) {
        super(context, 0, articles);
        this.mainActivity = mainActivity;
        this.listView = article_list_view;
        this.adapter = this;
        this.filtered = articles;
        this.items = (ArrayList<NewsGroupArticle>) filtered.clone();
    }

    @Override
    public void add(@Nullable NewsGroupArticle object) {
        items.add(object);
        super.add(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends NewsGroupArticle> collection) {
        items.addAll(collection);
        super.addAll(collection);
    }

    @Override
    public void addAll(NewsGroupArticle... items) {
        super.addAll(items);
    }

    @Override
    public void clear() {
        items.clear();
        super.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        NewsGroupArticle newsgroup_article = filtered.get(position);

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

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Override
    public void sort(@NonNull Comparator<? super NewsGroupArticle> comparator) {
        currentComperator = comparator;
        Collections.sort(filtered, comparator);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (friendFilter == null) {
            friendFilter = new FriendFilter();
        }

        return friendFilter;
    }

    private class FriendFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<NewsGroupArticle> tempList = new ArrayList<NewsGroupArticle>();

                for (NewsGroupArticle article : items) {
                    if (article.getSubjectString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(article);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = items.size();
                ArrayList<NewsGroupArticle> temp = new ArrayList<>();
                temp.addAll(items);
                filterResults.values = temp;
            }

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (ArrayList<NewsGroupArticle>) results.values;
            Collections.sort(filtered, currentComperator);

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
