package com.myappcompany.isaac.dealday.BD;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.myappcompany.isaac.dealday.Enums.Platform;
import com.myappcompany.isaac.dealday.Model.ChaveCategoria;
import com.myappcompany.isaac.dealday.Model.Item;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by isaac on 24/03/18.
 */


//Classe que getencia a entrada dos dados no banco
public class DataBaseService {
    private SQLiteDatabase dataBaseService;
    private SimpleDateFormat simpleDateFormatGet;
    private SimpleDateFormat simpleDateFormatGetToday;
    private Context context;

    public DataBaseService(Context context) {
        this.context = context;
        DataBase dataBase = new DataBase(context);
        if(dataBase != null){
            dataBaseService = dataBase.getWritableDatabase();
        }


        simpleDateFormatGet = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        simpleDateFormatGetToday = new SimpleDateFormat("yyyy-MM-dd");
    }

    public void insert (Item item){

        int favorite;
        if(!item.isFavorite()){
            favorite = 0;
        }else{
            favorite = 1;
        }

        ContentValues valores = new ContentValues();
        valores.put("title", item.getTitle());
        valores.put("pubDate", formatStringDateInsert(simpleDateFormatGet.format(item.getPubDate())));
        valores.put("description", item.getDescription());
        valores.put("urlImage", item.getUrlImage());
        valores.put("link", item.getLink());
        valores.put("category", item.getCategory());
        valores.put("platform", item.getPlatform());
        valores.put("favorite", favorite);

        dataBaseService.insert("item", null, valores);
    }

    public void update(Item item){
        int favorite;
        if(!item.isFavorite()){
            favorite = 0;
        }else{
            favorite = 1;
        }

        ContentValues valores = new ContentValues();
        valores.put("favorite", favorite);

        dataBaseService.update("item", valores, "link = ?", new String[]{""+item.getLink()});
    }

    public void delete(Item item){
        dataBaseService.delete("item", "title = "+item.getTitle(), null);
    }


    public void freeDB(){
        String query = "DELETE FROM item WHERE title IN (select title from item ORDER BY datetime(pubDate) LIMIT 500)";
        dataBaseService.execSQL(query);
    }

