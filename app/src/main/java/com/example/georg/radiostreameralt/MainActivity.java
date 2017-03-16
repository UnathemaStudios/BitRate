package com.example.georg.radiostreameralt;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RadiosFragment radiosFragment;
    private PlayingNowFragment playingNowFragment;
    private RecordFragment recordFragment;
    private ImageButton ibPPbutton;
    private ImageView ivImageSmall;
    private TextView tvDescription;
    private int finger = 0;
    public ArrayList<Radio> radiosList;
    private File radiosFile;
    private File radiosFileEXTdir;
    private File radiosFileEXT;
    private int playing;
    private static final int STOPPED = 0;
    private static final int PLAYING = 2;
    private RelativeLayout playerLayout;
    private int durationtmp;
    private boolean backPressed = false;
    private boolean playerVisible = false;


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

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        playerLayout = (RelativeLayout) findViewById(R.id.relativeLayout2);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
                                .colorAccent);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

                        if (tabLayout.getSelectedTabPosition() == 0 && playerVisible) {
                            slideToBottom();
                            playerVisible = false;
                        }
                        else if (tabLayout.getSelectedTabPosition() != 0 && !playerVisible) {
                            slideToTop();
                            playerVisible = true;
                        }
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
//
                    }
                }
        );
        viewPager.setOffscreenPageLimit(2);
        pageSelector(1);

        //-//-//-//-Radios List Init-//-//-//-//-//-//

        initRadiosList();

        //-//-//-//- Tabs Ended -//-//-//-//-//

        ibPPbutton = (ImageButton) findViewById(R.id.ibPPbutton);
        ivImageSmall = (ImageView) findViewById(R.id.ivImagePlayBar);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        playing = STOPPED;
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);

        ibPPbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing == PLAYING) {
                    disableButtons();
                    //STOP player
                    Log.d("STOP BUTTON", "STOPPED PRESSED");
                    tellServiceP("PLAYER_STOP"); //broadcast STOP (for media player)
                    playing = STOPPED;
                }
                else if (playing == STOPPED) {
                    disableButtons();
                    //start player
                    //start media player service
                    tellServiceP("PLAYER_PLAY", "http://philae.shoutca.st:8307/stream", 1);
                    finger = 1;
                }
            }
        });
        //start listening for broadcasts
        if (serviceReceiver != null) {
            registerReceiver(serviceReceiver, new IntentFilter("0"));
            registerReceiver(serviceReceiver, new IntentFilter("1"));
            registerReceiver(serviceReceiver, new IntentFilter("2"));
            registerReceiver(serviceReceiver, new IntentFilter("radioToPlay"));
            registerReceiver(serviceReceiver, new IntentFilter("REC_CURRENT"));
        }

        if (isMyServiceRunning(MainService.class)) //IF mediaplayer service is running
        {
            tellServiceP("REQUEST_STATUS"); //request mediaplayer service status
        }

    }


    @Override
    public void onBackPressed() {
        if (!backPressed) {
            Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            backPressed = true;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressed = false;
                }
            }, 2000);
        }
        else super.onBackPressed();

    }

    private void playerStop() {
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        ibPPbutton.setEnabled(true);
        playing = STOPPED;
    }

    private void playerPlay() {
        ibPPbutton.setBackgroundResource(R.drawable.ic_stop);
        ibPPbutton.setEnabled(true);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        playing = PLAYING;
    }


    public void disableButtons() {
        ibPPbutton.setEnabled(false);
//        findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(playingNowFragment, "Playing Now");
        adapter.addFragment(radiosFragment, "Radios");
        adapter.addFragment(recordFragment, "Record");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_play_circle);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_radio);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_recording_now);
    }

    //Broadcast Receiver
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("0")) //if 0 (STOPPED) is received
            {
                //ui after Stopped
                playerStop();
            }
            else if (intent.getAction().equals("1")) //if 1 (LOADING) is received
            {
                findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
            }
            else if (intent.getAction().equals("2")) //if 2 (PLAYING) is received
            {
                playerPlay();
            }
            else if (intent.getAction().equals("REC_CURRENT")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //check for permission first and then rec
                    checkForWritePermissionAndRec(0);
                }
                else {
                    rec(radiosList.get(finger).getUrl(), 0);
                }
            }
        }
    };

    @Override
    protected void onRestart() {
        if (isMyServiceRunning(MainService.class)) //if media player service is running
        {
            send("REQUEST_STATUS"); //request media player status
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(serviceReceiver); //stop listening for broadcasts
        saveToFIle();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkForWritePermissionAndRec(int duration) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.w("Main", "Permission (Write to external storage) already granted");
            rec(radiosList.get(finger).getUrl(), duration);
        }
        else {
            durationtmp = duration;
            String permissionRequested[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissionRequested, 5);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.w("Main", "Permission (Write to external storage) just granted");
                rec(radiosList.get(finger).getUrl(), durationtmp);
            }
            else {
                Log.w("Main", "Permission (Write to external storage) denied");
            }
        }
    }

    public void rec(String url, int duration) {

        if (duration != 0) {
            tellServiceR("RECORD", url, duration);
        }
        else {
            tellServiceR("RECORD", url, -1);
        }
    }

    //send function to broadcast an action
    public void send(String actionToSend) {
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        sendBroadcast(intent);
    }

    public void tellServiceP(String action, String url, int finger) {
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        intent.putExtra("url", url);
        intent.putExtra("finger", finger);
        startService(intent);
    }

    public void tellServiceP(String action) {
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        startService(intent);
    }


    public void tellServiceR(String action, String url, int duration) {
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        intent.putExtra("urlString", url);
        intent.putExtra("duration", duration);
        intent.putExtra("name", radiosList.get(finger).getName());
        startService(intent);
    }

    public void tellServiceR(String action, int key) {
        Intent serviceIntent = new Intent(this, MainService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtra("key", key);
        startService(serviceIntent);
    }

    public void initRadiosList(){
        radiosFile = new File(getFilesDir(), "RadiosList");
        radiosFileEXTdir = new File(Environment.getExternalStorageDirectory() + "/Streams");
        radiosFileEXT = new File(radiosFileEXTdir.getAbsolutePath(), "RadiosList.txt");
        radiosList = new ArrayList<>();
        Log.w("radiosFile.exists()", radiosFile.exists() + "");
        if (!radiosFile.exists())
        {
            radiosList.add(new Radio("1055 Rock", "http://46.4.121.138:8006/1055rock", R.drawable.ic_radios_logo_1055));
            radiosList.add(new Radio("InfinityGreece", "http://philae.shoutca.st:8307/stream", R.drawable.ic_radio_infinitygreece));
            radiosList.add(new Radio("Radio Nowhere", "http://radio.arenafm.gr:45054/;stream.mp3", R.drawable.ic_radio_nowhere));
            try
            {
                boolean fileCreated = radiosFile.createNewFile();
                Log.w("fileCreated", "" + fileCreated);
                if (!fileCreated) Log.w("FILE ERROR", "FileNOTcreatedd");
                saveToFIle();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (!radiosFileEXTdir.exists()) radiosFileEXTdir.mkdirs();
        if (!radiosFileEXT.exists())
        {
            try
            {
                radiosFileEXT.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        //Read List
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(radiosFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        radiosList.clear();
        String name, url;
        int icon;
        try {
            while ((name = reader.readLine()) != null)
            {
                url = reader.readLine();
                icon = Integer.parseInt(reader.readLine());
                radiosList.add(new Radio(name, url, icon));
                System.out.println(name + url + icon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFIle()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(radiosFile));
            //BufferedWriter writerEXT = new BufferedWriter(new FileWriter(radiosFileEXT));
            for (int i = 0; i < radiosList.size(); i++)
            {
                writer.write(radiosList.get(i).getName());
                writer.newLine();
                //writerEXT.write(radiosList.get(i).getName() + " ");
                writer.write(radiosList.get(i).getUrl());
                writer.newLine();
                //writerEXT.write(radiosList.get(i).getUrl() + " ");
                writer.write(Integer.toString(radiosList.get(i).getIcon()));
                writer.newLine();
                //writerEXT.write(Integer.toString(radiosList.get(i).getIcon()));
            }
            writer.close();
            //writerEXT.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //function to check if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void slideToBottom() {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, playerLayout.getHeight());
        animate.setDuration(250);
        animate.setFillAfter(true);
        playerLayout.startAnimation(animate);
        //playerLayout.setVisibility(View.GONE);
    }

    public void slideToTop() {
        //playerLayout.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, playerLayout.getHeight(), 0);
        animate.setDuration(250);
        animate.setFillAfter(true);
        playerLayout.startAnimation(animate);
    }

    private void pageSelector(int pagePosition) {
        tabLayout.setScrollPosition(pagePosition, 0f, true);
        viewPager.setCurrentItem(pagePosition);
    }

    public int getPlayerDrawable() {
        return radiosList.get(finger).getIcon();
    }

    public String getPlayerName() {
        return radiosList.get(finger).getName();
    }

    public void setFinger(int finger) {
        this.finger = finger;
        ivImageSmall.setBackgroundResource(radiosList.get(finger).getIcon());
        tvDescription.setText(radiosList.get(finger).getName());
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            //return fragmentTitleList.get(position);
            return null;
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}

//TODO LIST
//
//TODO: RecordingNow fragment onView FIX
//TODO: Icon shape/size + 2 column grid view??
//TODO: Notification icons/buttons??
//TODO: Player color??
//TODO: FAB?? add/move custom radio??
//TODO: Recording stop by duration FIX + while(true) + broadcast current recording time/size?
//TODO: Recording http error arenafm kitkat FIX
//TODO: FolderRecordings + play with external player
//TODO: PlayingNow Controls + Volume + color??
//TODO: Log file
//TODO: Warnings (implementation by api version) FIX
//TODO: 
//TODO: Alarm (Schedule)
//TODO: Sleep timer
//TODO: Testing
//TODO: PlayStore
//TODO:
//TODO: Chat
//
