package com.example.avatar.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.avatar.MyItemDecoration;
import com.example.avatar.R;
import com.example.avatar.adapters.CropImageAdapter;
import com.example.avatar.listeners.ItemOnClick;
import com.example.avatar.utils.SharedPrefsUtil;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ItemOnClick {
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 200;
    private GoogleSignInClient mGoogleSignInClient;
    public static Uri mCropImageUri = null;
    public static Bitmap bitmap, saveBm;
    @BindView(R.id.ln_main)
    LinearLayout mLayout;
    @BindView(R.id.rcv_crop_image)
    RecyclerView mRecyclerView;
    CropImageAdapter adapter;
    List<String> list = new ArrayList<>();
    int count;
    static int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        readList();
        adapter = new CropImageAdapter(this,list,this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.addItemDecoration(new MyItemDecoration(this, R.dimen.item_offset));
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

    private void readList(){
        list.clear();
        count = SharedPrefsUtil.getIntegerPreference(this,"COUNT",0);
        for (int i=0;i<count;i++){
            String path = SharedPrefsUtil.getStringPreference(this,"PATH"+i);
            list.add(path);
        }
    }

    private void updateList(int position,String path){
        SharedPrefsUtil.setStringPreference(this,"PATH"+position,path);
    }
    private void addToList(){
        count = SharedPrefsUtil.getIntegerPreference(this,"COUNT",0);
        SharedPrefsUtil.setStringPreference(this,"PATH"+count,"");
        count++;
        SharedPrefsUtil.setIntegerPreference(this,"COUNT",count);
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
            case R.id.it_add:
                addToList();
                readList();
                adapter = new CropImageAdapter(MainActivity.this,list,this);
                adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);
                break;
            case R.id.it_save:
                saveTotalImage();
                break;
            case R.id.it_logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                logoutGoogle();
                Intent intent = new Intent(MainActivity.this,SigninActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               // .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external
            // storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and
            // see if we get error.
            boolean requirePermissions = false;
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                requestPermissions(
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                cropImage(imageUri);
            }
        } else if (requestCode==1001 && resultCode==RESULT_OK){
            try {
                String path = data.getStringExtra("PATH");
                updateList(position,path);
                readList();
                adapter.notifyDataSetChanged();
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
        startActivityForResult(intent,1001);
    }

    @SuppressLint("NewApi")
    @Override
    public void clickItem(int pos) {
        position = pos;
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    public void removeItem(int pos) {
        position = pos;
        removeItemAt(pos);
        readList();
        adapter = new CropImageAdapter(MainActivity.this,list,this);
        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
    }

    private void removeItemAt(int position){
        int count  = SharedPrefsUtil.getIntegerPreference(this,"COUNT",0);
        for (int i=0;i<count;i++){
            SharedPrefsUtil.setStringPreference(this,"PATH"+i,"");
        }
        list.remove(position);
        SharedPrefsUtil.setIntegerPreference(this,"COUNT",count-1);
        for (int i=0;i<list.size();i++){
            SharedPrefsUtil.setStringPreference(this,"PATH"+i,list.get(i));
        }
    }

    private void saveTotalImage(){
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
    }

    private void readResultFromFireBase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("users");
        DatabaseReference currentUserDB = databaseReference.child(user.getUid());
        DatabaseReference myRef = currentUserDB.child("result");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
//                    long WORKOUT_NUM = (long) dataSnapshot.child("WORKOUT_NUM").getValue();
//                    long MINUTES_NUM = (long) dataSnapshot.child("MINUTES_NUM").getValue();
//                    long CALORIES_NUM = (long) dataSnapshot.child("CALORIES_NUM").getValue();
//                    long CUR_STREAK_NUM = (long) dataSnapshot.child("CUR_STREAK_NUM").getValue();
//                    long BEST_STREAK_NUM = (long) dataSnapshot.child("BEST_STREAK_NUM").getValue();
//                    CalendarFragment.this.WORKOUT_NUM = (int) WORKOUT_NUM;
//                    CalendarFragment.this.MINUTES_NUM = (int) MINUTES_NUM;
//                    CalendarFragment.this.CALORIES_NUM = (int) CALORIES_NUM;
//                    CalendarFragment.this.CUR_STREAK_NUM = (int) CUR_STREAK_NUM;
//                    CalendarFragment.this.BEST_STREAK_NUM = (int) BEST_STREAK_NUM;
//                    mWorkoutNum.setText(WORKOUT_NUM+"");
//                    mMinutesNum.setText(MINUTES_NUM+"");
//                    mCaloriesNum.setText(CALORIES_NUM+"");
//                    mCurStreakNum.setText(CUR_STREAK_NUM+"");
//                    mBestStreakNum.setText(BEST_STREAK_NUM+"");
                    // Toast.makeText(getContext(),"Loaded",Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addResultToFireBase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("users");
        DatabaseReference currentUserDB = databaseReference.child(user.getUid());
        DatabaseReference myRef = currentUserDB.child("result");
        //myRef.child("CUR_STREAK_NUM").setValue(CUR_STREAK_NUM);
    }
}