    //M??todo para pagina????o dos itens
    public List<Item> getPageItems(int totalItems, boolean suggest, String category){

        //Vari??vel offset define o n??mero de itens que ser??
        //apresentado na tela por vez
        String offset = Integer.toString(totalItems);
        String query = "";

        if(suggest){
            Calendar todayCalendar = Calendar.getInstance();
            String today_date = simpleDateFormatGetToday.format(todayCalendar.getTime());
            //execu????o da query 20 itens por p??gina, ordenado pela data de publicacao
            query = "select * from item WHERE category = '"+category+"'";
        } else {
//            query = "select * from item ORDER BY datetime(pubDate) DESC LIMIT 20 OFFSET "+offset;
            query = "select * from item";
            boolean checked = false;
            boolean already_first = false;
            SharedPreferences sharedPref = context.getSharedPreferences("promoshow_shared_prefs", Context.MODE_PRIVATE);

            for(Platform platform : Platform.values()){
                checked = sharedPref.getBoolean(platform.getNome(), false);
                if(checked){
                    if(!already_first){
                        query = query.concat(" WHERE platform = '"+platform.getNome()+"'");
                        already_first = true;
                    } else{
                        query = query.concat(" OR platform = '"+platform.getNome()+"'");
                    }
                }
            }

        }

        query = query.concat(" ORDER BY datetime(pubDate) DESC LIMIT 20 OFFSET "+offset);

        Cursor  cursor = dataBaseService.rawQuery(query,null);

        //Declara????o das vari??veis
        List<Item> itens = new ArrayList<Item>();
        Item item;
        Date date;
        boolean favorite = false;
        String formatedDate;

        //Bloco para pegar itens da p??gina
        try {
            if(cursor.moveToFirst()){
                while (!cursor.isAfterLast()) {
                    item = new Item(null, null, null, null, null, null, null, false, false);

                    item.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    formatedDate = formatStringDateGet(cursor.getString(cursor.getColumnIndex("pubDate")));
                    date = simpleDateFormatGet.parse(formatedDate);
                    item.setPubDate(date);
                    item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    item.setUrlImage(cursor.getString(cursor.getColumnIndex("urlImage")));
                    item.setLink(cursor.getString(cursor.getColumnIndex("link")));
                    item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                    item.setPlatform(cursor.getString(cursor.getColumnIndex("platform")));

                    if(cursor.getInt(cursor.getColumnIndex("favorite")) == 1){
                        favorite = true;
                    } else {
                        favorite = false;
                    }


                    item.setFavorite(favorite);

                    itens.add(item);

                    if(cursor.getPosition() == 10){
                        itens.add(new Item(null, null, null, null, null, null, null, false, true));
                    }

                    cursor.moveToNext();
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return itens;
    }

    //Formatando data para inserir no item
    private String formatStringDateGet(String date){
        String year = date.substring(0, date.length() - 15);
        String month = date.substring(5, date.length() - 12);
        String day = date.substring(8, date.length() - 9);
        String time = date.substring(11, date.length());

        String formatedDate = day + "/" + month + "/" + year + " " + time;
        return formatedDate;
    }

    public List<Item> getFavoriteItems(){

        String query = query = "select * from item WHERE favorite = 1 ORDER BY datetime(pubDate) DESC";

        Cursor  cursor = dataBaseService.rawQuery(query,null);

        //Declara????o das vari??veis
        List<Item> itens = new ArrayList<Item>();
        Item item;
        Date date;
        boolean favorite = false;
        String formatedDate;

        try {
            if(cursor.moveToFirst()){
                while (!cursor.isAfterLast()) {
                    item = new Item(null, null, null, null, null, null, null, false, false);

                    item.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    formatedDate = formatStringDateGet(cursor.getString(cursor.getColumnIndex("pubDate")));
                    date = simpleDateFormatGet.parse(formatedDate);
                    item.setPubDate(date);
                    item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    item.setUrlImage(cursor.getString(cursor.getColumnIndex("urlImage")));
                    item.setLink(cursor.getString(cursor.getColumnIndex("link")));
                    item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                    item.setPlatform(cursor.getString(cursor.getColumnIndex("platform")));

                    if(cursor.getInt(cursor.getColumnIndex("favorite")) == 1){
                        favorite = true;
                    } else {
                        favorite = false;
                    }

                    item.setFavorite(favorite);

                    itens.add(item);

                    cursor.moveToNext();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return itens;
    }

    public List<Item> searchItems(String title){

        if(title == null){
            return null;
        }

        String query = query = "select * from item WHERE title LIKE '%"+title+"%' ORDER BY datetime(pubDate) DESC";

        Cursor  cursor = dataBaseService.rawQuery(query,null);

        //Declara????o das vari??veis
        List<Item> itens = new ArrayList<Item>();
        Item item;
        Date date;
        boolean favorite = false;
        String formatedDate;

        try {
            if(cursor.moveToFirst()){
                while (!cursor.isAfterLast()) {
                    item = new Item(null, null, null, null, null, null, null, false, false);

                    item.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    formatedDate = formatStringDateGet(cursor.getString(cursor.getColumnIndex("pubDate")));
                    date = simpleDateFormatGet.parse(formatedDate);
                    item.setPubDate(date);
                    item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    item.setUrlImage(cursor.getString(cursor.getColumnIndex("urlImage")));
                    item.setLink(cursor.getString(cursor.getColumnIndex("link")));
                    item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                    item.setPlatform(cursor.getString(cursor.getColumnIndex("platform")));

                    if(cursor.getInt(cursor.getColumnIndex("favorite")) == 1){
                        favorite = true;
                    } else {
                        favorite = false;
                    }

                    item.setFavorite(favorite);

                    itens.add(item);

                    cursor.moveToNext();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return itens;
    }

    //Formatando data para inserir no banco
    private String formatStringDateInsert(String date){

        String year = date.substring(6, date.length() - 9);
        String month = date.substring(3, date.length() - 14);
        String day = date.substring(0, date.length() - 17);

        String time = date.substring(11, date.length());

        String formatedDate = year+"-"+month+"-"+day+" "+time;
        return formatedDate;
    }

    //M??todo que retorna se o item j?? est?? no banco
    public boolean itemExists(String link){
        //Retira apostrofos do titulo para
        //evitar erros

        Cursor cursor = dataBaseService.rawQuery("SELECT * FROM item WHERE link = '"+link+"' LIMIT 1", null);
        if(cursor.getCount() > 0){
            return true;
        } else{
            return false;
        }
    }

    //M??todo que retorna o total de itens no banco
    public long totalItensCount(){
         return DatabaseUtils.queryNumEntries(dataBaseService, "item");
    }

    public long totalCategoryCount(){
        return DatabaseUtils.queryNumEntries(dataBaseService, "chavecategoria");
    }

    public List<ChaveCategoria> getKeyCategories(){
        List<ChaveCategoria> chaveCategorias = new ArrayList<ChaveCategoria>();

        String query = "select * from chavecategoria";

        Cursor cursor = dataBaseService.rawQuery(query, null);

        ChaveCategoria chaveCategoria;
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                chaveCategoria = new ChaveCategoria(cursor.getString(cursor.getColumnIndex("ckey")),
                        cursor.getString(cursor.getColumnIndex("category")));

                chaveCategorias.add(chaveCategoria);
                cursor.moveToNext();
            }
        }

        return chaveCategorias;

    }

    public void fillCategories(){

        //Inicio PC, Playstation e Xbox

        ContentValues valores = new ContentValues();
        valores.put("ckey", "PS4");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "XBOX");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PC ");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "STEAM");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PSN");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UPLAY ");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UBISOFT");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GAME ");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GAMER ");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        //FIM PC, Playstation e Xbox

        //INICIO Coisas e Produtos Gratis
        valores = new ContentValues();
        valores.put("ckey", "GR??TIS");
        valores.put("category", "Coisas e Produtos Gr??tis");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GRATUITO");
        valores.put("category", "Coisas e Produtos Gr??tis");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DE GRA??A");
        valores.put("category", "Coisas e Produtos Gr??tis");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Coisas e Produtos

        //INICIO Celulares e Smartphones
        valores = new ContentValues();
        valores.put("ckey", "CELULAR ");
        valores.put("category", "Celulares e Smartphones");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SMARTPHONE ");
        valores.put("category", "Celulares e Smartphones");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "IPHONE");
        valores.put("category", "Celulares e Smartphones");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GALAXY");
        valores.put("category", "Celulares e Smartphones");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Celulares e Smartphones

        //INICIO Eletrodom??sticos
        valores = new ContentValues();
        valores.put("ckey", "GELADEIRA");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MICROONDAS");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MICRO-ONDAS");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FOG??O");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "LIQUIDIFICADOR");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BATEDEIRA");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FORNO");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PIPOQUEIRA");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", " LAVAR ");
        valores.put("category", "Eletrodom??sticos");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Eletrodom??sticos

