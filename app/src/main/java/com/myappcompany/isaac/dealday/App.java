package com.myappcompany.isaac.dealday;

import android.app.Application;
import android.content.Intent;

import com.myappcompany.isaac.dealday.Service.DownloadService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), DownloadService.class));
    }
}
