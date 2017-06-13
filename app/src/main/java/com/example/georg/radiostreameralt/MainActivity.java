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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final int STOPPED = 0;
	private static final int LOADING = 1;
	private static final int PLAYING = 2;
	public ArrayList<Radio> radiosList;
	private ViewPager viewPager;
	private TabLayout tabLayout;
	private RadiosFragment radiosFragment;
	private PlayingNowFragment playingNowFragment;
	private RecordFragment recordFragment;
	private ImageButton ibPPbutton;
	private ImageView ivImageSmall;
	private TextView tvDescription;
	private int finger = 0;
	private File radiosFile;
	private File radiosFileEXTdir;
	private File radiosFileEXT;
	private int playing;
	private RelativeLayout playerLayout;
	private int durationtmp;
	private boolean backPressed = false;
	private boolean playerVisible = false;
	//Broadcast Receiver
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("0")) //if 0 (STOPPED) is received
			{
				//ui after Stopped
				playerStop();
				playingNowFragment.setSleepText(0);
			} else if (intent.getAction().equals("1")) //if 1 (LOADING) is received
			{
				findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
				ibPPbutton.setVisibility(View.INVISIBLE);
				playing = LOADING;
				playingNowFragment.setPPButtonStatus(LOADING, radiosList.get(finger).isRecorded());
			} else if (intent.getAction().equals("2")) //if 2 (PLAYING) is received
			{
				playerPlay();
			} else if (intent.getAction().equals("SET_FINGER")) {
				setFinger(intent.getIntExtra("finger", -1));
			} else if (intent.getAction().equals("timeRemaining")) {
				playingNowFragment.setSleepText(intent.getIntExtra("timeRemainingInt", -999));
			}
		}
	};
	
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
		tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
			
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				super.onTabSelected(tab);
				int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color.colorAccent);
				tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
				
				if (tabLayout.getSelectedTabPosition() == 0 && playerVisible) {
					slideToBottom();
					playerVisible = false;
				} else if (tabLayout.getSelectedTabPosition() != 0 && !playerVisible) {
					slideToTop();
					playerVisible = true;
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				super.onTabUnselected(tab);
				int tabIconColor = ContextCompat.getColor(MainActivity.this.getApplicationContext(), R.color.textColorPrimary);
				tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				super.onTabReselected(tab);
			}
		});
		viewPager.setOffscreenPageLimit(2);
		pageSelector(1);
		
		//load default radio list from XML
		radiosList = new ArrayList<>();
		try {
			XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = xppFactory.newPullParser();
			xmlPullParser.setInput(getApplicationContext().getResources().openRawResource(R.raw.defaultradios), "utf-8");
			
			String name = null;
			String url = null;
			String logo = null;
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						
						if (xmlPullParser.getName().equals("Name")) {
							name = xmlPullParser.nextText();
						}
						
						if (xmlPullParser.getName().equals("Url")) {
							url = xmlPullParser.nextText();
						}

						if (xmlPullParser.getName().equals("Logo"))
						{
							logo = xmlPullParser.nextText();
						}

						if (xmlPullParser.getName().equals("Description")){
							String description = xmlPullParser.nextText();
							radiosList.add(new Radio(name, url, logo, false, description));
						}
						
						break;
					case XmlPullParser.END_TAG:
//						Log.w("getName", xmlPullParser.getName());						
						break;
					default:
						break;
				} // end switch
				
				// Move forward the parsing "cursor", or you can stop parsing
				eventType = xmlPullParser.next();
				
			} // end whiles
			
			
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		//load user radio list from XML if exists
		File directory = new File(String.valueOf(getApplicationContext().getFilesDir()));
		File outputSource = new File(directory, "userradios.xml");
		if (outputSource.exists())
		{
			try {
				XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlPullParser = xppFactory.newPullParser();
				xmlPullParser.setInput(new FileInputStream(outputSource), "utf-8");
				
				String name = null;
				String url = null;
				String logo = null;
				int eventType = xmlPullParser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
						case XmlPullParser.START_TAG:
							
							if (xmlPullParser.getName().equals("Name")) {
								name = xmlPullParser.nextText();
							}
							
							if (xmlPullParser.getName().equals("Url")) {
								url = xmlPullParser.nextText();
							}
							
							if (xmlPullParser.getName().equals("Logo"))
							{
								logo = xmlPullParser.nextText();
							}

							if (xmlPullParser.getName().equals("Description")){
								String description = xmlPullParser.nextText();
								radiosList.add(new Radio(name, url, logo, false, description));
							}

							break;
						case XmlPullParser.END_TAG:
//						Log.w("getName", xmlPullParser.getName());						
							break;
						default:
							break;
					} // end switch
					
					// Move forward the parsing "cursor", or you can stop parsing
					eventType = xmlPullParser.next();
					
				} // end whiles
				
				
			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
		//-//-//-//- Tabs Ended -//-//-//-//-//
		
		ibPPbutton = (ImageButton) findViewById(R.id.ibPPbutton);
		ivImageSmall = (ImageView) findViewById(R.id.ivImagePlayBar);
		tvDescription = (TextView) findViewById(R.id.tvDescription);
		findViewById(R.id.loadingLayout).setVisibility(View.INVISIBLE);
		playing = STOPPED;
		ibPPbutton.setImageResource(R.drawable.ic_play);
		
		ibPPbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playing == PLAYING) {
					stop();
				} else if (playing == STOPPED) {
					play();
				}
			}
		});
		//start listening for broadcasts
		if (serviceReceiver != null) {
			registerReceiver(serviceReceiver, new IntentFilter("0"));
			registerReceiver(serviceReceiver, new IntentFilter("1"));
			registerReceiver(serviceReceiver, new IntentFilter("2"));
			registerReceiver(serviceReceiver, new IntentFilter("SET_FINGER"));
			registerReceiver(serviceReceiver, new IntentFilter("timeRemaining"));
		}
		
		if (isMyServiceRunning(MainService.class)) {
			tellServiceP("REQUEST_PLAYER_STATUS");
		} else setFinger(1);
	}
	
	void loadUserRadiosToXML()
	{
		int numberOfUserListEntries = 0;
		FileOutputStream fileOutputStream = null;
		File directory = new File(String.valueOf(getApplicationContext().getFilesDir()));
		if (!directory.exists())
		{
			boolean directoryCreated = directory.mkdirs();
			Log.w("Recorder", "Created directory");
			if (!directoryCreated)
			{
				Log.w("Recorder", "Failed to create directory");
			}
		}
		
		File outputSource = new File(directory, "userradios.xml");
		
		try
		{
			fileOutputStream = new FileOutputStream(outputSource);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try
		{
			xmlSerializer.setOutput(writer);
			xmlSerializer.startDocument("UTF-8", true);
			xmlSerializer.startTag(null, "Radios");
			
			for (Radio entry : radiosList) {
				
				if (entry.isMadeByUser())
				{
					xmlSerializer.startTag(null, "Name");
					xmlSerializer.text(entry.getName());
					xmlSerializer.endTag(null, "Name");
					
					xmlSerializer.startTag(null,"Url");
					xmlSerializer.text(entry.getUrl());
					xmlSerializer.endTag(null, "Url");
					
					xmlSerializer.startTag(null,"Logo");
					xmlSerializer.text("defaultradio");
					xmlSerializer.endTag(null,"Logo");
					
					numberOfUserListEntries++;
				}
			}
			
			xmlSerializer.endTag(null, "Radios");
			xmlSerializer.endDocument();
			xmlSerializer.flush();
			String dataWrite = writer.toString();
			assert fileOutputStream != null;
			fileOutputStream.write(dataWrite.getBytes());
			fileOutputStream.close();
		}catch (IllegalArgumentException | IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		
		if (numberOfUserListEntries ==0)
		{
			outputSource.delete();
			Log.w("userradios.xml", "deleted");
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
		} else super.onBackPressed();
	}
	
	public void play() {
		disableButtons();
		playingNowFragment.disableButtons(true);
		tellServiceP("PLAYER_PLAY", radiosList.get(finger).getUrl(), finger);
	}
	
	public void stop() {
		disableButtons();
		playingNowFragment.disableButtons(true);
		tellServiceP("PLAYER_STOP");
	}
	
	private void playerStop() {
		ibPPbutton.setVisibility(View.VISIBLE);
		ibPPbutton.setImageResource(R.drawable.ic_play);
		ibPPbutton.setEnabled(true);
		playing = STOPPED;
		findViewById(R.id.loadingLayout).setVisibility(View.INVISIBLE);
		playingNowFragment.setPPButtonStatus(STOPPED, radiosList.get(finger).isRecorded());
	}
	
	private void playerPlay() {
		ibPPbutton.setVisibility(View.VISIBLE);
		ibPPbutton.setImageResource(R.drawable.ic_stop);
		ibPPbutton.setEnabled(true);
		findViewById(R.id.loadingLayout).setVisibility(View.INVISIBLE);
		playing = PLAYING;
		playingNowFragment.setPPButtonStatus(PLAYING, radiosList.get(finger).isRecorded());
	}
	
	public void disableButtons() {
		ibPPbutton.setEnabled(false);
		ibPPbutton.setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
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
	
	@Override
	protected void onRestart() {
		if (isMyServiceRunning(MainService.class)) {
			tellServiceP("REQUEST_STATUS");
		}
//		Log.w("MAINACTIVITY", "RESTART");
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(serviceReceiver);
//		Log.w("MAINACTIVITY", "DESTROYED");
		super.onDestroy();
	}
	
	public void recordCurrentRadio(int duration) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Log.w("Main", "Permission (Write to external storage) already granted");
				rec(radiosList.get(finger).getUrl(), duration);
			} else {
				durationtmp = duration;
				String permissionRequested[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
				requestPermissions(permissionRequested, 5);
			}
		} else {
			rec(radiosList.get(finger).getUrl(), duration);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 5) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.w("Main", "Permission (Write to external storage) just granted");
				rec(radiosList.get(finger).getUrl(), durationtmp);
			} else {
				Log.w("Main", "Permission (Write to external storage) denied");
			}
		}
	}
	
	public void rec(String url, int duration) {
		if (duration != 0) {
			tellServiceR("RECORD", url, duration);
		} else {
			tellServiceR("RECORD", url, -1);
		}
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
	
	public void tellServiceT(String action, int sleepTime) {
		Intent serviceIntent = new Intent(this, MainService.class);
		serviceIntent.setAction(action);
		serviceIntent.putExtra("sleepTime", sleepTime);
		startService(serviceIntent);
	}
	
	//function to check if a service is running
	public boolean isMyServiceRunning(Class<?> serviceClass) {
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
		findViewById(R.id.relativeLayoutWrapper).setVisibility(View.GONE);
	}
	
	public void slideToTop() {
		findViewById(R.id.relativeLayoutWrapper).setVisibility(View.VISIBLE);
		TranslateAnimation animate = new TranslateAnimation(0, 0, playerLayout.getHeight(), 0);
		animate.setDuration(500);
		animate.setFillAfter(true);
		playerLayout.startAnimation(animate);
	}
	
	private void pageSelector(int pagePosition) {
		tabLayout.setScrollPosition(pagePosition, 0f, true);
		viewPager.setCurrentItem(pagePosition);
	}
	
	public String getPlayerDrawable() {
		return radiosList.get(finger).getLogo();
	}
	
	public String getPlayerName() {
		return radiosList.get(finger).getName();
	}
	
	public String getPlayerUrl() {
		return radiosList.get(finger).getUrl();
	}
	
	public void setFinger(int finger) {
		this.finger = finger;
		ivImageSmall.setImageResource(getResources().getIdentifier(radiosList.get(finger).getLogo(),"raw",getApplicationContext().getPackageName()));
		tvDescription.setText(radiosList.get(finger).getName());
	}
	
	public int getPlaying() {
		return playing;
	}
	
	public void setIsRecordedStatus(boolean status) {
		radiosList.get(finger).setRecorded(status);
		setPlayingNowIsRecorded();
	}
	
	public void setIsRecordedStatus(boolean status, String Name) {
		for (int i = 0; i < radiosList.size(); i++) {
			if (radiosList.get(i).getName().equals(Name)) {
				radiosList.get(i).setRecorded(status);
			}
		}
		setPlayingNowIsRecorded();
	}
	
	public void isRecordedStatusFalseAll() {
		for (Radio entry : radiosList) {
			entry.setRecorded(false);
		}
		setPlayingNowIsRecorded();
	}
	
	public void setPlayingNowIsRecorded() {
		playingNowFragment.setPPButtonStatus(playing, radiosList.get(finger).isRecorded());
	}
	
	public void setBadgeCount(int recordings) {
		recordFragment.setBadgeCount(recordings);
	}
	
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
//TODO: chceckbox instead of radiogroup in record dialog
//TODO: VLC / UNICODE METADATA
//TODO: License
//
//TODO: Notification Custom
//TODO: Http raspberry file for the radios(not locally saved file)
//TODO: Log file
//TODO: Alarm (Schedule)
//TODO: Testing
//TODO: PlayStore
//TODO: Chat
//