        //INICIO Moda e Acess??rios
        valores = new ContentValues();
        valores.put("ckey", "VESTIDO");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SAIA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAMISA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAL??A ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SHORT ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUSA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAMISETA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "REGATA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BRINCO ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ANEL ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "COLAR ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CORD??O ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PULSEIRA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "C&A");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "RENNER");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MARISA");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "RIACHUELO");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESPLANADA");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MODA ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BIJU ");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BIJUTERIA");
        valores.put("category", "Moda e Acess??rios");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Moda e Acess??rios

        //INICIO Tecnologia e Escrit??rio
        valores = new ContentValues();
        valores.put("ckey", "ESCRIVANHINA");
        valores.put("category", "Tecnologia e Escrit??rio");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESCRIT??RIO");
        valores.put("category", "Tecnologia e Escrit??rio");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "IMPRESSORA");
        valores.put("category", "Tecnologia e Escrit??rio");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PAPEL ");
        valores.put("category", "Tecnologia e Escrit??rio");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AGENDA ");
        valores.put("category", "Tecnologia e Escrit??rio");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Tecnologia e Escrit??rio

        //INICIO Comida e Bebida
        valores = new ContentValues();
        valores.put("ckey", "BISCOITO");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BOLACHA");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BANANA");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MA???? ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UVA ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NESCAU");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TODDY");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SUCO ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "POLPA ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CARNE ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ARROZ ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FEIJ??O");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Comida e Bebida

        //INICIO Restaurantes
        valores = new ContentValues();
        valores.put("ckey", "RESTAURANTE");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BURGUER ");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BATATA");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MILK");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SORVETE");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BOB'S");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MCDONALDS");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GIRAFFAS");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Restaurantes

        //INICIO Casa e Cozinha
        valores = new ContentValues();
        valores.put("ckey", "TRAMONTINA");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CHURRASCO");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MESA ");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TUPPEWARE");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FACAS ");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TALHER");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PANELA ");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FRIGIDEIRA");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Casa e Cozinha

        //INICIO Automotivos
        valores = new ContentValues();
        valores.put("ckey", "MOTO ");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CARRO ");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PNEU");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAPACETE");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MULTIM??DIA");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Automotivos

        //INICIO Sa??de e Beleza
        valores = new ContentValues();
        valores.put("ckey", "CABELO");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CHAPINHA");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "APARADOR DE PELOS");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BATOM");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESMALTE ");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MAQUIAGEM");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SHAMPOO");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CONDICIONADOR");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUSH");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "R??MEL ");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BOTIC??RIO");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AVON");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NATURA");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BERENICE");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", " KAY");
        valores.put("category", "Sa??de e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Sa??de e Beleza

        //INICIO Computadores e Inform??tica
        valores = new ContentValues();
        valores.put("ckey", "KABUM");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PICHAU");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUESKYINFO");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CHIPART");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ROCKETZ");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PLACA ");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MEM??RIA");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "INTEL");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AMD");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PROCESSADOR");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FONTE ");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TECLADO");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MOUSE");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PENDRIVE");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NOTEBOOK");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FONE ");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HEADSET");
        valores.put("category", "Computadores e Inform??tica");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Computadores e Inform??tica

        //INICIO TV, Som e V??deo
        valores = new ContentValues();
        valores.put("ckey", "TV ");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TELEVIS??O ");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DVD");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLURAY");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLU-RAY");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLU RAY");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOME THEATER");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SOM");
        valores.put("category", "TV, Som e V??deo");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM TV, Som e V??deo

        //INICIO Livros
        valores = new ContentValues();
        valores.put("ckey", "LIVRO");
        valores.put("category", "Livros");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "KINDLE");
        valores.put("category", "Livros");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Livros

        //INICIO Viagens
        valores = new ContentValues();
        valores.put("ckey", "VIAGEM ");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "VIAGENS ");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "VOO");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PASSAGEM");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PASSAGENS");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOTEL ");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOSPEDAGEM");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TAM ");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GOL ");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "A??RE");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Viagens

        //INICIO Bebes e Crian??as
        valores = new ContentValues();
        valores.put("ckey", "BONECO");
        valores.put("category", "Bebes e Crian??as");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BONECA");
        valores.put("category", "Bebes e Crian??as");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BRINQUEDO");
        valores.put("category", "Bebes e Crian??as");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BEB??");
        valores.put("category", "Bebes e Crian??as");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CRIAN??A");
        valores.put("category", "Bebes e Crian??as");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Bebes e Crian??as

        //INICIO Esportes e Exec??cios
        valores = new ContentValues();
        valores.put("ckey", "CENTAURO");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NETSHOES");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NIKE");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ADIDAS");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UNDER ARMOUR");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "REBOOK");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "T??NIS");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BICICLETA");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ACADEMIA");
        valores.put("category", "Esportes e Exec??cios");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Esportes e Exec??cios

        //INICIO Presentes e Servi??os
        valores = new ContentValues();
        valores.put("ckey", "DIA DO");
        valores.put("category", "Presentes e Servi??os");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DIA DA");
        valores.put("category", "Presentes e Servi??os");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NATAL");
        valores.put("category", "Presentes e Servi??os");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PRESENTE");
        valores.put("category", "Presentes e Servi??os");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SERVI??O");
        valores.put("category", "Presentes e Servi??os");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Presentes e Servi??os

        //INICIO Entretenimento e Lazer
        valores = new ContentValues();
        valores.put("ckey", "PISCINA");
        valores.put("category", "Entretenimento e Lazer");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PLAY");
        valores.put("category", "Entretenimento e Lazer");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PEBOLIM");
        valores.put("category", "Entretenimento e Lazer");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SINUCA");
        valores.put("category", "Entretenimento e Lazer");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Entretenimento e Lazer

        //INICIO Outros
        valores = new ContentValues();
        valores.put("ckey", "OUTROS");
        valores.put("category", "Outros");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Outros
    }
}
