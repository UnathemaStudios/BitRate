package com.example.georg.radiostreameralt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

class MetadataThread extends Thread
{
	private Context context;
	private String metadataUrl;
	private String streamTitle="";
	
	MetadataThread(String metadataUrl, Context context) {
		this.metadataUrl = metadataUrl;
		this.context = context;
	}
	@Override
	public void run()
	{
		IcyStreamMeta streamMeta = new IcyStreamMeta();
		try {
			streamMeta.setStreamUrl(new URL(metadataUrl));
			streamMeta.refreshMeta();
			Log.w("METADATA", streamMeta.getStreamTitle());
//			Log.w("METADATA", streamMeta.getArtist());
//			Log.w("METADATA", streamMeta.getTitle());
//			Log.w("METADATA", String.valueOf(streamMeta.getMetadata()));
//			Log.w("METADATA", String.valueOf(streamMeta.getStreamUrl()));
			
			streamTitle = streamMeta.getStreamTitle();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		Intent intent = new Intent();
		intent.setAction("metadataBroadcast");
		intent.putExtra("streamTitle", streamTitle);
		context.sendBroadcast(intent);
		
	}
}
//"http://s10.voscast.com:9940"
