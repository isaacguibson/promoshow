package com.myappcompany.isaac.dealday.Tabs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.TextView;

import com.myappcompany.isaac.dealday.Adapter.FeedAdapter;
import com.myappcompany.isaac.dealday.MainActivity;
import com.myappcompany.isaac.dealday.Model.Item;
import com.myappcompany.isaac.dealday.Model.RSSObject;
import com.myappcompany.isaac.dealday.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isaac on 29/03/18.
 */

public class  SuggestionsItemsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressDialog mDialog;
    private ProgressBar progressBar;
    private FeedAdapter adapter;
    private RSSObject rssObject;
    private static List<Item> itens;
    //private DataBaseService dataBaseService;
    private Boolean isScrolling;
    private int currentItems, scrollOutItems;
    private SharedPreferences sharedPref;
    private int totalItemsCount;
    private TextView empty_suggest;
    private Resources res;
    private String[] categories;

    //Variavel para manter informação se o feed já foi carregado alguma vez!
    boolean feedFilled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        empty_suggest = (TextView) view.findViewById(R.id.textViewEmptySuggest);
        empty_suggest.setVisibility(View.GONE);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBarToday);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewToday);

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

        res = getResources();
        categories = res.getStringArray(R.array.categorie_arrays);

        itens = new ArrayList<Item>();

        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Carrega o feed
        mDialog = new ProgressDialog(getActivity());

        sharedPref = getActivity().getSharedPreferences("promoshow_shared_prefs", Context.MODE_PRIVATE);
        if(sharedPref.getInt("category", 0) == 0){
            empty_suggest.setVisibility(View.VISIBLE);
        }

        loadSuggestFeed();

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

    private void loadSuggestFeed(){
        itens = new ArrayList<Item>();
        itens = MainActivity.getDataBaseService().getPageItems(0, true, categories[sharedPref.getInt("category", 0)]);

        if(itens.isEmpty()){
            empty_suggest.setVisibility(View.VISIBLE);
            return;
        } else {
            empty_suggest.setVisibility(View.GONE);
        }

        totalItemsCount = itens.size();
        rssObject = new RSSObject(itens);
        adapter = new FeedAdapter(rssObject, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void loadMoreItems(){
        adapter.getRssObject().getItems().addAll(loadMore(totalItemsCount));
        totalItemsCount = itens.size();
        adapter.notifyDataSetChanged();
    }

    //pegando itens da paginacao do banco
    private List<Item> loadMore(int totalItems){
        return MainActivity.getDataBaseService().getPageItems(totalItems, true, categories[sharedPref.getInt("category", 0)]);
    }
}
