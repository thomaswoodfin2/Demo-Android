package com.pinpoint.appointment.fragment;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.adapter.AppointmentPagerAdapter;
import com.pinpoint.appointment.customviews.CustomTabLayout;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;


public class AppointmentMainFregment extends Fragment implements ClickEvent {

    private static int lastViewPagerPosition = 0;
    private HomeActivity parent;
    public CustomTabLayout tabLayout;
    public ViewPager mViewPager;
    public AppointmentPagerAdapter viewPagerAdapter;
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      parent.tv_header.setAllCaps(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_sent_received, container, false);
        mContext = rootView.getContext();
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.VISIBLE);
        parent.tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments_header)));
        parent.iv_settings.setImageResource(R.mipmap.ic_add_user);


        tabLayout = GenericView.findViewById(getView(), R.id.tabLayout);
        mViewPager = GenericView.findViewById(getView(), R.id.pager);

        viewPagerAdapter = new AppointmentPagerAdapter(parent.getApplicationContext(), getChildFragmentManager());
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(lastViewPagerPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                lastViewPagerPosition = position;
                BaseApplication.viewPagerPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        String[] tabs = parent.getResources().getStringArray(R.array.tabs_types_appointments);
        for (String tabName : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }
        tabLayout.setTabTextColors(parent.getResources().getColor(R.color.dashboard_grey),
                parent.getResources().getColor(R.color.primary_blue_color));
        tabLayout.setSelectedTabIndicatorColor(parent.getResources().getColor(R.color.primary_blue_color));
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
//                BaseApplication.viewPagerPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (BaseConstants.SELECT_SENT) {
            BaseConstants.SELECT_SENT = true;
            TabLayout.Tab tab = tabLayout.getTabAt(0);

            if (tab != null) {
                tab.select();
            }
        } else {
//                BaseConstants.SELECT_SENT=true;
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) {
                tab.select();
            }
        }

    }


    @Override
    public void onClickEvent(View view) {

    }
}



