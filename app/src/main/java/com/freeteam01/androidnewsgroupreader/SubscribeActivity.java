package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadNewsGroups();
    }

    private void loadNewsGroups() {
        ArrayList<String> newsgroups = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            newsgroups.add("TestNewsGroupServer " + String.valueOf(i + 1));
        }

        //new NetworkHelper(newsgroups).start();

        NewsGroupsAdapter adapter = new NewsGroupsAdapter(this, newsgroups);

        ListView lv_newsgroups = (ListView) findViewById(R.id.lv_newsgroups);
        lv_newsgroups.setAdapter(adapter);
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

    class NetworkHelper extends Thread {
        private ArrayList<String> newsgroups_;

        public NetworkHelper(ArrayList<String> newsgroups) {
            this.newsgroups_ = newsgroups;
        }

        @Override
        public void run() {
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                newsgroups_ = new ArrayList<>(service.getAllNewsgroups());
                service.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
