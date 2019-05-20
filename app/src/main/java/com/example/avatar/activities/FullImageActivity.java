package com.example.avatar.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avatar.MyItemDecoration;
import com.example.avatar.R;
import com.example.avatar.adapters.CropImageAdapter;
import com.example.avatar.adapters.FullImageAdapter;
import com.example.avatar.listeners.ItemOnClick;
import com.example.avatar.models.ItemCropImage;
import com.example.avatar.models.ItemFullImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageActivity extends AppCompatActivity implements ItemOnClick {
    @BindView(R.id.rcv_full_image)
    RecyclerView mRecyclerView;
    FullImageAdapter adapter;
    List<ItemFullImage> list = new ArrayList<>();
    DatabaseReference myRef;
    DatabaseReference myDBImageRef;
    StorageReference imagesRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        ButterKnife.bind(this);
        adapter = new FullImageAdapter(this,list,this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.addItemDecoration(new MyItemDecoration(this, R.dimen.item_offset));
        accessDBFireBase();
        accessStorageFirebase();
        getListFromFirebase();
    }

    private void getListFromFirebase(){
        List<ItemFullImage> list = new ArrayList<>();
        myDBImageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ItemFullImage fullImage = dataSnapshot.getValue(ItemFullImage.class);
                list.add(fullImage);
                updateList(list);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

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
        //myRef.child(list.get(position).getId()).removeValue();
    }

    private void updateList(List<ItemFullImage> list){
        this.list.clear();
        this.list.addAll(list);
        adapter = new FullImageAdapter(FullImageActivity.this, this.list,this);
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    @Override
    public void clickItem(int pos) {

    }

    @Override
    public void removeItem(int pos) {

    }
}
