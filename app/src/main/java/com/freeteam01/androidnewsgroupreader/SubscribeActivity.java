package com.freeteam01.androidnewsgroupreader;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.freeteam01.androidnewsgroupreader.Adapter.NewsgroupServerSpinnerAdapter;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SubscribeActivity extends AppCompatActivity implements AzureServiceEvent, SearchView.OnQueryTextListener {
    private NewsGroupAdapter adapter;
    private ProgressBar progressBar;
    private Spinner server_spinner;
    private String server;
    private ArrayList<NewsGroupEntry> items;
    private List<NewsGroupEntry> changedItems;
    private NewsgroupServerSpinnerAdapter server_spinner_adapter_;
    private SearchView search_view_;

    @Override
    public void onStart() {
        super.onStart();

        if (changedItems == null)
            changedItems = new ArrayList<>();
        else
            changedItems.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.lv_newsgroups);
        items = new ArrayList<>();
        adapter = new NewsGroupAdapter(this, items);
        listView.setAdapter(adapter);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        server_spinner = (Spinner) findViewById(R.id.spin_server);
        server_spinner_adapter_ = new NewsgroupServerSpinnerAdapter(this, new ArrayList<String>());
        server_spinner_adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        server_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                server = server_spinner.getItemAtPosition(position).toString();
                showNewsgroups();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                server = null;
                showNewsgroups();
            }
        });
        server_spinner_adapter_.addAll(RuntimeStorage.instance().getAllNewsgroupServers());
        server_spinner.setAdapter(server_spinner_adapter_);

        Button myButton = (Button) findViewById(R.id.btn_save);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StringBuffer responseText = new StringBuffer();
                responseText.append("Changed the following Newsgroups...\n");

                for (int i = 0; i < changedItems.size(); i++) {
                    responseText.append("\n" + changedItems.get(i).getName() + " - subscribed to " + changedItems.get(i).isSubscribed());
                }

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        AzureService.getInstance().persistSubscribedNewsgroups(changedItems);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object o = adapter.getItem(position);
                if (o == null)
                    return;
                NewsGroupEntry entry = (NewsGroupEntry) o;
                entry.setSubscribed(!entry.isSubscribed());
                adapter.notifyDataSetChanged();
                if (!changedItems.contains(entry))
                    changedItems.add(entry);
            }
        });

        showNewsgroups();
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

    private void showNewsgroups() {

        adapter.clear();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (server == null)
                    return null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                try {
                    RuntimeStorage.instance().getNewsgroupServer(server).reload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (server == null)
                    return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        adapter.addAll(RuntimeStorage.instance().getNewsgroupServer(server).getAllNewsgroups());
                        adapter.sort(new NewsGroupEntryComparator());
                    }
                });
                super.onPostExecute(aVoid);
            }
        }.execute();
        adapter.notifyDataSetChanged();
    }

    @Override
    public <T> void OnLoaded(Class<T> classType, List<T> entries) {

    }

    @Override
    public void onBackPressed() {
        if (!search_view_.isIconified()) {
            search_view_.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) SubscribeActivity.this.getSystemService(Context.SEARCH_SERVICE);

        search_view_ = null;
        if (searchItem != null) {
            search_view_ = (SearchView) searchItem.getActionView();
        }
        if (search_view_ != null) {
            search_view_.setSearchableInfo(searchManager.getSearchableInfo(SubscribeActivity.this.getComponentName()));
        }
        search_view_.setSubmitButtonEnabled(true);
        search_view_.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    static class NewsGroupEntryComparator implements Comparator<NewsGroupEntry> {
        public int compare(NewsGroupEntry n1, NewsGroupEntry n2) {
            return n1.getName().compareTo(n2.getName());
        }
    }

    private class NewsGroupAdapter extends ArrayAdapter<NewsGroupEntry> implements Filterable {


        private ArrayList<NewsGroupEntry> filtered;
        private ArrayList<NewsGroupEntry> items;

        public NewsGroupAdapter(Context context, ArrayList<NewsGroupEntry> list) {
            super(context, 0, list);
            this.filtered = list;
            this.items = (ArrayList<NewsGroupEntry>) list.clone();
        }

        @Override
        public void add(@Nullable NewsGroupEntry object) {
            items.add(object);
            super.add(object);
        }

        @Override
        public void addAll(@NonNull Collection<? extends NewsGroupEntry> collection) {
            items.addAll(collection);
            super.addAll(collection);
        }

        @Override
        public void clear() {
            items.clear();
            super.clear();
        }

        @Override
        public void remove(@Nullable NewsGroupEntry object) {
            items.remove(object);
            super.remove(object);
        }

        @Override
        public int getCount() {
            return filtered.size();
        }

        @Nullable
        @Override
        public NewsGroupEntry getItem(int position) {
            return filtered.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.entry_info, parent, false);
                holder = new ViewHolder(row);

                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        NewsGroupEntry entry = (NewsGroupEntry) cb.getTag();
                        entry.setSubscribed(cb.isChecked());
                        if (!changedItems.contains(entry))
                            changedItems.add(entry);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            NewsGroupEntry entry = getItem(position);
            holder.entries.setText(" (" + entry.getArticleCount() + ")");
            holder.name.setText(entry.getName());
            holder.name.setChecked(entry.isSubscribed());
            holder.name.setTag(entry);
            return row;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new NewsGroupFilter();
        }

        private class ViewHolder {
            public TextView entries;
            public CheckBox name;

            public ViewHolder(View base) {
                entries = (TextView) base.findViewById(R.id.entries);
                name = (CheckBox) base.findViewById(R.id.cb_subscribe);
            }
        }


        private class NewsGroupFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    ArrayList<NewsGroupEntry> tempList = new ArrayList<>();

                    for (NewsGroupEntry entry : items) {
                        if (entry.getName().contains(constraint)) {
                            tempList.add(entry);
                        }
                    }

                    filterResults.count = tempList.size();
                    filterResults.values = tempList;
                } else {
                    filterResults.count = items.size();
                    ArrayList<NewsGroupEntry> temp = new ArrayList<>();
                    temp.addAll(items);
                    filterResults.values = temp;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (ArrayList<NewsGroupEntry>) results.values;

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}