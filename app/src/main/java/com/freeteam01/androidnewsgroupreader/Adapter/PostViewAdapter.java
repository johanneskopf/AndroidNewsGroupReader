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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.PostActivity;
import com.freeteam01.androidnewsgroupreader.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PostViewAdapter extends ArrayAdapter<NewsGroupArticle> implements Filterable{
    private AppCompatActivity mainActivity;
    private ListView listView;
    private PostViewAdapter adapter;
    private FriendFilter friend_filter;
    private ArrayList<NewsGroupArticle> articles_shown_;
    private Set<NewsGroupArticle> search_set_ = new HashSet<>();
    private Set<NewsGroupArticle> shown_ = new HashSet<>();

    public PostViewAdapter(AppCompatActivity mainActivity, ListView article_list_view, Context context, ArrayList<NewsGroupArticle> articles) {
        super(context, 0, articles);
        this.mainActivity = mainActivity;
        this.listView = article_list_view;
        this.articles_shown_ = articles;
        this.adapter = this;
        this.search_set_.addAll(articles);
    }

    public void addData(Collection<NewsGroupArticle> data){
        adapter.addAll(data);
        search_set_.clear();
        shown_.clear();
        search_set_.addAll(data);
    }

    public void delete(){
        adapter.clear();
        search_set_.clear();
    }

    public Set<NewsGroupArticle> getShown(){
        return shown_;
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

    @Override
    public Filter getFilter() {
        if (friend_filter == null) {
            friend_filter = new FriendFilter();
        }

        return friend_filter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class FriendFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<NewsGroupArticle> tempList = new ArrayList<NewsGroupArticle>();

                // search content in friend list
                for (NewsGroupArticle article : search_set_) {
                    if (article.getSubjectString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(article);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = search_set_.size();
                ArrayList<NewsGroupArticle> temp = new ArrayList<>();
                temp.addAll(search_set_);
                filterResults.values = temp;
            }

            return filterResults;
        }


        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<NewsGroupArticle> shownArticles = (ArrayList<NewsGroupArticle>) results.values;
            shown_.addAll(shownArticles);
            adapter.clear();

            adapter.addAll(shownArticles);

            adapter.notifyDataSetChanged();
        }
    }

}
