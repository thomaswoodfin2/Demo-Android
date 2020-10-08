package com.pinpoint.appointment.customviews;

import android.content.Context;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;


/**
 * Created by admin on 28-Jul-17.
 */

public class CustomTabLayout extends TabLayout {
    public CustomTabLayout(Context context) {
        super(context);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setupWithViewPager(ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
        this.removeAllTabs();


        ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);

        PagerAdapter adapter;
        if (viewPager != null) {

            adapter = viewPager.getAdapter();

            for (int i = 0, count = adapter.getCount(); i < count; i++) {
                Tab tab = this.newTab();
                this.addTab(tab.setText(adapter.getPageTitle(i)));


                TextView view = (TextView) ((ViewGroup) slidingTabStrip.getChildAt(i)).getChildAt(1);
                view.setTypeface(BaseApplication.getInstance().getTypeface(BaseConstants.OPENSANS_SEMIBOLD));

            }
        }
    }
}
