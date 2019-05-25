package com.example.avatar.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
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
    @BindView(R.id.btn_save)
    Button mSave;
    @BindView(R.id.btn_print)
    Button mPrint;
    int position;
    private boolean haveEmail, isPrint = false;
    Typeface type;
    int pick_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        type = Typeface.createFromAsset(getAssets(),"fonts/Pacifico-Regular.ttf");
        mName.setTypeface(type);
        mImage1.setOnClickListener(this::onClick);
        mImage2.setOnClickListener(this::onClick);
        mImage3.setOnClickListener(this::onClick);
        mImage4.setOnClickListener(this::onClick);
        mSave.setOnClickListener(this::onClick);
        mPrint.setOnClickListener(this::onClick);

        //dang xuat
        mName.setOnLongClickListener(l->{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this,SigninActivity.class);
            startActivity(intent);
            finish();
            return false;
        });
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
                if (pick_id == 1) CropImage.startPickImageActivity(this);
                else if (pick_id == 2) checkEmail();
                else doPhotoPrint();
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
            requestEmail(isPrint);
        } else {
            saveFullImage();
        }
    }

    private void saveFullImage(){

        mProgressBar.setVisibility(View.VISIBLE);
        Bitmap[] parts = new Bitmap[5];
        float one_px = getResources().getDimension(R.dimen.one_px);
        float canh = 360*one_px;
        try {
            mImage1.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) mImage1.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            parts[1] = Bitmap.createScaledBitmap(bitmap, (int) canh, (int) canh, false);
        } catch (Exception e){

        }
        try {
            mImage2.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) mImage2.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            parts[2] = Bitmap.createScaledBitmap(bitmap, (int) canh, (int) canh, false);
        } catch (Exception e){

        }
        try {
            mImage3.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) mImage3.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            parts[3] = Bitmap.createScaledBitmap(bitmap, (int) canh, (int) canh, false);
        } catch (Exception e){

        }
        try {
            mImage4.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) mImage4.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            parts[4] = Bitmap.createScaledBitmap(bitmap, (int) canh, (int) canh, false);
        } catch (Exception e){

        }
        saveBm = Bitmap.createBitmap((int) (400*one_px+2*canh), (int)(400*one_px+2*canh), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(saveBm);
        canvas.drawColor(Color.parseColor("#ffffff"));
        Paint paint = new Paint();
        if (parts[1]!=null) canvas.drawBitmap(parts[1], 100*one_px, 100*one_px, paint);
        if (parts[2]!=null) canvas.drawBitmap(parts[2], 300*one_px+canh, 100*one_px, paint);
        if (parts[3]!=null) canvas.drawBitmap(parts[3], 100*one_px, 300*one_px+canh, paint);
        if (parts[4]!=null) canvas.drawBitmap(parts[4], 300*one_px+canh, 300*one_px+canh, paint);
        if (!isPrint){
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
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider",des);
            intent.setDataAndType(photoURI,"image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private void requestEmail(boolean isPrint){
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
                if (!isPrint) saveFullImage();
                else doPhotoPrint();
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
            case R.id.img_crop1:
                pick_id = 1;
                cropImage(1);
                break;
            case R.id.img_crop2:
                pick_id = 1;
                cropImage(2);
                break;
            case R.id.img_crop3:
                pick_id = 1;
                cropImage(3);
                break;
            case R.id.img_crop4:
                pick_id = 1;
                cropImage(4);
                break;
            case R.id.btn_save:
                pick_id = 2;
                if (!hasPermissions(this, permissions)) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
                } else {
                    checkEmail();
                }
                break;
            case R.id.btn_print:
                pick_id = 3;
                if (!hasPermissions(this, permissions)) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
                } else {
                    doPhotoPrint();
                }
                break;
        }
    }

    private void doPhotoPrint() {
        isPrint = true;
        checkEmail();
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("droids.jpg - print", saveBm);
        isPrint = false;
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
