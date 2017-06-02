package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Adapter.NewsgroupServerSpinnerAdapter;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Services.AzureServiceEvent;
import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import java.util.ArrayList;
import java.util.List;

public class SubscribeActivity extends AppCompatActivity implements AzureServiceEvent {
    private NewsGroupAdapter adapter;
    private Spinner server_spinner;
    private String server;
    private ArrayList<NewsGroupEntry> items;
    private List<NewsGroupEntry> changedItems;
    private NewsgroupServerSpinnerAdapter server_spinner_adapter_;

    @Override
    public void onStart() {
        super.onStart();

        /*if (AzureService.getInstance().isAzureServiceEventFired()) {
            OnNewsgroupsLoaded(AzureService.getInstance().getNewsGroupEntries());
            Log.d("AzureService", "SubscribeActivity loaded entries as AzureEvent was already fired");
        }*/ /*else {
            AzureService.getInstance().addAzureServiceEventListener(this);
            Log.d("AzureService", "SubscribeActivity subscribed to AzureEvent");
        }*/
    }

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

//        checkSaveButtonClick();

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

                entry.setSubscribed(!entry.isSubscribed());
                adapter.notifyDataSetChanged();
                if (!changedItems.contains(entry))
                    changedItems.add(entry);
            }
        });

        // TODO Hint: MobileServiceSyncTable.java  tells you which functions are performing
        // TODO             local operations, and which remote operations

        showNewsgroups();
//        showEntriesFromTestData();

//        AzureService.getInstance().addAzureServiceEventListener(this);
//        Log.d("AzureService", "SubscribeActivity subscribed to AzureEvent");
//        if (AzureService.getInstance().isAzureServiceEventFired()) {
//            OnNewsgroupsLoaded(AzureService.getInstance().getNewsGroupEntries());
//            Log.d("AzureService", "SubscribeActivity loaded entries as AzureEvent was already fired");
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                AzureService.getInstance().setNewsGroupEntries(items);
/*                for (NewsGroupEntry newsGroupEntry:changedItems) {
                    NewsGroupEntry reset = getEntryWithName(items, newsGroupEntry.getName());
                    reset.setSelected(!newsGroupEntry.isSubscribed());
                }*/
//                AzureService.getInstance().setSelectedNewsGroupEntries(changedItems);
//                AzureService.getInstance().persistSelectedNewsGroupEntries(changedItems);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnNewsgroupsLoaded(List<NewsGroupEntry> newsGroupEntries) {
    }

    private void showNewsgroups() {
        adapter.clear();
        if (server != null) {
            adapter.addAll(RuntimeStorage.instance().getNewsgroupServer(server).getAllNewsgroups());
        }
        adapter.notifyDataSetChanged();
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    /*private void checkSaveButtonClick() {
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
    }*/

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


    private class NewsGroupAdapter extends ArrayAdapter<NewsGroupEntry> {


        public NewsGroupAdapter(Context context, ArrayList<NewsGroupEntry> list) {
            super(context, 0, list);
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
            NewsGroupEntry entry = getItem(position); //getItem(position);
            holder.entries.setText(" (" + entry.getArticleCount() + ")");
            holder.name.setText(entry.getName());
            holder.name.setChecked(entry.isSubscribed());
            holder.name.setTag(entry);
            return row;
        }

        private class ViewHolder {
            public TextView entries;
            public CheckBox name;

            public ViewHolder(View base) {
                entries = (TextView) base.findViewById(R.id.entries);
                name = (CheckBox) base.findViewById(R.id.cb_subscribe);
            }
        }
    }
}