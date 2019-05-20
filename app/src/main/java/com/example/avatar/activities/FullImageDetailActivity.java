package com.example.avatar.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.avatar.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageDetailActivity extends AppCompatActivity {
    @BindView(R.id.img_full)
    ImageView mImage;
    String url;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_detail);
        ButterKnife.bind(this);
        url = getIntent().getStringExtra("URL");
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImage);
    }
}
