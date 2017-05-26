package com.freeteam01.androidnewsgroupreader.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.R;

import java.util.ArrayList;

public class NewsgroupServerSpinnerAdapter extends ArrayAdapter<String> {
    public NewsgroupServerSpinnerAdapter(Context context, ArrayList<String> newsgroups) {
        super(context, 0, newsgroups);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String newsgroup = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsgroup_post_listview, parent, false);
        }

        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_post);
        tv_name.setText(newsgroup);
        return convertView;
    }
}
