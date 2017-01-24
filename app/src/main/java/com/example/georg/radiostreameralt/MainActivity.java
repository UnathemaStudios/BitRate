package com.example.georg.radiostreameralt;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RadiosFragment radiosFragment;
    private PlayingNowFragment playingNowFragment;
    private RecordFragment recordFragment;
    private ImageButton ibStop;
    ImageButton ibPPbutton;
    ImageView ivImageSmall;
    TextView tvDescription;
    private int playing; //0=stopped 1=playing 2=paused

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //-#_#_#_#_#_#_#_#_#_#_#_#_#_#_#-//

        radiosFragment = new RadiosFragment();
        playingNowFragment = new PlayingNowFragment();
        recordFragment = new RecordFragment();
        //swIcon = (Switch)findViewById(R.id.swIconOption);
        //swIcon.setChecked(false);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
                                .colorAccent);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
                                .textColorPrimary);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
                                .colorAccent);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }
                }
        );
        //-//-//-//- Tabs Ended -//-//-//-//-//

        ibPPbutton = (ImageButton)findViewById(R.id.ibPPbutton);
        ivImageSmall = (ImageView)findViewById(R.id.ivImagePlayBar);
        tvDescription = (TextView)findViewById(R.id.tvDescription);
        ibStop = (ImageButton)findViewById(R.id.ibStop);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        playing = 0;
        if(playing == 0||playing == 2) {
            ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        }
        else  ibPPbutton.setBackgroundResource(R.drawable.ic_pause_circle_filled);

        ibPPbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing==1){
                    //pause player
                    playerLoading();
                }
                else if(playing == 0||playing == 2){
                    //start player
                    playerLoading();
                }
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing == 2||playing == 1){
                    //stop
                    playerLoading();
                }
            }
        });
    }

    private void playerStop(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        ibStop.setEnabled(false);
        ibPPbutton.setEnabled(true);
        playing = 0;
    }

    private void playerPlay(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_pause_circle_filled);
        ibStop.setEnabled(true);
        ibPPbutton.setEnabled(true);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        //ivImageSmall.setBackgroundResource();
        //tvDescription.setText(radiofwna.get(position pou esteiles).getName);
        playing = 1;
    }

    private void playerPause(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        playing = 2;
        ibPPbutton.setEnabled(true);
        ibStop.setEnabled(true);
    }

    private void playerLoading(){
        ibPPbutton.setEnabled(false);
        ibStop.setEnabled(false);
        findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Toast.makeText(this,"strt", Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(playingNowFragment, "Playing Now");
        adapter.addFragment(radiosFragment, "Radios");
        adapter.addFragment(recordFragment, "Record");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons(){
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_play_circle);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_radio);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_recording_now);
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    class ViewPagerAdapter extends FragmentPagerAdapter{
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position){
            return fragmentList.get(position);
        }

        @Override
        public int getCount(){
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){

            //return fragmentTitleList.get(position);
            return null;
        }

        public void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}


