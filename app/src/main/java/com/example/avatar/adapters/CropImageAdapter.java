package com.example.avatar.adapters;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avatar.R;
import com.example.avatar.listeners.ItemOnClick;
import com.example.avatar.utils.SharedPrefsUtil;

import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CropImageAdapter extends RecyclerView.Adapter<CropImageAdapter.ViewHolder> {
    private Context context;
    private List<String> arrayList;
    private ItemOnClick listener;


    public CropImageAdapter(Context context, List<String> arrayList, ItemOnClick listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CropImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_crop_image,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CropImageAdapter.ViewHolder holder, int position) {
        try {
            if (!arrayList.get(position).equals("")){
                try {
                    Uri uri = Uri.fromFile(new File(arrayList.get(position)));
                    if (uri!=null) holder.mImage.setImageURI(uri);
                } catch (Exception e){

                }
            }
        } catch (Exception e){

        }

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               listener.clickItem(position);
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
        @BindView(R.id.img_crop)
        ImageView mImage;
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