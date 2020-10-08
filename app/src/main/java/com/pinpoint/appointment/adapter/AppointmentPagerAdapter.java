package com.pinpoint.appointment.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.fragment.AppointmentListFragment;
import com.pinpoint.appointment.fragment.AppointmentReceivedListFragment;
import com.pinpoint.appointment.fragment.FriendsListFragment;

public class AppointmentPagerAdapter extends FragmentPagerAdapter {

    private final Context context;
    private final String[] tabs;

    public AppointmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        tabs = context.getResources().getStringArray(R.array.tabs_types_appointments);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment fragment = new AppointmentListFragment();
            Bundle args = new Bundle();
            args.putInt(FriendsListFragment.VIEW_PAGER_POSITION, position);
            fragment.setArguments(args);
            return fragment;
            case 1:
                Fragment fragmentReceived = new AppointmentReceivedListFragment();
                Bundle args1 = new Bundle();
                args1.putInt(FriendsListFragment.VIEW_PAGER_POSITION, position);
                fragmentReceived.setArguments(args1);
                return fragmentReceived;
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

}