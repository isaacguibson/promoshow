package com.myappcompany.isaac.dealday.Tabs;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.myappcompany.isaac.dealday.Adapter.FeedAdapter;
import com.myappcompany.isaac.dealday.Constantes.ConstantesPlataformas;
import com.myappcompany.isaac.dealday.Constantes.RSSConstantes;
import com.myappcompany.isaac.dealday.MainActivity;
import com.myappcompany.isaac.dealday.Model.Item;
import com.myappcompany.isaac.dealday.Model.RSSObject;
import com.myappcompany.isaac.dealday.R;
import com.myappcompany.isaac.dealday.Service.XMLParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isaac on 28/03/18.
 */

public class AllItemsFragment extends Fragment {

    RecyclerView recyclerView;
    ProgressDialog mDialog;
    ProgressBar progressBar;
    FeedAdapter adapter;
    RSSObject rssObject;
    static List<Item> itens;
    //DataBaseService dataBaseService;
    Boolean isScrolling;
    int currentItems, scrollOutItems;
    int totalItemsCount;

    private Bundle savedInstanceState;

    //Variavel para manter informação se o feed já foi carregado alguma vez!
    boolean feedFilled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        this.savedInstanceState = savedInstanceState;

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init(){
        //dataBaseService = new DataBaseService(getContext());
        feedFilled = false;

        itens = new ArrayList<Item>();

        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Carrega o feed
        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);

        if(this.savedInstanceState == null){
            loadRSS();
        }

        //Esse bloco carrega a paginacao
        isScrolling = false;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = linearLayoutManager.getChildCount();
                scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(isScrolling && (currentItems + scrollOutItems == totalItemsCount)){
                    isScrolling = false;

                    loadMoreItems();
                }
            }
        });
    }

    //pegando itens da paginacao do banco
    private List<Item> loadMore(int totalItems){
        return MainActivity.getDataBaseService().getPageItems(totalItems, false, "");
    }

    //Atividade assincrona para pegar mais itens
    private void loadMoreItems(){
        AsyncTask<String, String, String> loadMoreData = new AsyncTask<String, String, String>() {

            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            protected String doInBackground(String... strings) {
                //adapter.getRssObject().getItems().add(new Item(null, null, null, null, null, null, null, false, true));
                adapter.getRssObject().getItems().addAll(loadMore(totalItemsCount));
                totalItemsCount = itens.size();

                return null;
            }
        };

        loadMoreData.execute();
    }

    //Atividade assincrona para pegar itens ou recarregar o feed
    private void loadRSS() {
        AsyncTask<String, String, String> loadRSSAsync = new AsyncTask<String, String, String>() {

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Carregando...");
                mDialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                try{
                    if(MainActivity.getDataBaseService().totalItensCount() < 25) {
                        if(isConnected()){
                            XMLParser xmlParser = new XMLParser(MainActivity.getMainContext(), false);
                            xmlParser.fillItems(ConstantesPlataformas.PELANDO,
                                    RSSConstantes.RSS_PELANDO);
                            xmlParser.fillItems(ConstantesPlataformas.PROMOBIT,
                                    RSSConstantes.RSS_PROMOBIT);
                            xmlParser.fillItems(ConstantesPlataformas.ADRENALINE,
                                    RSSConstantes.RSS_ADRENALINE);
                            xmlParser.fillItems(ConstantesPlataformas.PROMOFORUM,
                                    RSSConstantes.RSS_PROMOFORUM);
                            xmlParser.fillItems(ConstantesPlataformas.HARDMOB,
                                    RSSConstantes.RSS_HARDMOB);
                            xmlParser.fillItems(ConstantesPlataformas.MELHORES_DESTINOS,
                                    RSSConstantes.RSS_MELHORES_DESTINOS);
                            xmlParser.fillItems(ConstantesPlataformas.PASSAGENS_IMPERDIVEIS,
                                    RSSConstantes.RSS_PASSAGENS_IMPERDIVEIS);
                        }
                    }

                    itens = new ArrayList<Item>();
                    itens = MainActivity.getDataBaseService().getPageItems(0, false, "");
                    totalItemsCount = itens.size();

                    if(!feedFilled){
                        rssObject = new RSSObject(itens);
                    } else {
                        rssObject.getItems().addAll(itens);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                mDialog.dismiss();
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if(!feedFilled){
                    adapter = new FeedAdapter(rssObject, getActivity());
                    recyclerView.setAdapter(adapter);
                    feedFilled = true;
                }
                adapter.notifyDataSetChanged();

                if(!isConnected()){
                    Toast.makeText(getContext(), "Sem acesso à internet. Verifique sua conexão!", Toast.LENGTH_SHORT).show();
                }

                fillDataBase();
            }
        };

        loadRSSAsync.execute();
    }

    //Atividade assincrona para baixar as promocoes
    //em background!
    public void fillDataBase(){
        AsyncTask<String, String, String> downloadData = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try{
                    XMLParser xmlParser = new XMLParser(MainActivity.getMainContext(), false);
                    xmlParser.fillItems(ConstantesPlataformas.PELANDO,
                            RSSConstantes.RSS_PELANDO);
                    xmlParser.fillItems(ConstantesPlataformas.PROMOBIT,
                            RSSConstantes.RSS_PROMOBIT);
                    xmlParser.fillItems(ConstantesPlataformas.ADRENALINE,
                            RSSConstantes.RSS_ADRENALINE);
                    xmlParser.fillItems(ConstantesPlataformas.PROMOFORUM,
                            RSSConstantes.RSS_PROMOFORUM);
                    xmlParser.fillItems(ConstantesPlataformas.HARDMOB,
                            RSSConstantes.RSS_HARDMOB);
                    xmlParser.fillItems(ConstantesPlataformas.MELHORES_DESTINOS,
                            RSSConstantes.RSS_MELHORES_DESTINOS);
                    xmlParser.fillItems(ConstantesPlataformas.PASSAGENS_IMPERDIVEIS,
                            RSSConstantes.RSS_PASSAGENS_IMPERDIVEIS);
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        if(isConnected()) {
            downloadData.execute();
        }
    }

    //Testa se o usuário esta connectado
    //A internet
    public  boolean isConnected() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getActivity().getSystemService(getContext().CONNECTIVITY_SERVICE);
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
