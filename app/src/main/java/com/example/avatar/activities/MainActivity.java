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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.avatar.MyItemDecoration;
import com.example.avatar.R;
import com.example.avatar.adapters.CropImageAdapter;
import com.example.avatar.listeners.ItemOnClick;
import com.example.avatar.models.ItemCropImage;
import com.example.avatar.models.ItemFullImage;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
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
    FrameLayout mLayout;
    @BindView(R.id.rcv_crop_image)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    CropImageAdapter adapter;
    List<ItemCropImage> list = new ArrayList<>();
    long count;
    static int position;
    DatabaseReference myRef;
    DatabaseReference myDBImageRef;
    StorageReference imagesRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.GONE);
        accessDBFireBase();
        accessStorageFirebase();
        getListFromFirebase();
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

    private void accessDBFireBase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("users");
        DatabaseReference currentUserDB = databaseReference.child(user.getUid());
        myRef = currentUserDB.child("data");
        myDBImageRef = currentUserDB.child("dbimages");
    }

    private void accessStorageFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("user").child((user.getUid()));
        imagesRef = storageRef.child("images");
    }

    private void getListFromFirebase(){
        List<ItemCropImage> list = new ArrayList<>();
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = (String) dataSnapshot.getValue();
                list.add(new ItemCropImage(dataSnapshot.getKey(),value));
                updateList(list);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = (String) dataSnapshot.getValue();
                String key = dataSnapshot.getKey();
                int vt = -1;
                for (int i=0;i<list.size();i++)
                    if (list.get(i).getId().equals(key)){
                        vt = i;
                        break;
                    }
                list.set(vt,new ItemCropImage(key,value));
                updateList(list);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int vt = -1;
                for (int i=0;i<list.size();i++)
                    if (list.get(i).getId().equals(key)){
                        vt = i;
                        break;
                    }
                list.remove(vt);
                updateList(list);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToList(){
        myRef.push().setValue("");
    }

    private void removeItemAt(int position){
        myRef.child(list.get(position).getId()).removeValue();
    }

    private void updateItemAt(int position, String path){
        myRef.child(list.get(position).getId()).setValue(path);
    }

    private void updateList(List<ItemCropImage> list){
        this.list.clear();
        this.list.addAll(list);
        adapter = new CropImageAdapter(MainActivity.this, this.list,this);
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
//        if (list.size() == 0) {
//            mTapToStart.setVisibility(View.VISIBLE);
//        } else {
//            mTapToStart.setVisibility(View.GONE);
//        }
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
                break;
            case R.id.it_save:
                saveFullImage();
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
                .requestIdToken(getString(R.string.default_web_client_id))
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
                updateItemAt(position,path);
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
        removeItemAt(position);
    }

    private void saveFullImage(){
        mLayout.setDrawingCacheEnabled(true);
        mLayout.buildDrawingCache(true);
        saveBm = Bitmap.createBitmap(mLayout.getDrawingCache());
        mLayout.setDrawingCacheEnabled(false);
        StringBuilder filename = new StringBuilder(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        filename.append(".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        saveBm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference sref = imagesRef.child(filename.toString());
        UploadTask uploadTask = sref.putBytes(data);
        mProgressBar.setVisibility(View.VISIBLE);

        uploadTask = sref.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return sref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this,"Lưu thành công",Toast.LENGTH_SHORT).show();
                    String name = filename.toString();
                    float s = data.length;
                    DecimalFormat df = new DecimalFormat("#.##");
                    String size = (s>=1024*1024?df.format((s/(1024*1024)))+"MB":df.format((s/(1024)))+"KB");
                    String url =task.getResult().toString();
                    ItemFullImage fullImage = new ItemFullImage();
                    fullImage.setName(name);
                    fullImage.setSize(size);
                    fullImage.setUrl(url);
                    myDBImageRef.push().setValue(fullImage);
                } else {
                    Toast.makeText(MainActivity.this,"Lưu thất bại",Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.GONE);
                Intent intent = new Intent(MainActivity.this,FullImageActivity.class);
                startActivity(intent);
            }
        });
    }
}
