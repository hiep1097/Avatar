package com.example.avatar.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.avatar.BuildConfig;
import com.example.avatar.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageDetailActivity extends AppCompatActivity {
    @BindView(R.id.img_full)
    ImageView mImage;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    String url;
    StorageReference imagesRef;
    String name;
    String KEY;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_detail);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.GONE);
        accessStorageFirebase();
        url = getIntent().getStringExtra("URL");
        name = getIntent().getStringExtra("NAME");
        KEY = getIntent().getStringExtra("KEY");
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_detail_full,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.it_download:
                downloadFullImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadFullImage() {
        mProgressBar.setVisibility(View.VISIBLE);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File des = new File(storageDir,name);
        imagesRef.child(name).getFile(des).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(des)));
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(FullImageDetailActivity.this,"Tải xuống thành công!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri photoURI = FileProvider.getUriForFile(FullImageDetailActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider",des);
                intent.setDataAndType(photoURI,"image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(FullImageDetailActivity.this,"Tải xuống thất bại!"+exception,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void accessStorageFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("user").child((user.getUid()));
        imagesRef = storageRef.child("images");
    }
}
