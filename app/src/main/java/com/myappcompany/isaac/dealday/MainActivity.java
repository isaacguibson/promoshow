package com.myappcompany.isaac.dealday;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.myappcompany.isaac.dealday.BD.DataBaseService;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static DataBaseService dataBaseService;
    FrameLayout frameLayout;
    static BottomNavigationView navigation;
    private static Context mainContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobileAds.initialize(this, "ca-app-pub-5476955901521727~4720473327");

        mainContext = getApplicationContext();
        dataBaseService = new DataBaseService(getApplicationContext());

        if(!isConnected() && dataBaseService.totalItensCount() < 25){
            Toast.makeText(getApplicationContext(), "Sem acesso à internet. Verifique sua conexão!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(dataBaseService.totalItensCount() >= 3200){
            dataBaseService.freeDB();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.content);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Promoshow");
        setSupportActionBar(toolbar);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screemSizeHeight = size.y;
        frameLayout.setMinimumHeight(screemSizeHeight - (toolbar.getHeight() + navigation.getHeight()));

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new DealsFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_deals:
                    transaction.replace(R.id.content, new DealsFragment()).commit();
                    return true;
                case R.id.navigation_favorites:
                    transaction.replace(R.id.content, new FavoritesFragment()).commit();
                    return true;
                case R.id.navigation_search:
                    transaction.replace(R.id.content, new SearchFragment()).commit();
                    return true;
            }
            return false;
        }
    };

    //Testa se o usuário esta connectado
    //A internet
    public  boolean isConnected() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_settings){
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public static DataBaseService getDataBaseService() {
        return dataBaseService;
    }

    public static void setDataBaseService(DataBaseService dataBaseService) {
        MainActivity.dataBaseService = dataBaseService;
    }

    public static Context getMainContext() {
        return mainContext;
    }

    public static void setMainContext(Context mainContext) {
        MainActivity.mainContext = mainContext;
    }
}
