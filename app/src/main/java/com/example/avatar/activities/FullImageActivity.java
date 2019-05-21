package com.example.avatar.activities;

import android.os.Bundle;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    List<String> listKey = new ArrayList<>();
    DatabaseReference myDBImageRef;
    StorageReference imagesRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        ButterKnife.bind(this);
        adapter = new FullImageAdapter(this,list,listKey,this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.addItemDecoration(new MyItemDecoration(this, R.dimen.item_offset));
        accessDBFireBase();
        accessStorageFirebase();
        getListFromFirebase();
    }

    private void getListFromFirebase(){
        List<ItemFullImage> list = new ArrayList<>();
        List<String> listKey = new ArrayList<>();
        myDBImageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ItemFullImage fullImage = dataSnapshot.getValue(ItemFullImage.class);
                list.add(fullImage);
                updateList(list);
                listKey.add(dataSnapshot.getKey());
                updateListKey(listKey);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int vt = -1;
                for (int i=0;i<list.size();i++)
                    if (listKey.get(i).equals(key)){
                        vt = i;
                        break;
                    }
                listKey.remove(vt);
                updateListKey(listKey);
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

    private void removeItemAt(int position){
        myDBImageRef.child(listKey.get(position)).removeValue();
    }

    private void removeImageAt(int position){
        imagesRef.child(list.get(position).getName()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FullImageActivity.this,"Đã xóa",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FullImageActivity.this,"Xóa thất bại!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateList(List<ItemFullImage> list){
        this.list.clear();
        this.list.addAll(list);
        adapter = new FullImageAdapter(FullImageActivity.this, this.list, listKey,this);
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updateListKey(List<String> listKey){
        this.listKey.clear();
        this.listKey.addAll(listKey);
    }

    private void accessDBFireBase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("users");
        DatabaseReference currentUserDB = databaseReference.child(user.getUid());
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
        removeItemAt(pos);
        removeImageAt(pos);
    }
}
