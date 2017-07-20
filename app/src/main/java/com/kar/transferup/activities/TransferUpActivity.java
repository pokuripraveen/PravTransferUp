package com.kar.transferup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kar.transferup.R;
import com.kar.transferup.adapter.TransferUpViewPagerAdapter;
import com.kar.transferup.base.BaseActivity;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Message;
import com.kar.transferup.model.User;
import com.kar.transferup.storage.PreferenceManager;
import com.kar.transferup.util.AppUtil;
import com.kar.transferup.util.DBUtil;
import com.kar.transferup.view.CircularTransform;
import com.squareup.picasso.Picasso;


public class TransferUpActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager mViewpager;
    private TabLayout mTablayout;
    private TransferUpViewPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transferup);
        routeIfFromSystemTray();
        initView();
    }

    private void initView() {
        initFloatingButton();
        initToolbar();
        initNavigationView();
        initViewPager();
    }

    private void initFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Function under construction", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        User user = PreferenceManager.getInstance().getUser();
        ((TextView)headerView.findViewById(R.id.username)).setText(user.getName());
        ((TextView)headerView.findViewById(R.id.mobile)).setText(user.getCountryCode()+" "+user.getMobileNumber());
        ImageView contactImage = (ImageView)headerView.findViewById(R.id.userIcon);
        String url = user.getContactImageUrl();
        if(url != null) {
            Picasso.with(this).load(url).placeholder(R.drawable.default_contact).transform(new CircularTransform()).into(contactImage);
        } else{
            Picasso.with(this).load(R.drawable.default_contact).placeholder(R.drawable.default_contact).transform(new CircularTransform()).into(contactImage);
        }
    }

    private void initViewPager() {
        if(hasAllPermissions()){
            prepareViewPager();
        } else{
            verifyAppPermissions();
        }
    }

    @Override
    public void onAcquiredContactsPermission() {
        Logger.i("onAcquiredContactsPermission ..!!");
        prepareViewPager();
    }

    private void prepareViewPager() {
        mViewpager = (ViewPager) findViewById(R.id.container);
        mTablayout = (TabLayout) findViewById(R.id.tab_layout);
        mTablayout.setupWithViewPager(mViewpager);
        mPagerAdapter = new TransferUpViewPagerAdapter(getSupportFragmentManager());
        mViewpager.setAdapter(mPagerAdapter);
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void routeIfFromSystemTray() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if(bundle.getString("fromPhone") != null) {
                if(!"foreground".equalsIgnoreCase(bundle.getString("from"))) {
                    DBUtil dbUtil = DBUtil.getInstance();
                    Message message = Message.getMessage(bundle);
                    dbUtil.insertMessage(message, AppUtil.Type.OTHER, dbUtil.getChatID(bundle.getString("fromPhone")));
                    DBUtil.getInstance().updateUserChat(message, AppUtil.Type.ME);
                }
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra("name", bundle.getString("fromName"));
                i.putExtra("phone", bundle.getString("fromPhone"));
                startActivity(i);
            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
