package com.giviews.giviewsmessenger._sliders;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.giviews.giviewsmessenger.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by bagicode on 12/04/17.
 */

public class FragmentSlider extends Fragment {

    private static final String ARG_PARAM1 = "params";

    private String imageUrls;

    public FragmentSlider() {
    }

    public static FragmentSlider newInstance(String params) {
        FragmentSlider fragment = new FragmentSlider();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imageUrls = getArguments().getString(ARG_PARAM1);

        Uri uri = Uri.parse("android.resource://com.giviews.giviewsmessenger/drawable/"+imageUrls);
        View view = inflater.inflate(R.layout.fragment_slider_item, container, false);
        final ImageView img = (ImageView) view.findViewById(R.id.img);
        Picasso.with(getActivity()).load(uri).placeholder(R.drawable.image_slider_1).into(img);
        return view;
    }
}