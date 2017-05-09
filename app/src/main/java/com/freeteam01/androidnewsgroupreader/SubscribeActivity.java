package com.freeteam01.androidnewsgroupreader;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

public class SubscribeActivity extends AppCompatActivity {
    NewsGroupAdapter dataAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        displayTestData();

        LoadNewsGroupsTask loader = new LoadNewsGroupsTask();
        loader.execute();

        checkButtonClick();
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

    public void populateList(ArrayList<NewsGroupEntry> newsgroups) {
        dataAdapter = new NewsGroupAdapter(this, R.layout.entry_info, newsgroups);
        final ListView listView = (ListView) findViewById(R.id.lv_newsgroups);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                NewsGroupEntry entry = (NewsGroupEntry) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + entry.getName(),
                        Toast.LENGTH_SHORT).show();
                entry.setSelected(!entry.isSelected());
                dataAdapter.notifyDataSetChanged();
            }
        });
    }

    private void displayTestData() {
        ArrayList<NewsGroupEntry> entryList = new ArrayList<NewsGroupEntry>();
        entryList.add(new NewsGroupEntry(0, "tu-graz.algo", false));
        entryList.add(new NewsGroupEntry(1, "tu-graz.datenbanken", true));
        entryList.add(new NewsGroupEntry(2, "tu-graz.diverses", false));
        entryList.add(new NewsGroupEntry(3, "tu-graz.skripten", true));
        entryList.add(new NewsGroupEntry(4, "tu-graz.telekom", true));
        entryList.add(new NewsGroupEntry(5, "tu-graz.veranstaltungen", false));
        entryList.add(new NewsGroupEntry(6, "tu-graz.wohnungsmarkt", false));

        dataAdapter = new NewsGroupAdapter(this, R.layout.entry_info, entryList);
        ListView listView = (ListView) findViewById(R.id.lv_newsgroups);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                NewsGroupEntry entry = (NewsGroupEntry) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + entry.getName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class NewsGroupAdapter extends ArrayAdapter<NewsGroupEntry> {

        private ArrayList<NewsGroupEntry> countryList;

        public NewsGroupAdapter(Context context, int textViewResourceId,
                                ArrayList<NewsGroupEntry> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<NewsGroupEntry>();
            this.countryList.addAll(countryList);
        }

        private class ViewHolder {
            TextView entries;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.entry_info, null);

                holder = new ViewHolder();
                holder.entries = (TextView) convertView.findViewById(R.id.entries);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        NewsGroupEntry country = (NewsGroupEntry) cb.getTag();
                        Toast.makeText(getApplicationContext(),
                                "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
                                Toast.LENGTH_SHORT).show();
                        country.setSelected(cb.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NewsGroupEntry entry = countryList.get(position);
            holder.entries.setText(" (" + entry.getArticleCount() + ")");
            holder.name.setText(entry.getName());
            holder.name.setChecked(entry.isSelected());
            holder.name.setTag(entry);

            return convertView;
        }
    }

    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");

                ArrayList<NewsGroupEntry> countryList = dataAdapter.countryList;
                for (int i = 0; i < countryList.size(); i++) {
                    NewsGroupEntry entry = countryList.get(i);
                    if (entry.isSelected()) {
                        responseText.append("\n" + entry.getName());
                    }
                }

                Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class LoadNewsGroupsTask extends AsyncTask<Void, Void, ArrayList<NewsGroupEntry>> {
        ArrayList<NewsGroupEntry> newsgroups = new ArrayList<NewsGroupEntry>();

        @Override
        protected ArrayList<NewsGroupEntry> doInBackground(Void... params) {
            try {
                NewsGroupService service = new NewsGroupService();
                service.Connect();
                newsgroups.addAll(service.getAllNewsgroups());
                service.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newsgroups;
        }

        protected void onPostExecute(ArrayList<NewsGroupEntry> newsgroups) {
//            dataAdapter.clear();
//            dataAdapter.addAll(newsgroups);
//            dataAdapter.notifyDataSetChanged();
            populateList(newsgroups);
        }
    }
}