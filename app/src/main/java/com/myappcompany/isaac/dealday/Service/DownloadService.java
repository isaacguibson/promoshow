package com.myappcompany.isaac.dealday.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.myappcompany.isaac.dealday.Constantes.ConstantesPlataformas;
import com.myappcompany.isaac.dealday.Constantes.RSSConstantes;
import com.myappcompany.isaac.dealday.MainActivity;
import com.myappcompany.isaac.dealday.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadService extends Service {

    String[] notificationTexts = {"Lembre-se de checar as novas promoções.", "Você pode gostar das novidades que temos!",
                                    "Dê uma chance para essas promoções", "Você tem um minuto para vêr essas promoções?",
                                    "Pensando em comprar algo? Dê uma olhada aqui!"};

    int remember = 6;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        atualizar();

        return START_STICKY;
    }

    public void atualizar(){

        final Random random = new Random();
        int horas = 0;
        int minutos = 10;
        int segundos = 0;
        final long tempo = (horas * 60 * 60 * 1000) + (minutos * 60 * 1000) + (segundos * 1000);
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask timerTaskAsync = new TimerTask(){

            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            AsyncTask<String, String, String> downloadTask = new AsyncTask<String, String, String>() {
                                @Override
                                protected String doInBackground(String... strings) {
                                    if (isConnected()) {
                                        Log.d("WARN", "INICIO");
                                        XMLParser xmlParser = new XMLParser(getApplicationContext(), true);
                                        xmlParser.fillItems(ConstantesPlataformas.PELANDO, RSSConstantes.RSS_PELANDO);
                                        xmlParser.fillItems(ConstantesPlataformas.PROMOBIT, RSSConstantes.RSS_PROMOBIT);
                                        xmlParser.fillItems(ConstantesPlataformas.ADRENALINE, RSSConstantes.RSS_ADRENALINE);
                                        xmlParser.fillItems(ConstantesPlataformas.PROMOFORUM, RSSConstantes.RSS_PROMOFORUM);
                                        xmlParser.fillItems(ConstantesPlataformas.HARDMOB, RSSConstantes.RSS_HARDMOB);
                                        xmlParser.fillItems(ConstantesPlataformas.MELHORES_DESTINOS, RSSConstantes.RSS_MELHORES_DESTINOS);
                                        xmlParser.fillItems(ConstantesPlataformas.PASSAGENS_IMPERDIVEIS, RSSConstantes.RSS_PASSAGENS_IMPERDIVEIS);
                                        Log.d("WARN", "FIM");
                                    }
                                    return null;
                                }

                            };

                            if(isConnected() && MainActivity.getMainContext() == null){
                                downloadTask.execute();
                                if(remember <=0){
                                    showNotification(notificationTexts[random.nextInt(5)]);
                                    remember = 6;
                                } else {
                                    remember--;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTaskAsync,0,tempo);

    }

    @SuppressWarnings("deprecation")
    private void showNotification(String notText){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Promoshow")
                        .setContentText(notText);

        mBuilder.setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

    //Testa se o usuário esta connectado
    //A internet
    public  boolean isConnected() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }
}
