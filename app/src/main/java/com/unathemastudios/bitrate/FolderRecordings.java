package com.unathemastudios.bitrate;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FolderRecordings extends Fragment implements ConfirmationDialog.NoticeDialogListener {

	private ArrayList<String> recFiles;
	private FunDapter adapter;

	public FolderRecordings() {
		// Required empty public constructor
		//Empty Constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_folder_recordings, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".") + 1), Thread.currentThread().getStackTrace()[2].getMethodName());

		//Initializing the adapter and the dictionary
//		TextView tvRecordingsName = (TextView) getActivity().findViewById(R.id.tvFolderRecordingsName);
		ListView lvFolderRecordings = (ListView) getActivity().findViewById(R.id.lvFolderRecordings);
		registerForContextMenu(lvFolderRecordings);

		BindDictionary<String> dictionary = new BindDictionary<>();
		dictionary.addStringField(R.id.tvFolderRecordingsName, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return recordingNameFromFileName(item);
			}
		});
		dictionary.addStringField(R.id.tvFolderRecordingsDate, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return recordingDateFromFilename(item);
			}
		});
		dictionary.addStringField(R.id.tvFolderRecordingsRuntime, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return recordingTimeFromFilename(item);
			}
		});
		dictionary.addStringField(R.id.tvFolderRecordingsSize, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return (prettySize((new File(Environment.getExternalStorageDirectory().toString() +
						"/Streams/" + item).length()) / 1024));
			}
		});

		adapter = new FunDapter(getContext(), recFiles, R.layout.rec_files_layout, dictionary);
		lvFolderRecordings.setAdapter(adapter);
		

		//Setting onItemClickListener
		lvFolderRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//do something knowing the recFolder.get(position)
				Log.w(this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".") + 1),
						Thread.currentThread().getStackTrace()[2].getMethodName());

				File file = new File(Environment.getExternalStorageDirectory() + "/Streams/", recFiles.get(position));

//				API<24 working
				MimeTypeMap myMime = MimeTypeMap.getSingleton();
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				if (getExtension(file.getName()) == null) {
					Toast.makeText(getContext(), "This file has no extension.",
							Toast.LENGTH_LONG).show();
				}
				else {
					String mimeType = myMime.getMimeTypeFromExtension(getExtension(file.getName()));
					newIntent.setDataAndType(Uri.fromFile(file.getAbsoluteFile()), mimeType);
					newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					try {
						getActivity().startActivity(newIntent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(getContext(), "No handler for this type of file.",
								Toast.LENGTH_LONG).show();
					}
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
		File[] Files = new File(Environment.getExternalStorageDirectory().toString() + "/Streams").listFiles();
		for (File file : Files)
			if (!file.isDirectory() && Objects.equals(getExtension(file.getName()), "mp3")) {
				recFiles.add(file.getName());
			}
		adapter.updateData(recFiles);
		if(recFiles.isEmpty()) getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.VISIBLE);
		else getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.GONE);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.lvFolderRecordings) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.folder_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.delete_folder_recording:
				ConfirmationDialog confirmationDialog = new ConfirmationDialog();
				confirmationDialog.setTargetFragment(FolderRecordings.this, 5);
				confirmationDialog.show(getFragmentManager(), "napalm!", info.position);
				return true;
			case R.id.delete_all_folder_recording:
				ConfirmationDialog confirmationDialogAll = new ConfirmationDialog();
				confirmationDialogAll.setTargetFragment(FolderRecordings.this, 6);
				confirmationDialogAll.show(getFragmentManager(), "napalmAll!", -1);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	public String getExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index != -1) {
			return fileName.substring(index + 1);
		}
		else return null;
	}

	public String prettySize(long sizeInKB) {
		if (sizeInKB < 1024) {
			return String.valueOf(sizeInKB) + "KB ";
		}
		else /*if (sizeInKB<1048576)*/ {
			return String.format(Locale.US, "%.2f", (double) sizeInKB / 1024) + "MB ";
		}
	}

	String recordingNameFromFileName(String fileName) {

		return fileName.substring(0, fileName.length() - 4).substring(0, fileName.substring(0, fileName.length() - 4).length() - 12);
	}

	String recordingDateFromFilename(String fileName) {
		String dateUnformatted = fileName.substring(0, fileName.length() - 4).substring(fileName.substring(0, fileName.length() - 4).length() - 12);
		return dateUnformatted.substring(4, 6) + "/" + dateUnformatted.substring(2, 4) + "/" + 20 + dateUnformatted.substring(0, 2);
	}

	String recordingTimeFromFilename(String fileName) {
		String dateUnformatted = fileName.substring(0, fileName.length() - 4).substring(fileName.substring(0, fileName.length() - 4).length() - 12);
		return dateUnformatted.substring(6, 8) + ":" + dateUnformatted.substring(8, 10) + ":" + dateUnformatted.substring(10, 12);
	}

	@Override
	public void onDialogPositiveClick(int pos) {
		if(pos != -1) {
			new File(Environment.getExternalStorageDirectory().toString() + "/Streams",
					recFiles.get(pos)).delete();
			recFiles.remove(pos);
			adapter.updateData(recFiles);
			if(recFiles.isEmpty()) getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.VISIBLE);
			else getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.GONE);
		}
		else{
			File dir = new File(Environment.getExternalStorageDirectory().toString() +
					"/Streams");
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++) {
					new File(dir, children[i]).delete();
				}
				recFiles.clear();
				adapter.updateData(recFiles);
				if(recFiles.isEmpty()) getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.VISIBLE);
				else getActivity().findViewById(R.id.folderIsEmpty).setVisibility(View.GONE);
			}
		}
	}
}
