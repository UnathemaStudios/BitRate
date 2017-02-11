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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
    private int playing; //0=stopped 1=playing 2=paused
    private RelativeLayout playerLayout;
    private String currentUrl;
    private String currentRadioName;
    private int currentRadioDrawable;
    private int durationtmp;
    private boolean backPressed = false;
    private boolean playerVisible = false;
    private File radiosFile;
	private BufferedReader bufferedReader;


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

		playerLayout = (RelativeLayout)findViewById(R.id.relativeLayout2);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
                                .colorAccent);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

                        if (tabLayout.getSelectedTabPosition() == 0 && playerVisible){
                            slideToBottom();
                            playerVisible = false;
                        }
                        else if(tabLayout.getSelectedTabPosition() != 0 && !playerVisible){
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
//                        int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color
//                                .colorAccent);
//                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }
                }
        );

        pageSelector(1);

        //-//-//-//- Tabs Ended -//-//-//-//-//

        ibPPbutton = (ImageButton)findViewById(R.id.ibPPbutton);
        ivImageSmall = (ImageView)findViewById(R.id.ivImagePlayBar);
        tvDescription = (TextView)findViewById(R.id.tvDescription);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        playing = 0;
        if(playing == 0||playing == 2) {
            ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        }
        else  ibPPbutton.setBackgroundResource(R.drawable.ic_stop);

        ibPPbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing==1){
                    disableButtons();
                    //STOP player
                    send("STOP_STREAM"); //broadcast STOP (for media player)
                    playing = 0;
                }
                else if(playing == 0||playing == 2){
                    disableButtons();
                    //start player
                    if (playing==0) //if media player isn't paused
                    {
                        if(!isMyServiceRunning(MediaPlayerService.class)) {
                            //start media player service
                            Intent playIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                            playIntent.putExtra("urlString", "http://philae.shoutca.st:8307/stream");
                            startService(playIntent);
                            currentUrl = "http://philae.shoutca.st:8307/stream";
                            currentRadioName = "InfinityGreece";
                        }
                        else{
                            send("RESUME_STREAM");
                            playing = 1;
                        }

                    }
                    else //if it is paused
                    {
                        send("RESUME_STREAM"); //broadcast RESUME (for media player)
                        playing = 1;
                    }
                }
            }
        });
        //start listening for broadcasts
        if (serviceReceiver != null)
        {
            registerReceiver(serviceReceiver, new IntentFilter("TIME_UPDATE"));
            registerReceiver(serviceReceiver, new IntentFilter("0"));
            registerReceiver(serviceReceiver, new IntentFilter("1"));
            registerReceiver(serviceReceiver, new IntentFilter("2"));
            registerReceiver(serviceReceiver, new IntentFilter("3"));
            registerReceiver(serviceReceiver, new IntentFilter("RECORDING_ADDED"));
            registerReceiver(serviceReceiver, new IntentFilter("RECORDING_STOPPED"));
            registerReceiver(serviceReceiver, new IntentFilter("radioToPlay"));
            registerReceiver(serviceReceiver, new IntentFilter("REC_CURRENT"));
            registerReceiver(serviceReceiver, new IntentFilter("PLAYING_NOW_UPDATE"));
        }

        if (isMyServiceRunning(MediaPlayerService.class)) //IF mediaplayer service is running
        {
            send("REQUEST_STATUS"); //request mediaplayer service status
        }
        //--/---/--/--/-/-/-/-/-/--/--/--/-Files Testing//-//-//-/-/-/-/-////-/-/-/

        radiosFile = new File(getApplicationContext().getFilesDir(), "RadiosList.txt");
        try {
            bufferedReader = new BufferedReader(new FileReader(radiosFile));
            String str;
            while((str=bufferedReader.readLine())!=null){
                System.out.println(str);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void playerStop(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        ibPPbutton.setEnabled(true);
        playing = 0;
    }

    private void playerPlay(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_stop);
        ibPPbutton.setEnabled(true);
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        playing = 1;
    }

    private void playerPause(){
        ibPPbutton.setBackgroundResource(R.drawable.ic_play);
        playing = 2;
        ibPPbutton.setEnabled(true);
    }

    private void disableButtons(){
        ibPPbutton.setEnabled(false);
//        findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this,"start", Toast.LENGTH_SHORT).show();
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

    //Broadcast Receiver
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("TIME_UPDATE")) //if TIME_UPDATE is received
            {
//				double time;
//				time = intent.getDoubleExtra("time", 0); //set time received to time variable, if its empty put 0
//
//				//change current time text field to XX:XX format
//				currenttime.setText(String.format(Locale.US,"%02d:%02d",
//						TimeUnit.MILLISECONDS.toMinutes((long) time),
//						TimeUnit.MILLISECONDS.toSeconds((long) time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) time))));
            }
            else if (intent.getAction().equals("0")) //if 0 (STOPPED) is received
            {
                //ui after Stopped
                playerStop();
            }
            else if (intent.getAction().equals("1")) //if 1 (LOADING) is received
            {
                //ui after Loading
                findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
//				isPaused = false;
//				play.setEnabled(false);
//				pause.setEnabled(false);
//				stop.setEnabled(false);
//
//				status.setText(R.string.status_loading);
            }
            else if (intent.getAction().equals("2")) //if 2 (PLAYING) is received
            {
                //ui after Resume
                playerPlay();
            }
            else if (intent.getAction().equals("3")) //if 3 (PAUSED) is received
            {
                //ui after Pause
                playerPause();
            }
            else if (intent.getAction().equals("RECORDING_ADDED"))
            {
                //Toast.makeText(getApplicationContext(), String.valueOf(currentkey),Toast.LENGTH_LONG).show();
//				activeRecordingList.add(String.valueOf(intent.getIntExtra("key", -1)));
//				dataAdapter2.notifyDataSetChanged();
            }
            else if (intent.getAction().equals("RECORDING_STOPPED"))
            {
//				int currentkey = intent.getIntExtra("key", -1);
//				for (int i=0; i < activeRecordingList.size(); i++)
//				{
//					if (Integer.parseInt(activeRecordingList.get(i)) == currentkey)
//					{
//						activeRecordingList.remove(i);
//						dataAdapter2.notifyDataSetChanged();
//					}
//				}

            }
			else if (intent.getAction().equals("radioToPlay"))
			{
				Toast.makeText(getApplicationContext(), intent.getStringExtra("urlString"), Toast.LENGTH_SHORT).show();
				currentUrl = intent.getStringExtra("urlString");
				disableButtons();
				send("CLOSE");
				
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run()
					{
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								int i = 0;
								while (isMyServiceRunning(MediaPlayerService.class))
								{
									Log.w("Waiting Service to Stop", Integer.toString(i));
									i++;
								}
								//start media player service
								Intent playIntent = new Intent(MainActivity.this, MediaPlayerService.class);
								playIntent.putExtra("urlString", currentUrl);
								startService(playIntent);
							}
						}).start();
					}
				},50);
                currentRadioName = intent.getStringExtra("radioName");
                currentRadioDrawable = intent.getIntExtra("imageID",R.color.transparent);
                ivImageSmall.setBackgroundResource(currentRadioDrawable);
                tvDescription.setText(currentRadioName);
            }
            else if(intent.getAction().equals("REC_CURRENT")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    //check for permission first and then rec
                    checkForWritePermissionAndRec(0);
                }
                else
                {
                  rec(currentUrl, 0);
                }
            }
            else if(intent.getAction().equals("PLAYING_NOW_UPDATE")){
                send("PLAYING_NOW_STATUS", currentRadioName, currentRadioDrawable);
            }
        }
    };
	
    @Override
    protected void onRestart()
    {
        if (isMyServiceRunning(MediaPlayerService.class)) //if media player service is running
        {
            send("REQUEST_STATUS"); //request media player status
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(serviceReceiver); //stop listening for broadcasts
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkForWritePermissionAndRec(int duration)
    {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
			Log.w("Main", "Permission (Write to external storage) already granted");
            rec(currentUrl, duration);
        }
        else
        {
            durationtmp = duration;
            String permissionRequested[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissionRequested, 5);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
				Log.w("Main", "Permission (Write to external storage) just granted");
				rec(currentUrl,durationtmp);
            }
            else
            {
                Log.w("Main", "Permission (Write to external storage) denied");
            }
        }
    }

    public void recorder(String action, String urlString, long duration)
    {
            Intent serviceIntent = new Intent(MainActivity.this, Recorder.class);
            serviceIntent.putExtra("Action", action);
            serviceIntent.putExtra("urlString", urlString);
            serviceIntent.putExtra("duration", duration);
            serviceIntent.putExtra("name", currentRadioName);
            MainActivity.this.startService(serviceIntent);
    }
    public void recorder(String action, int key)
    {
        Intent serviceIntent = new Intent(MainActivity.this, Recorder.class);
        serviceIntent.putExtra("Action", action);
        serviceIntent.putExtra("key", key);
        MainActivity.this.startService(serviceIntent);
    }
    public void rec(String url, int duration){

        if (duration != 0)
        {
            recorder("RECORD", url, duration);
        }
        else
        {
            recorder("RECORD", url, -1);
        }
    }

    //send function to broadcast an action
    public void send(String actionToSend)
    {
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        sendBroadcast(intent);
    }
    public void send(String actionToSend, String name, int drawable){
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        intent.putExtra("name", name);
        intent.putExtra("drawable", drawable);
        sendBroadcast(intent);
    }

    //function to check if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    public void slideToBottom(){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,playerLayout.getHeight());
        animate.setDuration(250);
        animate.setFillAfter(true);
        playerLayout.startAnimation(animate);
        //playerLayout.setVisibility(View.GONE);
    }

    public void slideToTop(){
        //playerLayout.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0,0,playerLayout.getHeight(),0);
        animate.setDuration(250);
        animate.setFillAfter(true);
        playerLayout.startAnimation(animate);
    }

    private void pageSelector(int pagePosition){
        tabLayout.setScrollPosition(pagePosition,0f,true);
        viewPager.setCurrentItem(pagePosition);
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
