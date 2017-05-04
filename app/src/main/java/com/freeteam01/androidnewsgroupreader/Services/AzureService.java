package com.freeteam01.androidnewsgroupreader.Services;

import android.content.Context;

import com.microsoft.windowsazure.mobileservices.*;

import java.net.MalformedURLException;

public class AzureService {
    private String mMobileBackendUrl = "https://newsgroupreader.azurewebsites.net";
    private Context mContext;
    private MobileServiceClient mClient;
    private static AzureService mInstance = null;

    private AzureService(Context context) {
        mContext = context;
        try {
            mClient = new MobileServiceClient(mMobileBackendUrl, mContext);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void Initialize(Context context) {
        if (mInstance == null) {
            mInstance = new AzureService(context);
        } else {
            throw new IllegalStateException("AzureServiceAdapter is already initialized");
        }
    }

    public static AzureService getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("AzureServiceAdapter is not initialized");
        }
        return mInstance;
    }

    public static boolean isInitialized(){
        return mInstance != null;
    }

    public MobileServiceClient getClient() {
        return mClient;
    }
}