package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;
import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SubscribeActivity extends AppCompatActivity {
    private NewsGroupAdapter adapter;
    private List<NewsGroupEntry> items;
    private List<NewsGroupEntry> changedItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.lv_newsgroups);
        items = new ArrayList<>();
        changedItems = new ArrayList<>();
        adapter = new NewsGroupAdapter(this, items); //R.layout.entry_info
        listView.setAdapter(adapter);

        checkSaveButtonClick();

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                NewsGroupEntry entry = (NewsGroupEntry) parent.getItemAtPosition(position);
                Object o = adapter.getItem(position);
                if (o == null)
                    return;
                NewsGroupEntry entry = (NewsGroupEntry) o;
//                Toast.makeText(getApplicationContext(),
//                        "Clicked on Row: " + entry.getName(),
//                        Toast.LENGTH_SHORT).show();

                entry.setSelected(!entry.isSelected());
                adapter.notifyDataSetChanged();
                if (!changedItems.contains(entry))
                    changedItems.add(entry);
            }
        });

        // TODO Hint: MobileServiceSyncTable.java  tells you which functions are performing
        // TODO             local operations, and which remote operations

        showLocalNewsgroups();
//        showEntriesFromTestData();
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

    private class NewsGroupAdapter extends BaseAdapter {

        private Context context;
        private int layoutResourceId;
        private List<NewsGroupEntry> list;
        private LayoutInflater layoutInflater = null;

        public NewsGroupAdapter(Context context, List<NewsGroupEntry> list) {
            this.context = context;
            this.list = list;
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private class ViewHolder {
            public TextView entries;
            public CheckBox name;

            public ViewHolder(View base) {
                entries = (TextView) base.findViewById(R.id.entries);
                name = (CheckBox) base.findViewById(R.id.checkBox1);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                LayoutInflater li = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = li.inflate(R.layout.entry_info, null);
                holder = new ViewHolder(row);

                /*holder.entries.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        NewsGroupEntry entry = (NewsGroupEntry) cb.getTag();
                        Toast.makeText(getApplicationContext(),
                                "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
                                Toast.LENGTH_SHORT).show();
                        entry.setSelected(cb.isChecked());
                    }
                });*/

                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        NewsGroupEntry entry = (NewsGroupEntry) cb.getTag();
//                        Toast.makeText(getApplicationContext(),
//                                "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
//                                Toast.LENGTH_SHORT).show();
                        entry.setSelected(cb.isChecked());
                        if (!changedItems.contains(entry))
                            changedItems.add(entry);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            NewsGroupEntry entry = list.get(position); //getItem(position);
            holder.entries.setText(" (" + entry.getArticleCount() + ")");
            holder.name.setText(entry.getName());
            holder.name.setChecked(entry.isSelected());
            holder.name.setTag(entry);
            return row;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return list != null ? list.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private void checkSaveButtonClick() {
        Button myButton = (Button) findViewById(R.id.btn_save);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StringBuffer responseText = new StringBuffer();
                responseText.append("Changed the following Newsgroups...\n");

                for (int i = 0; i < changedItems.size(); i++) {
                    responseText.append("\n" + changedItems.get(i).getName());
                }

                persistChangedItems();

                Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void persistChangedItems() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    try {
                        for (NewsGroupEntry item : changedItems) {
                            AzureService.getInstance().updateItemInTable(item);
                        }
                        changedItems.clear();
                    } catch (ExecutionException | InterruptedException e) {
                        Log.d("AzureService", e.getMessage());
                        createAndShowDialogFromTask(e, "Error");
                    }
                    Log.d("AzureService", "updated selected items in local database");
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    public NewsGroupEntry getEntryWithName(Collection<NewsGroupEntry> c, String name) {
        for (NewsGroupEntry o : c) {
            if (o != null && o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    private void showLocalNewsgroups() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<NewsGroupEntry> storedEntries = AzureService.getInstance().getLocalNewsGroupEntries();
                    Log.d("AzureService", "loaded items from local storage");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            items.clear();
                            for (NewsGroupEntry item : storedEntries) {
                                items.add(item);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (final Exception e) {
                    Log.d("AzureService", "local storage load: " + e.getMessage());
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

//            @Override
//            protected void onPreExecute() {}

            @Override
            protected void onPostExecute(Void result) {
                syncLocalWithRemote();
            }
        };

        runAsyncTask(task);
    }

    private void syncLocalWithRemote() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<NewsGroupEntry> storedEntries = AzureService.getInstance().syncNewsGroupEntries();

                    Log.d("AzureService", "loaded items from server");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            items.clear();
                            for (NewsGroupEntry item : storedEntries) {
                                items.add(item);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (final Exception e) {
                    Log.d("AzureService", "refreshItemsFromNewsgroupAndTable: " + e.getMessage());
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                syncItemsWithNewsgroup();
            }
        };

        runAsyncTask(task);
    }

    public class NewsGroupEntryComparator implements Comparator<NewsGroupEntry> {
        @Override
        public int compare(NewsGroupEntry o1, NewsGroupEntry o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private void syncItemsWithNewsgroup() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<NewsGroupEntry> results = new ArrayList<>();
                    NewsGroupService service = new NewsGroupService("news.tugraz.at");
                    service.Connect();
                    results.addAll(service.getAllNewsgroups());
                    service.Disconnect();

                    Collections.sort(results, new NewsGroupEntryComparator());

                    Log.d("AzureService", "loaded items from newsgroup");

                    try {
                        for (NewsGroupEntry item : results) {
                            NewsGroupEntry localStored = getEntryWithName(items, item.getName());
                            if (localStored == null) {
                                AzureService.getInstance().addItemInTable(item);
                                Log.d("AzureService", "stored item locally: " + item.getName());
                            }
                            // TODO update item if it already exists, and the values have changed
//                            else
//                                AzureService.getInstance().updateItemInTable(localStored);

//                            bool exists = AzureService.getInstance().isNewsgroupStored(item);
                        }
                    } catch (ExecutionException e) {
                        Log.d("AzureService", e.getMessage());
                        createAndShowDialogFromTask(e, "Error");
                    } catch (InterruptedException e) {
                        Log.d("AzureService", e.getMessage());
                        createAndShowDialogFromTask(e, "Error");
                    }

                    Log.d("AzureService", "synced items with newsgroup");

                    try {
                        final List<NewsGroupEntry> storedEntries = AzureService.getInstance().getLocalNewsGroupEntries();
                        Log.d("AzureService", "refreshed items from local storage");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                items.clear();
                                for (NewsGroupEntry item : storedEntries) {
                                    items.add(item);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (final Exception e) {
                        Log.d("AzureService", "local storage load: " + e.getMessage());
                        createAndShowDialogFromTask(e, "Error");
                    }
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                                /* // TODO delete items which are not in the Newsgroup anymore
                    for (NewsGroupEntry item : storedEntries) {
                        NewsGroupEntry crt = getEntryWithName(results, item.getName());
                        if (crt != null) {
                            crt.setSelected(item.isSelected());
                        } else { // not available anymore, therefore delete it
                            AzureService.getInstance().removeItemFromTable(item);
                        }
                    }*/
            }
        };

        runAsyncTask(task);
    }

    private void showEntriesFromTestData() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final ArrayList<NewsGroupEntry> results = new ArrayList<NewsGroupEntry>();
                    results.add(new NewsGroupEntry(0, "tu-graz.algo", false));
                    results.add(new NewsGroupEntry(1, "tu-graz.datenbanken", true));
                    results.add(new NewsGroupEntry(2, "tu-graz.diverses", false));
                    results.add(new NewsGroupEntry(3, "tu-graz.skripten", true));
                    results.add(new NewsGroupEntry(4, "tu-graz.telekom", true));
                    results.add(new NewsGroupEntry(5, "tu-graz.veranstaltungen", false));
                    results.add(new NewsGroupEntry(6, "tu-graz.wohnungsmarkt", false));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            items.clear();
                            for (NewsGroupEntry item : results) {
                                items.add(item);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (final Exception e) {
                    Log.d("AzureService", e.getMessage());
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}