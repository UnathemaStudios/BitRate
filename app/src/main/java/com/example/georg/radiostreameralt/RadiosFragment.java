package com.example.georg.radiostreameralt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.StaticImageLoader;
import com.vstechlab.easyfonts.EasyFonts;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RadiosFragment extends Fragment {

    private ArrayList<Radio> radiosList;

    public RadiosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        radiosList = new ArrayList<>();
        radiosList.add(new Radio("1055 Rock", "http://46.4.121.138:8006/1055rock", R.
                drawable.ic_radios_logo_1055));
        radiosList.add(new Radio("InfinityGreece", "http://philae.shoutca.st:8307/stream", R
                .drawable.ic_radio_infinitygreece));
        radiosList.add(new Radio("Radio Nowhere", "http://philae.shoutca.st:8307/stream", R
                .drawable.ic_radio_nowhere));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radios, container, false);

        ImageView ivLogo = (ImageView)view.findViewById(R.id.ivLogo);
        TextView tvName = (TextView)view.findViewById(R.id.tvName);

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

        FunDapter adapter = new FunDapter(view.getContext(),radiosList, R.layout
                .grid_view_layout, dictionary );

        GridView radiosGrid = (GridView) view.findViewById(R.id.gridView);
        radiosGrid.setAdapter(adapter);

        return view;
    }

}
