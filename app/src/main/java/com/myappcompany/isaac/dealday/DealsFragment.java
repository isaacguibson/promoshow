package com.myappcompany.isaac.dealday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myappcompany.isaac.dealday.Adapter.TabViewPagerAdapter;
import com.myappcompany.isaac.dealday.Tabs.AllItemsFragment;
import com.myappcompany.isaac.dealday.Tabs.SuggestionsItemsFragment;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DealsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DealsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DealsFragment extends Fragment {

//    private SectionsPagerAdapter sectionsPagerAdapter;

    ViewPager mViewPager;
    TabViewPagerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_deals, container, false);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.container);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setupViewPager(mViewPager);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new AllItemsFragment(), "Todos");
        adapter.addFragment(new SuggestionsItemsFragment(), "Sugestões");
        viewPager.setAdapter(adapter);
    }
}
