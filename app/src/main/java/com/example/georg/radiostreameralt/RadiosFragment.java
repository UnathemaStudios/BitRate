package com.example.georg.radiostreameralt;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.StaticImageLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RadiosFragment extends Fragment implements AddRadioDialog.NoticeDialogListener {

    private ArrayList<Radio> radiosList;
    private File radiosFile;
    private File radiosFileEXTdir;
    private File radiosFileEXT;
    private FloatingActionButton fabAddRadio;
    private FunDapter adapter;

    public RadiosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        radiosFile = new File(getContext().getFilesDir(), "RadiosList");
        radiosFileEXTdir = new File(Environment.getExternalStorageDirectory() + "/Streams");
        radiosFileEXT = new File(radiosFileEXTdir.getAbsolutePath(), "RadiosList.txt");
        radiosList = new ArrayList<>();
        Log.w("radiosFile.exists()", radiosFile.exists() + "");
        if (!radiosFile.exists()) {
            radiosList.add(new Radio("1055 Rock", "http://46.4.121.138:8006/1055rock", R.
                    drawable.ic_radios_logo_1055));
            radiosList.add(new Radio("InfinityGreece", "http://philae.shoutca.st:8307/stream", R
                    .drawable.ic_radio_infinitygreece));
            radiosList.add(new Radio("Radio Nowhere", "http://radio.arenafm.gr:45054/;stream.mp3", R
                    .drawable.ic_radio_nowhere));
            try {
                boolean fileCreated = radiosFile.createNewFile();
                Log.w("fileCreated", "" + fileCreated);
                if (!fileCreated)
                    Toast.makeText(getContext(), "FileNOTcreatedd", Toast.LENGTH_SHORT)
                            .show();
                saveToFIle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!radiosFileEXTdir.exists()) radiosFileEXTdir.mkdirs();
        if (!radiosFileEXT.exists()) {
            try {
                radiosFileEXT.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radios, container, false);

        try {
            readRadiosFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ImageView ivLogo = (ImageView) view.findViewById(R.id.ivLogo);
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        fabAddRadio = (FloatingActionButton) view.findViewById(R.id.fabAddRadio);

        BindDictionary<Radio> dictionary = new BindDictionary<>();

        dictionary.addStringField(R.id.tvName, new StringExtractor<Radio>() {
            @Override
            public String getStringValue(Radio item, int position) {
                return item.getName();
            }
        });

        dictionary.addStaticImageField(R.id.ivLogo, new StaticImageLoader<Radio>() {
            @Override
            public void loadImage(Radio item, ImageView imageView, int position) {
                imageView.setBackgroundResource(item.getIcon());
            }
        });

        adapter = new FunDapter(getContext(), radiosList, R.layout
                .grid_view_layout, dictionary);

        final GridView radiosGrid = (GridView) view.findViewById(R.id.gridView);
        radiosGrid.setAdapter(adapter);

        radiosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RadiosFragment.this.getContext(), radiosList.get(position).getName
                        () + " " + radiosList.get(position).getUrl(), Toast.LENGTH_SHORT).show();

                send("radioToPlay", radiosList.get(position).getUrl(), radiosList.get(position)
                        .getIcon(), radiosList.get(position).getName());
            }
        });

        radiosGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                radiosList.remove(position);
                adapter.updateData(radiosList);
                return false;
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabAddRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddRadioDialog addRadioDialog = new AddRadioDialog();
                addRadioDialog.setTargetFragment(RadiosFragment.this, 1);
                addRadioDialog.show(getFragmentManager(), "missiles");
            }
        });
    }

    @Override
    public void onDestroy() {
        saveToFIle();
        super.onDestroy();
    }

    //send function to broadcast an action
    public void send(String actionToSend, String url, int imageID, String radioName) {
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        intent.putExtra("urlString", url);
        intent.putExtra("imageID", imageID);
        intent.putExtra("radioName", radioName);
//        sendBroadcast(intent);
        getActivity().sendBroadcast(intent);
    }

    private void saveToFIle() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(radiosFile));
            //BufferedWriter writerEXT = new BufferedWriter(new FileWriter(radiosFileEXT));
            for (int i = 0; i < radiosList.size(); i++) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readRadiosFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(radiosFile));
        radiosList.clear();
        String name, url;
        int icon;
        while ((name = reader.readLine()) != null) {
            url = reader.readLine();
            icon = Integer.parseInt(reader.readLine());
            radiosList.add(new Radio(name, url, icon));
            System.out.println(name + url + icon);
        }
        reader.close();
    }

    @Override
    public void onDialogPositiveClick(String name, String url) {
        radiosList.add(new Radio(name, url, R.drawable.ic_radio));
        adapter.updateData(radiosList);
    }
}