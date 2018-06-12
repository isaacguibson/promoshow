package com.myappcompany.isaac.dealday.BD;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by isaac on 24/03/18.
 */

//Classe de gerenciamento do banco de dados
public class DataBase extends SQLiteOpenHelper {

    private final static String DB_NAME = "promoshowDB";
    private final static int DB_VERSION = 1;

    public DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    //Se o banco não existir ele é criado
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table item(" +
                "title text, pubDate text, description text," +
                "urlImage text, link text, category text, platform text, favorite int);");

        sqLiteDatabase.execSQL("create table chavecategoria(ckey text, category text);");
    }

    //Upgrade do banco
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table usuario;");
        onCreate(sqLiteDatabase);
    }
}
