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

    //Método para paginação dos itens
    public List<Item> getPageItems(int totalItems, boolean suggest, String category){

        //Variável offset define o número de itens que será
        //apresentado na tela por vez
        String offset = Integer.toString(totalItems);
        String query = "";

        if(suggest){
            Calendar todayCalendar = Calendar.getInstance();
            String today_date = simpleDateFormatGetToday.format(todayCalendar.getTime());
            //execução da query 20 itens por página, ordenado pela data de publicacao
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

        //Declaração das variáveis
        List<Item> itens = new ArrayList<Item>();
        Item item;
        Date date;
        boolean favorite = false;
        String formatedDate;

        //Bloco para pegar itens da página
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

        //Declaração das variáveis
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

        //Declaração das variáveis
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

    //Método que retorna se o item já está no banco
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

    //Método que retorna o total de itens no banco
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
        valores.put("ckey", "PC");
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
        valores.put("ckey", "UPLAY");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UBISOFT");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GAME");
        valores.put("category", "PC, PlayStation e Xbox");
        dataBaseService.insert("chavecategoria", null, valores);

        //FIM PC, Playstation e Xbox

        //INICIO Coisas e Produtos Gratis
        valores = new ContentValues();
        valores.put("ckey", "GRÁTIS");
        valores.put("category", "Coisas e Produtos Grátis");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GRATUITO");
        valores.put("category", "Coisas e Produtos Grátis");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DE GRAÇA");
        valores.put("category", "Coisas e Produtos Grátis");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Coisas e Produtos

        //INICIO Celulares e Smartphones
        valores = new ContentValues();
        valores.put("ckey", "CELULAR");
        valores.put("category", "Celulares e Smartphones");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SMARTPHONE");
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

        //INICIO Eletrodomésticos
        valores = new ContentValues();
        valores.put("ckey", "GELADEIRA");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MICROONDAS");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MICRO-ONDAS");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FOGÃO");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "LIQUIDIFICADOR");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BATEDEIRA");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FORNO");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PIPOQUEIRA");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "LAVAR");
        valores.put("category", "Eletrodomésticos");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Eletrodomésticos

        //INICIO Moda e Acessórios
        valores = new ContentValues();
        valores.put("ckey", "VESTIDO");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SAIA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAMISA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CALÇA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SHORT");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUSA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CAMISETA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "REGATA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BRINCO");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ANEL");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "COLAR");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CORDÃO");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PULSEIRA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "C&A");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "RENNER");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MARISA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "RIACHUELO");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESPLANADA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MODA");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BIJU");
        valores.put("category", "Moda e Acessórios");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Moda e Acessórios

        //INICIO Tecnologia e Escritório
        valores = new ContentValues();
        valores.put("ckey", "ESCRIVANHINA");
        valores.put("category", "Tecnologia e Escritório");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESCRITÓRIO");
        valores.put("category", "Tecnologia e Escritório");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "IMPRESSORA");
        valores.put("category", "Tecnologia e Escritório");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PAPEL");
        valores.put("category", "Tecnologia e Escritório");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AGENDA");
        valores.put("category", "Tecnologia e Escritório");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Tecnologia e Escritório

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
        valores.put("ckey", "MAÇÃ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UVA");
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
        valores.put("ckey", "SUCO");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "POLPA");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CARNE");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ARROZ");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FEIJÃO");
        valores.put("category", "Comida e Bebida");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Comida e Bebida

        //INICIO Restaurantes
        valores = new ContentValues();
        valores.put("ckey", "RESTAURANTE");
        valores.put("category", "Restaurantes");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BURGUER");
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
        valores.put("ckey", "MESA");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TUPPEWARE");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FACAS");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TALHER");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PANELA");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FRIGIDEIRA");
        valores.put("category", "Casa e Cozinha");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Casa e Cozinha

        //INICIO Automotivos
        valores = new ContentValues();
        valores.put("ckey", "MOTO");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CARRO");
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
        valores.put("ckey", "MULTIMÍDIA");
        valores.put("category", "Automotivos");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Automotivos

        //INICIO Saúde e Beleza
        valores = new ContentValues();
        valores.put("ckey", "CABELO");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CHAPINHA");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "APARADOR DE PELOS");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BATOM");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ESMALTE");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MAQUIAGEM");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SHAMPOO");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CONDICIONADOR");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUSH");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "RÍMEL");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BOTICÁRIO");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AVON");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NATURA");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BERENICE");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "KAY");
        valores.put("category", "Saúde e Beleza");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Saúde e Beleza

        //INICIO Computadores e Informática
        valores = new ContentValues();
        valores.put("ckey", "KABUM");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PICHAU");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLUESKYINFO");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CHIPART");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ROCKETZ");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PLACA");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MEMÓRIA");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "INTEL");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AMD");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PROCESSADOR");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FONTE");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TECLADO");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "MOUSE");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PENDRIVE");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NOTEBOOK");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GAMER");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "FONE");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HEADSET");
        valores.put("category", "Computadores e Informática");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Computadores e Informática

        //INICIO TV, Som e Vídeo
        valores = new ContentValues();
        valores.put("ckey", "TV");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TELEVISÃO");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DVD");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLURAY");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLU-RAY");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BLU RAY");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOME THEATER");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOME THEATER");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SOM");
        valores.put("category", "TV, Som e Vídeo");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM TV, Som e Vídeo

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
        valores.put("ckey", "VIAGE");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "VOO");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PASSAGE");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "VIAGEM");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOTEL");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "HOSPEDAGEM");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TAM");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "GOL");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "AÉRE");
        valores.put("category", "Viagens");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Viagens

        //INICIO Bebes e Crianças
        valores = new ContentValues();
        valores.put("ckey", "BONECO");
        valores.put("category", "Bebes e Crianças");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BONECA");
        valores.put("category", "Bebes e Crianças");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BRINQUEDO");
        valores.put("category", "Bebes e Crianças");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BEBÊ");
        valores.put("category", "Bebes e Crianças");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "CRIANÇA");
        valores.put("category", "Bebes e Crianças");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Bebes e Crianças

        //INICIO Esportes e Execícios
        valores = new ContentValues();
        valores.put("ckey", "CENTAURO");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NETSHOES");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NIKE");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ADIDAS");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "UNDER ARMOUR");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "REBOOK");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "TÊNIS");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "BICICLETA");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "ACADEMIA");
        valores.put("category", "Esportes e Execícios");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Esportes e Execícios

        //INICIO Presentes e Serviços
        valores = new ContentValues();
        valores.put("ckey", "DIA DO");
        valores.put("category", "Presentes e Serviços");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "DIA DA");
        valores.put("category", "Presentes e Serviços");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "NATAL");
        valores.put("category", "Presentes e Serviços");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "PRESENTE");
        valores.put("category", "Presentes e Serviços");
        dataBaseService.insert("chavecategoria", null, valores);

        valores = new ContentValues();
        valores.put("ckey", "SERVIÇO");
        valores.put("category", "Presentes e Serviços");
        dataBaseService.insert("chavecategoria", null, valores);
        //FIM Presentes e Serviços

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
