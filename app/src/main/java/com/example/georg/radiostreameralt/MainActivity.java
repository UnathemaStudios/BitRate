package com.example.georg.radiostreameralt;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RadiosFragment radiosFragment;
    private PlayingNowFragment playingNowFragment;
    private RecordFragment recordFragment;
    private boolean playing;

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
        //-//-//-//- Tabs Ended -//-//-//-//-//

        ImageButton ibPPbutton = (ImageButton)findViewById(R.id.ipPPbutton);
        ImageView ivImageSmall = (ImageView)findViewById(R.id.ivImagePlayBar);
        TextView tvDescription = (TextView)findViewById(R.id.tvDescription);
        playing = false;


    }

    @Override
    protected void onStart() {
        super.onStart();
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

            return null;
        }

        public void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}


