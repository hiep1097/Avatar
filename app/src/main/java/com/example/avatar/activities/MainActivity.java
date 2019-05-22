package com.example.avatar.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.avatar.BuildConfig;
import com.example.avatar.R;
import com.example.avatar.utils.DrawableUtil;
import com.example.avatar.utils.SharedPrefsUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 200;
    private static final int REQUEST_CROP_IMAGE = 1001;
    public static Bitmap bitmap, saveBm;
    @BindView(R.id.ln_main)
    RelativeLayout mLayout;
    @BindView(R.id.tv_name)
    TextView mName;
    @BindView(R.id.img_crop1)
    ImageView mImage1;
    @BindView(R.id.img_crop2)
    ImageView mImage2;
    @BindView(R.id.img_crop3)
    ImageView mImage3;
    @BindView(R.id.img_crop4)
    ImageView mImage4;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    int position;
    private boolean haveEmail;
    Typeface type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        type = Typeface.createFromAsset(getAssets(),"fonts/Pacifico-Regular.ttf");
        mName.setTypeface(type);
        mName.setOnClickListener(this::onClick);
        mImage1.setOnClickListener(this::onClick);
        mImage2.setOnClickListener(this::onClick);
        mImage3.setOnClickListener(this::onClick);
        mImage4.setOnClickListener(this::onClick);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initImage();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.it_save:
                checkEmail();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            cropImage(imageUri);
        } else if (requestCode==REQUEST_CROP_IMAGE && resultCode==RESULT_OK){
            try {
                String path = data.getStringExtra("PATH");
                updateImageAt(position,path);
            } catch (Exception e){
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void cropImage(Uri uri){
        Intent intent = new Intent(MainActivity.this,CropImageActivity.class);
        intent.setData(uri);
        startActivityForResult(intent,REQUEST_CROP_IMAGE);
    }

    private void checkEmail(){
        haveEmail = SharedPrefsUtil.getBooleanPreference(this,"EMAIL",false);
        if (!haveEmail){
            requestEmail();
        } else {
            saveFullImage();
        }
    }

    private void saveFullImage(){
        mProgressBar.setVisibility(View.VISIBLE);
        mLayout.setDrawingCacheEnabled(true);
        mLayout.buildDrawingCache(true);
        saveBm = Bitmap.createBitmap(mLayout.getDrawingCache());
        mLayout.setDrawingCacheEnabled(false);
        StringBuilder filename = new StringBuilder(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        filename.append(".jpg");
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File des = new File(storageDir,filename.toString());
        try (FileOutputStream out = new FileOutputStream(des)) {
            MainActivity.saveBm.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(des)));
            Toast.makeText(this,"Đã lưu",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri photoURI = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider",des);
        intent.setDataAndType(photoURI,"image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void requestEmail(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Nhập email")
                .inputRange(3,30)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText("Đồng ý")
                .negativeText("Hủy bỏ")
                .input("Nhập email", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }
                }).show();
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefsUtil.setBooleanPreference(MainActivity.this,"EMAIL",true);
                dialog.dismiss();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("email");
                myRef.push().setValue(dialog.getInputEditText().getText().toString());
                saveFullImage();
            }
        });
        dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_name:
                changeName();
                break;
            case R.id.img_crop1:
                cropImage(1);
                break;
            case R.id.img_crop2:
                cropImage(2);
                break;
            case R.id.img_crop3:
                cropImage(3);
                break;
            case R.id.img_crop4:
                cropImage(4);
                break;
        }
    }

    private void changeName() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Nhập tên")
                .inputRange(3,30)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText("Đồng ý")
                .negativeText("Hủy bỏ")
                .input("Nhập tên", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }
                }).show();
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName.setText(dialog.getInputEditText().getText().toString());
                mName.setTypeface(type);
                dialog.dismiss();
            }
        });
        dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void cropImage(int position){
        this.position = position;
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    private void initImage(){
        try {
            mImage1.setImageURI(Uri.fromFile(new File(SharedPrefsUtil.getStringPreference(this,"PATH1"))));
        } catch (Exception e){
            mImage1.setImageDrawable(DrawableUtil.getInstance().getDrawable(this,"circle_add_img"));
        }
        try {
            mImage2.setImageURI(Uri.fromFile(new File(SharedPrefsUtil.getStringPreference(this,"PATH2"))));
        } catch (Exception e){
            mImage2.setImageDrawable(DrawableUtil.getInstance().getDrawable(this,"circle_add_img"));
        }
        try {
            mImage3.setImageURI(Uri.fromFile(new File(SharedPrefsUtil.getStringPreference(this,"PATH3"))));
        } catch (Exception e){
            mImage3.setImageDrawable(DrawableUtil.getInstance().getDrawable(this,"circle_add_img"));
        }
        try {
            mImage4.setImageURI(Uri.fromFile(new File(SharedPrefsUtil.getStringPreference(this,"PATH4"))));
        } catch (Exception e){
            mImage4.setImageDrawable(DrawableUtil.getInstance().getDrawable(this,"circle_add_img"));
        }
    }

    private void updateImageAt(int position, String path){
        Uri uri = Uri.fromFile(new File(path));
        switch (position){
            case 1:
                SharedPrefsUtil.setStringPreference(MainActivity.this,"PATH1",path);
                mImage1.setImageURI(uri);
                break;
            case 2:
                SharedPrefsUtil.setStringPreference(MainActivity.this,"PATH2",path);
                mImage2.setImageURI(uri);
                break;
            case 3:
                SharedPrefsUtil.setStringPreference(MainActivity.this,"PATH3",path);
                mImage3.setImageURI(uri);
                break;
            case 4:
                SharedPrefsUtil.setStringPreference(MainActivity.this,"PATH4",path);
                mImage4.setImageURI(uri);
                break;
        }
    }

}
