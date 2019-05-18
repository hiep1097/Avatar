package com.example.avatar.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.avatar.R;
import com.example.avatar.utils.SharedPrefsUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CropImageActivity extends AppCompatActivity implements View.OnClickListener, CropImageView.OnCropImageCompleteListener,
        CropImageView.OnSetImageUriCompleteListener{
    @BindView(R.id.img_crop)
    CropImageView mCropImage;
    @BindView(R.id.btn_save)
    Button mSave;
    Uri uri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        ButterKnife.bind(this);
        initImage();
        mSave.setOnClickListener(this);
    }
    
    private void initImage(){
        uri = getIntent().getData();
        mCropImage.setImageUriAsync(uri);
        mCropImage.setCropShape(CropImageView.CropShape.OVAL);
        mCropImage.setAspectRatio(1, 1);
        mCropImage.setOnSetImageUriCompleteListener(this);
        mCropImage.setOnCropImageCompleteListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_save:
                mCropImage.getCroppedImageAsync();
                break;
        }
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        handleCropResult(result);
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            Toast.makeText(this, "Image load successful", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("AIC", "Failed to load image by URI", error);
            Toast.makeText(this, "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void handleCropResult(CropImageView.CropResult result) {
        Intent intent = new Intent();
        if (result.getError() == null) {
            Toast.makeText(this, "Image crop success", Toast.LENGTH_LONG).show();
            intent.putExtra("SAMPLE_SIZE", result.getSampleSize());
            if (result.getUri() != null) {
                intent.putExtra("URI", result.getUri());
            } else {
                MainActivity.bitmap = CropImage.toOvalBitmap(result.getBitmap());

            }
            setResult(RESULT_OK,intent);
            finish();
        } else {
            Toast.makeText(this, "Image crop failed: " + result.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
