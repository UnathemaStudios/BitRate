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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
	
	private boolean isPaused = false;	
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
					playerLoading();
					//pause player
					send("PAUSE_STREAM"); //broadcast PAUSE (for media player)	
					isPaused = true;
                }
                else if(playing == 0||playing == 2){
					playerLoading();
					//start player
					if (!isPaused) //if media player isn't paused
					{
						//start media player service
						Intent playIntent = new Intent(MainActivity.this, MediaPlayerService.class);
						playIntent.putExtra("urlString", "http://philae.shoutca.st:8307/stream");
						startService(playIntent);
		
					}
					else //if it is paused
					{
						send("RESUME_STREAM"); //broadcast RESUME (for media player)
						isPaused = false;
					}
                }
            }
        });
		
        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing == 2||playing == 1){
					playerLoading();
					//stop
					send("STOP_STREAM"); //broadcast STOP (for media player)
					isPaused = false;
                }
            }
        });
		//start listening for broadcasts
		if (serviceReceiver != null)
		{
			IntentFilter timeFilter = new IntentFilter("TIME_UPDATE");
			IntentFilter stopFilter = new IntentFilter("0");
			IntentFilter loadingFilter = new IntentFilter("1");
			IntentFilter playingFilter = new IntentFilter("2");
			IntentFilter pausedFilter = new IntentFilter("3");
			IntentFilter recordingKeyAdd = new IntentFilter("RECORDING_ADDED");
			IntentFilter recordingKeyRemove = new IntentFilter("RECORDING_STOPPED");
			registerReceiver(serviceReceiver, timeFilter);
			registerReceiver(serviceReceiver, stopFilter);
			registerReceiver(serviceReceiver, loadingFilter);
			registerReceiver(serviceReceiver, playingFilter);
			registerReceiver(serviceReceiver, pausedFilter);
			registerReceiver(serviceReceiver, recordingKeyAdd);
			registerReceiver(serviceReceiver, recordingKeyRemove);
		}
	
		if (isMyServiceRunning(MediaPlayerService.class)) //IF mediaplayer service is running
		{
			send("REQUEST_STATUS"); //request mediaplayer service status
		}
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
				//ui after Paused
				isPaused = true;
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
	private void checkForWritePermissionAndRec()
	{
		if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
		{
//			Log.w("Main", "Permission (Write to external storage) already granted");
//			if (duration.length() != 0)
//			{
//				recorder("RECORD", spinner.getSelectedItem().toString(), Long.parseLong(duration.getText().toString()));
//			}
//			else
//			{
//				recorder("RECORD", spinner.getSelectedItem().toString(), -1);
//			}
		}
		else
		{
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
//				Log.w("Main", "Permission (Write to external storage) just granted");
//				if (duration.length() != 0)
//				{
//					recorder("RECORD", spinner.getSelectedItem().toString(), Long.parseLong(duration.getText().toString()));
//				}
//				else
//				{
//					recorder("RECORD", spinner.getSelectedItem().toString(), -1);
//				}
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
		MainActivity.this.startService(serviceIntent);
	}
	public void recorder(String action, int key)
	{
		Intent serviceIntent = new Intent(MainActivity.this, Recorder.class);
		serviceIntent.putExtra("Action", action);
		serviceIntent.putExtra("key", key);
		MainActivity.this.startService(serviceIntent);
	}
	
	//send function to broadcast an action
	public void send(String actionToSend)
	{
		Intent intent = new Intent();
		intent.setAction(actionToSend);
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


