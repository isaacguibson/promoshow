package com.myappcompany.isaac.dealday.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;

import com.myappcompany.isaac.dealday.BD.DataBaseService;
import com.myappcompany.isaac.dealday.Constantes.ConstantesPlataformas;
import com.myappcompany.isaac.dealday.MainActivity;
import com.myappcompany.isaac.dealday.Model.ChaveCategoria;
import com.myappcompany.isaac.dealday.Model.Item;
import com.myappcompany.isaac.dealday.R;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by isaac on 22/03/18.
 */

public class XMLParser {

    private static final String N_CHANNEL_ID = "com.myappcompany.isaac.dealday.Service.NOT";
    private static final String N_CHANNEL_NAME = "NOTIFICATION Channel";

    XmlPullParserFactory xmlFactoryObject;
    XmlPullParser myparser;
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;
    Document doc;

    String tag_titulo;
    String tag_description;
    String tag_pubDate;
    String tag_link;
    String tag_urlImage;
    String tag_category;

    Context context;

    DataBaseService dataBaseService;

    private boolean background_flag;

    public XMLParser(Context context, boolean background_flag) {
        dataBaseService = new DataBaseService(context);
        this.context = context;

        this.background_flag = background_flag;
    }

    public synchronized List<Item> fillItems(String plataforma, String url){
        try {
            //Inicializando variaveis para tratar XML
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            myparser = xmlFactoryObject.newPullParser();

            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();

            //Lendo RSS da url
            doc = dBuilder.parse(new InputSource(new URL(url).openStream()));

            //Normalizando documento
            doc.getDocumentElement().normalize();

            fillItemList(doc, plataforma);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fillItemList(Document doc, String plataforma) {

        Resources res = context.getResources();
        int category_count = 0;
        String[] categories = res.getStringArray(R.array.categorie_arrays);
        SharedPreferences sharedPref = context.getSharedPreferences("promoshow_shared_prefs", Context.MODE_PRIVATE);

        //Pegando todos os itens do RSS
        NodeList nodeList = doc.getElementsByTagName("item");

        Item item = new Item(null, null, null, null, null, null, null, false, false);

        //Variaveis que serão inicializadas
        //e modificadas dentro do loop
        Element eElement;
        NodeList data;
        Element line;
        Node child;
        Node nNode;

        tag_titulo = null;
        tag_description = null;
        tag_pubDate = null;
        tag_link = null;
        tag_urlImage = null;
        tag_category = null;

        findTags(plataforma);

        String titulo = null;
        String description = null;
        String pubDate = null;
        String link = null;
        String urlImage = null;
        String category = null;
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            nNode = nodeList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                eElement = (Element) nNode;

                //Pegando Título
                data = eElement.getElementsByTagName(tag_titulo);
                line = (Element) data.item(0);
                child = line.getFirstChild();

                if(child instanceof CharacterData){
                    titulo = ((CharacterData) child).getData();
                }

                //Pegando Descricao
                //teste se null pois nem todos tem descricao
                if(tag_description != null){
                    data = eElement.getElementsByTagName(tag_description);
                    line = (Element) data.item(0);
                    child = line.getFirstChild();
                    if(child instanceof CharacterData){
                        //retirando tags html da descricao
                        description = ((CharacterData) child).getData().replaceAll("\\<.*?\\>", "");
                        //deixando descricao com apenas 200 characteres
                        description = normalizeString(description);
                    }
                }

                //Pegando PubDate
                ////teste se null pois nem todos tem pubDate
                if(tag_pubDate != null){
                    data = eElement.getElementsByTagName(tag_pubDate);
                    line = (Element) data.item(0);
                    child = line.getFirstChild();

                    if(child instanceof CharacterData){
                        pubDate = ((CharacterData) child).getData();
                    } else{
                        pubDate = child.getNodeValue();
                    }

                    try {
                        date = simpleDateFormat.parse(formatDates(pubDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                //Pegando o link
                data = eElement.getElementsByTagName(tag_link);
                line = (Element) data.item(0);
                child = line.getFirstChild();

                if(child instanceof CharacterData){
                    link = ((CharacterData) child).getData();
                } else{
                    link = child.getNodeValue();
                }

                if(tag_urlImage != null){
                    //Pegando Imagem
                    data = eElement.getElementsByTagName(tag_urlImage);
                    line = (Element) data.item(0);
                    if(plataforma.equals(ConstantesPlataformas.PELANDO)){
                        urlImage = line.getAttribute("url");
                    }
                }

                if(tag_category != null && !tag_category.equals("")){
                    //Pegando Categoria
                    data = eElement.getElementsByTagName(tag_category);
                    line = (Element) data.item(0);
                    child = line.getFirstChild();

                    if(child instanceof CharacterData){
                        category = ((CharacterData) child).getData();
                    } else{
                        category = child.getNodeValue();
                    }

                } else {
                    category = chooseCategory(titulo);
                }

                if(category.equals(categories[sharedPref.getInt("category", 0)])){
                    category_count++;
                }

                //Categoria viagens para sites especificos dessa categoria
                if(plataforma.equals(ConstantesPlataformas.MELHORES_DESTINOS)
                        ||plataforma.equals(ConstantesPlataformas.PASSAGENS_IMPERDIVEIS)){
                    category = "Viagens";
                }

                //Previnindo erros de descricao e data null
                if(description == null){
                    description = "";
                }

                if(date == null){
                    date = new Date();
                }

                //preenchendo item e adicionando a lista
                item = new Item(titulo, date,
                        description.replaceAll("\\<.*?\\>", ""),
                        urlImage, link, category, plataforma, false, false);

                if(dataBaseService.itemExists(item.getLink())){
                    return;
                } else {
                    dataBaseService.insert(item);
                }

            }
        }

        if(category_count > 0 && background_flag){
            showNotification(categories[sharedPref.getInt("category", 0)]);
        }
        return;
    }

    @SuppressWarnings("deprecation")
    private void showNotification(String notifc_category){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Promoshow")
                        .setContentText("Temos novos itens na categoria "+notifc_category);

        mBuilder.setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

    private String chooseCategory(String title){
        if(dataBaseService.totalCategoryCount() == 0){
            dataBaseService.fillCategories();
        }

        String uppercase_title = title.toUpperCase();

        List<ChaveCategoria> chaveCategorias = dataBaseService.getKeyCategories();

        for(ChaveCategoria cc : chaveCategorias){
            if(uppercase_title.contains(cc.getKey())){
                return cc.getCategory();
            }
        }

        return "Outros";
    }

    //Formatando a data para dd/MM/yyyy HH:mm:ss
    private String formatDates(String dateToFormat){
        dateToFormat = dateToFormat.substring(5, dateToFormat.length() - 6);

        if(dateToFormat.contains("Jan")){
            dateToFormat = dateToFormat.replaceAll("Jan", "01");
        }else if(dateToFormat.contains("Feb") || dateToFormat.contains("Fev")){
            dateToFormat = dateToFormat.replaceAll("Feb", "02");
            dateToFormat = dateToFormat.replaceAll("Fev", "02");
        }else if(dateToFormat.contains("Mar")){
            dateToFormat = dateToFormat.replaceAll("Mar", "03");
        }else if(dateToFormat.contains("Apr") || dateToFormat.contains("Abr")){
            dateToFormat = dateToFormat.replaceAll("Apr", "04");
            dateToFormat = dateToFormat.replaceAll("Abr", "04");
        }else if(dateToFormat.contains("May") || dateToFormat.contains("Mai")){
            dateToFormat = dateToFormat.replaceAll("May", "05");
            dateToFormat = dateToFormat.replaceAll("Mai", "05");
        }else if(dateToFormat.contains("June") || dateToFormat.contains("Jun")){
            dateToFormat = dateToFormat.replaceAll("June", "06");
            dateToFormat = dateToFormat.replaceAll("Jun", "06");
        }else if(dateToFormat.contains("July") || dateToFormat.contains("Jul")){
            dateToFormat = dateToFormat.replaceAll("July", "07");
            dateToFormat = dateToFormat.replaceAll("Jul", "07");
        }else if(dateToFormat.contains("Aug") || dateToFormat.contains("Ago")){
            dateToFormat = dateToFormat.replaceAll("Aug", "08");
            dateToFormat = dateToFormat.replaceAll("Ago", "08");
        }else if(dateToFormat.contains("Sept") || dateToFormat.contains("Set")){
            dateToFormat = dateToFormat.replaceAll("Sept", "09");
            dateToFormat = dateToFormat.replaceAll("Set", "09");
        }else if(dateToFormat.contains("Oct") || dateToFormat.contains("Out")){
            dateToFormat = dateToFormat.replaceAll("Oct", "10");
            dateToFormat = dateToFormat.replaceAll("Out", "10");
        }else if(dateToFormat.contains("Nov")){
            dateToFormat = dateToFormat.replaceAll("Nov", "11");
        }else if(dateToFormat.contains("Dec") || dateToFormat.contains("Dez")){
            dateToFormat = dateToFormat.replaceAll("Dec", "12");
            dateToFormat = dateToFormat.replaceAll("Dez", "12");
        }

        String onlyDate = dateToFormat.substring(0, dateToFormat.length() - 9);
        String onlyHours = dateToFormat.substring(11);

        onlyDate = onlyDate.replaceAll(" ", "/");

        dateToFormat = onlyDate;
        dateToFormat = dateToFormat.concat(" "+onlyHours);

        return dateToFormat;
    }


    //preenchedo os valores da tags dos xml RSS
    public void findTags(String plataforma){

            tag_titulo = "title";
            tag_link = "link";

            if(plataforma.equals(ConstantesPlataformas.MELHORES_DESTINOS)
                    || plataforma.equals(ConstantesPlataformas.PASSAGENS_IMPERDIVEIS)
                    || plataforma.equals(ConstantesPlataformas.PROMOBIT)){
                tag_pubDate = "pubDate";
                tag_description = "description";
            }else if(plataforma.equals(ConstantesPlataformas.PELANDO)){
                tag_description = "description";
                tag_urlImage = "media:content";
                tag_category = "category";
                tag_pubDate = "pubDate";
            } else if(plataforma.equals(ConstantesPlataformas.ADRENALINE)
                    || plataforma.equals(ConstantesPlataformas.PROMOFORUM)){
                tag_description = "content:encoded";
                tag_pubDate = "pubDate";
            }

    }

    //Descricao so pode ter 200 caracteres
    public String normalizeString(String description){
        if(description.length() > 200){
            StringBuilder stringBuilder = new StringBuilder(description.substring(0, 200));
            stringBuilder.append(" ...");
            return stringBuilder.toString();
        } else {
            return description;
        }
    }
}
