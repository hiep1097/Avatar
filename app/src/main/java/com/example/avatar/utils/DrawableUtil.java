package com.example.avatar.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class DrawableUtil {
    private static DrawableUtil instance;
    private DrawableUtil(){

    }
    public static DrawableUtil getInstance(){
        if (instance==null){
            instance = new DrawableUtil();
        }
        return instance;
    }
    public Drawable getDrawable(Context context, String image){
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(image, "drawable",
                context.getPackageName());
        Drawable drawable = resources.getDrawable(resourceId);
        return drawable;
    }
}
