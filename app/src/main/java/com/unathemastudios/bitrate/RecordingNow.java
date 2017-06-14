package com.unathemastudios.bitrate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.amigold.fundapter.interfaces.StaticImageLoader;
import com.unathemastudios.bitrate.bitrate.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RecordingNow extends Fragment
{
	@Override
	public void onDestroy()
	{
		getActivity().unregisterReceiver(serviceReceiver);
		super.onDestroy();
	}
	
	private ArrayList<RecordingRadio> recordingNowRadios = new ArrayList<>();
	private FunDapter adapter;
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals("recList"))
			{
				HashMap<Integer, Recording> rec = new HashMap<>();
				rec = (HashMap<Integer, Recording>) intent.getSerializableExtra("recHashMap");
				((MainActivity)getActivity()).isRecordedStatusFalseAll();
				if (rec != null)
				{
					recordingNowRadios.clear();
					for (Map.Entry<Integer, Recording> entry : rec.entrySet())
					{
						if (entry != null)
						{

							if(entry.getValue().getStatus()!= MainService.NOTRECORDING) {
								recordingNowRadios.add(new RecordingRadio(entry.getValue()
										.getName(), entry.getKey(), entry.getValue()
										.getCurrentRecordingTimeInSeconds(), entry.getValue()
										.getCurrentSizeInKB(), entry.getValue().getDuration()));
								((MainActivity)getActivity()).setIsRecordedStatus(true, entry
										.getValue().getName());
							}
						}
						else
						{
							Log.w("MainActivity", "HashMap entry is null");
						}
					}
					((MainActivity)getActivity()).setBadgeCount(recordingNowRadios.size());
					adapter.updateData(recordingNowRadios);
				}
				else
				{
					Log.w("MainActivity", "HashMap is null");
				}
			}
		}
	};
	
	public RecordingNow()
	{
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (serviceReceiver != null)
		{
			getActivity().registerReceiver(serviceReceiver, new IntentFilter("recList"));			
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_recording_now, container, false);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

//		TextView tvrecordingNowRadio = (TextView) getActivity().findViewById(R.id.tvRecordingNow);
		
		BindDictionary<RecordingRadio> dictionary = new BindDictionary<>();
		dictionary.addStringField(R.id.tvRecordingNow, new StringExtractor<RecordingRadio>()
		{
			@Override
			public String getStringValue(RecordingRadio item, int position)
			{
				return item.getName();
			}
		});
		dictionary.addStringField(R.id.tvRecordingNowSize, new StringExtractor<RecordingRadio>()
		{
			@Override
			public String getStringValue(RecordingRadio item, int position)
			{
				return prettySize(item.getSize());
			}
		});
		dictionary.addStringField(R.id.tvRecordingNowTime, new StringExtractor<RecordingRadio>()
		{
			@Override
			public String getStringValue(RecordingRadio item, int position)
			{
				if(item.getDuration()==-60) return prettyTime(item.getTime()) + "/--:--";
				else return prettyTime(item.getTime()) + "/" + prettyTime(item.getDuration());
			}
		});
		dictionary.addStaticImageField(R.id.ivRecordingNowClose, new StaticImageLoader() {
			@Override
			public void loadImage(Object item, ImageView imageView, int position) {
				imageView.setImageResource(R.drawable.ic_close_black_24dp);
			}
		}).onClick(new ItemClickListener<RecordingRadio>() {
			@Override
			public void onClick(RecordingRadio item, int position, View view) {
				((MainActivity) getActivity()).tellServiceR("STOP_RECORD", recordingNowRadios.get(position).getId());
			}
		});
		
		adapter = new FunDapter<>(view.getContext(), recordingNowRadios, R.layout.recording_now_layout, dictionary);
		
		ListView listView = (ListView) getActivity().findViewById(R.id.recordingNowListView);
		listView.setAdapter(adapter);
		
		/*listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				((MainActivity) getActivity()).tellServiceR("STOP_RECORD", recordingNowRadios.get(position).getId());
			}
		});*/
	}
	
	public String prettyTime(long timeInSeconds)
	{
		return String.format(Locale.US, "%02d:%02d", timeInSeconds / 60, timeInSeconds % 60);
	}
	
	public String prettySize(int sizeInKB)
	{
		if (sizeInKB < 1024)
		{
			return String.valueOf(sizeInKB) + " KB ";
		} else /*if (sizeInKB<1048576)*/
		{
			return String.format(Locale.US, "%.2f", (double) sizeInKB / 1024) + " MB ";
		}
	}
}

//	//function to check if a service is running

//	private boolean isMyServiceRunning(Class<?> serviceClass)
//	{
//		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
//		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
//		{
//			if (serviceClass.getName().equals(service.service.getClassName()))
//			{
//				return true;
//			}
//		}
//		return false;
//	}
