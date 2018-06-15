package com.myappcompany.isaac.dealday;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.myappcompany.isaac.dealday.Adapter.FeedAdapter;
import com.myappcompany.isaac.dealday.Model.Item;
import com.myappcompany.isaac.dealday.Model.RSSObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private EditText searchText;
    private ImageButton searchButton;
    private RecyclerView recyclerView;
    private List<Item> itens;
    private FeedAdapter adapter;
    private RSSObject rssObject;
    //private DataBaseService dataBaseService;
    private TextView textViewSearch;
    private TextView textViewEmptySearch;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //dataBaseService = new DataBaseService(getContext());

        searchText = (EditText) view.findViewById(R.id.textSearch);
        searchButton = (ImageButton) view.findViewById(R.id.imageButtonSearch);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewSearch);
        textViewSearch = (TextView) view.findViewById(R.id.textViewSearch);
        textViewEmptySearch = (TextView) view.findViewById(R.id.textViewEmptySearch);

        textViewEmptySearch.setVisibility(View.GONE);

        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);
        itens = new ArrayList<Item>();
        rssObject = new RSSObject(itens);
        adapter = new FeedAdapter(rssObject, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

//        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.SearchLinearLayout);
//        BottomNavigationView navigation = (BottomNavigationView) view.findViewById(R.id.navigation);
//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int screemSizeHeight = size.y;

        searchButton.setOnClickListener(searchListener);

        recyclerView.setPadding(0, 0, 0, MainActivity.navigation.getHeight());

        return view;
    }

    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            textViewSearch.setVisibility(View.GONE);
            textViewEmptySearch.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            String search =  searchText.getText().toString();

            if (search.equals("") || search == null){
                textViewSearch.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                return;
            }

            List<Item> resultItems = MainActivity.getDataBaseService().searchItems(searchText.getText().toString());
            adapter.getRssObject().setItems(resultItems);
            adapter.notifyDataSetChanged();
            if(resultItems == null || resultItems.isEmpty()){
                textViewEmptySearch.setVisibility(View.VISIBLE);
                return;
            }

        }
    };

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
