package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.ArrayList;

public class SubscribeActivity extends AppCompatActivity {
    NewsGroupsAdapter newsgroupadapter_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lv_newsgroups = (ListView) findViewById(R.id.lv_newsgroups);
        newsgroupadapter_ = new NewsGroupsAdapter(this, new ArrayList<String>());
        lv_newsgroups.setAdapter(newsgroupadapter_);

        LoadNewsGroupsTask loader = new LoadNewsGroupsTask();
        loader.execute();
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

    private class LoadNewsGroupsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> newsgroups = null;
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                newsgroups = new ArrayList<>(service.getAllNewsgroups());
                service.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newsgroups;
        }

        protected void onPostExecute(ArrayList<String> newsgroups) {
            newsgroupadapter_.addAll(newsgroups);
            newsgroupadapter_.notifyDataSetChanged();
        }

    }


    public class NewsGroupsAdapter extends ArrayAdapter<String> {
        public NewsGroupsAdapter(Context context, ArrayList<String> newsgroups) {
            super(context, 0, newsgroups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String newsgroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsgroup, parent, false);
            }

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            tv_name.setText(newsgroup);
            return convertView;
        }
    }
}
