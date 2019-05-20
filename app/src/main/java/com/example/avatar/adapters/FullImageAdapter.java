package com.example.avatar.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.avatar.R;
import com.example.avatar.activities.FullImageDetailActivity;
import com.example.avatar.listeners.ItemOnClick;
import com.example.avatar.models.ItemCropImage;
import com.example.avatar.models.ItemFullImage;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageAdapter extends RecyclerView.Adapter<FullImageAdapter.ViewHolder> {
    private Context context;
    private List<ItemFullImage> arrayList;
    private ItemOnClick listener;


    public FullImageAdapter(Context context, List<ItemFullImage> arrayList, ItemOnClick listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FullImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_full_image,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FullImageAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .load(arrayList.get(position).getUrl())
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.mImage);

        holder.mName.setText(arrayList.get(position).getName());
        holder.mSize.setText(arrayList.get(position).getSize());

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullImageDetailActivity.class);
                intent.putExtra("URL",arrayList.get(position).getUrl());
                context.startActivity(intent);
            }
        });

        holder.mImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.mImage);
                //inflating menu from xml resource
                popup.inflate(R.menu.popup_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.it_delete:
                                //handle menu1 click
                                listener.removeItem(position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (arrayList != null) {
            return arrayList.size();
        }
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_full)
        ImageView mImage;
        @BindView(R.id.tv_name)
        TextView mName;
        @BindView(R.id.tv_size)
        TextView mSize;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            int mWidth = (getWidthScreen(context)-120)/2;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mWidth,mWidth);
            mImage.setLayoutParams(layoutParams);
        }
    }

    public static int getWidthScreen(Context context){
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int mWidthScreen = display.getWidth();
        return mWidthScreen;
    }

}
