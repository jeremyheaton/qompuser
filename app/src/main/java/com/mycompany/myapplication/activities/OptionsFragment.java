package com.mycompany.myapplication.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycompany.myapplication.R;
import com.mycompany.myapplication.helpers.Helpers;

import java.util.Objects;

public class OptionsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.options_fragment, container, false);
        return rootView;
    }

    public void createQR(String userId) {
        String url = "https://ancient-tor-6266.herokuapp.com/client/" + userId;
        Bitmap urlQR = Helpers.encodeToQrCode(url, 512, 512);
        ImageView imageView1 = (Objects.requireNonNull(getView()).findViewById(R.id.imageView1));
        imageView1.setImageBitmap(urlQR);
        TextView t = getView().findViewById(R.id.urlHolder);
        t.setText(url);
    }
}