package com.example.georg.radiostreameralt;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.coreui.*;
import android.support.coreui.BuildConfig;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.os.BuildCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FolderRecordings extends Fragment {

    private ArrayList<String> recFiles;
    private TextView tvRecordingsName;
    private ListView lvFolderRecordings;
	private FunDapter adapter;

    public FolderRecordings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".")+1), Thread.currentThread().getStackTrace()[2].getMethodName());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".")+1), Thread.currentThread().getStackTrace()[2].getMethodName());	
		return inflater.inflate(R.layout.fragment_folder_recordings, container, false);
    }
	
	@Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".")+1), Thread.currentThread().getStackTrace()[2].getMethodName());
		
		//Initializing the adapter and the dictionary
        tvRecordingsName = (TextView)getActivity().findViewById(R.id.tvFolderRecordingsName);
        lvFolderRecordings = (ListView)getActivity().findViewById(R.id.lvFolderRecordings);
		registerForContextMenu(lvFolderRecordings);
		
        BindDictionary<String> dictionary = new BindDictionary<>();
		dictionary.addStringField(R.id.tvFolderRecordingsName, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return item;
			}
		});
		
		adapter = new FunDapter(getContext(), recFiles, R.layout.rec_files_layout,
                dictionary);
		lvFolderRecordings.setAdapter(adapter);
		
		//Setting onItemClickListener
		lvFolderRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//do something knowing the recFolder.get(position)
				Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".")+1),
						Thread.currentThread().getStackTrace()[2].getMethodName());
				
				File file = new File(Environment.getExternalStorageDirectory()+"/Streams/",recFiles.get(position));
				
//				API<24 working
				MimeTypeMap myMime = MimeTypeMap.getSingleton();
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				String mimeType = myMime.getMimeTypeFromExtension(getExtension(file.getName()));
				newIntent.setDataAndType(Uri.fromFile(file.getAbsoluteFile()),mimeType);
				newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					getActivity().startActivity(newIntent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(getContext(), "No handler for this type of file.",
							Toast.LENGTH_LONG).show();
				}
				
//				API > 24 NOT working
//				MimeTypeMap myMime = MimeTypeMap.getSingleton();
//				Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
//						getActivity().getApplicationContext().getPackageName()+".provider", file.getAbsoluteFile());				
//				String mimeType = myMime.getMimeTypeFromExtension(getExtension(file.getName()));
//				Intent newIntent = new Intent(Intent.ACTION_VIEW);
//				newIntent.setDataAndType(uri,mimeType);
//				newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//				newIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//				newIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//				newIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//				getActivity().startActivity(newIntent);
			}
        });
		recFiles = new ArrayList<>();
		File[] Files = new File(Environment.getExternalStorageDirectory().toString()+"/Streams").listFiles();
		for (File file : Files)
			if (!file.isDirectory())
			{
				Log.w(file.getName(), getExtension(file.getName()));
				recFiles.add(file.getName());
			}
		adapter.updateData(recFiles);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.lvFolderRecordings) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.folder_context_menu, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case R.id.delete:
				new File (Environment.getExternalStorageDirectory().toString()+"/Streams",recFiles.get(info.position)).delete();
				recFiles.remove(info.position);
				adapter.updateData(recFiles);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	public String getExtension(String fileName)
	{
		int index = fileName.lastIndexOf('.');
		if (index != -1)
		{
			return fileName.substring(index+1);
		}
		else return null;
	}
}
