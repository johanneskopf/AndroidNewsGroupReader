package com.freeteam01.androidnewsgroupreader.Other;

import android.os.AsyncTask;

public abstract class SpinnerAsyncTask<N, V, V1> extends AsyncTask<N, V, V1> {

    ISpinnableActivity activity;

    public SpinnerAsyncTask(ISpinnableActivity activity) {
        this.activity = activity;
    }

    @Override
    protected V1 doInBackground(N... params) {
        activity.addedBackgroundJob();
        return null;
    }

    @Override
    protected void onPostExecute(V1 o) {
        activity.finishedBackgroundJob();
        super.onPostExecute(o);
    }
}
