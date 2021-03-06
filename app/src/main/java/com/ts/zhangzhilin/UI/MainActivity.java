package com.ts.zhangzhilin.UI;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ts.zhangzhilin.mycrop.R;
import com.ts.zhangzhilin.util.AnimUtils;

import static com.ts.zhangzhilin.Constant.MyCrop.mycrop_shape;

/**
 * Created by zhangzhilin on 6/15/16.
 */
public class MainActivity extends AppCompatActivity {

        private static final String TAG = "MainActivity";
        //图片选择Intent
        private Intent selectImageintent;
        private final int selecetRequestCode=0x1000;
       private final int cropImageRequestCode=0x1001;
       private final int requestPermissionCode=0x1002;

       private ImageView mMainview;
       private ImageView mImagePreview;
       private FloatingActionButton mFab;
       private FloatingActionButton mFabCropRect;
       private FloatingActionButton mFabCropOval;
       private Uri mImageUri;
       private Uri mOutPutUri;
       private boolean isFabOpened=false;
       private boolean isCroppedOval=false;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_result);
            mImagePreview=(ImageView) findViewById(R.id.mycrop_image_preview);
            mMainview =(ImageView)findViewById(R.id.mycrop_main_view);
            mFab=(FloatingActionButton) findViewById(R.id.select_crop_path);
            mFab.setOnClickListener(fabOnclicklistener);
            mFabCropRect=(FloatingActionButton) findViewById(R.id.mycrop_crop_rect);
            mFabCropRect.setOnClickListener(fabOnclicklistener);
            mFabCropOval = (FloatingActionButton) findViewById(R.id.mycrop_crop_oval);
            mFabCropOval.setOnClickListener(fabOnclicklistener);
            //((ImageView) findViewById(R.id.image_view_preview)).setImageURI(getIntent().getData());
            setupAppBar();
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermission();
            }
        }

//        @Override
//        public boolean onCreateOptionsMenu(final Menu menu) {
//           // getMenuInflater().inflate(R.menu.mycrop_add_image, menu);
//
//            return true;
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            if (item.getItemId() == R.id.menu_add) {
//                selectImage();
//            }
//            return super.onOptionsItemSelected(item);
//        }

    /**
     * 选择图片后，获取图片Uri
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case selecetRequestCode:
                closeMenu(mFab);
            if (resultCode == RESULT_OK) {
                if (requestCode == selecetRequestCode) {
                    //图片选取成功 setImageData
                    //OverLayView GestureCropView 状态Reset并显示。默认界面隐藏
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.pick_success), Toast.LENGTH_SHORT).show();

                    mImageUri = data.getData();
                    if (mImageUri != null) {
                        startCropActivity(mImageUri);
                    }else{
                        //图片选取失败
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.pick_failed), Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                //图片选取失败
                Toast.makeText(MainActivity.this, getResources().getString(R.string.pick_failed), Toast.LENGTH_SHORT).show();
            }
                break;
            case cropImageRequestCode:
                if(resultCode==RESULT_OK){
                    mOutPutUri=data.getData();
                    if(mOutPutUri!=null){
                       showImagePreview(mOutPutUri);
                    }

                }else{

                }
                break;
            default:
                Toast.makeText(MainActivity.this, getResources().getString(R.string.unknow_response_code), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==requestPermissionCode){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //Reserve
            }else{
                MainActivity.this.finish();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
        *Select image.
        */
        private void selectImage(){
           if (selectImageintent==null){

            if (Build.VERSION.SDK_INT < 19) {
                selectImageintent=new Intent();
                selectImageintent.setType("image/*");
                selectImageintent.setAction(Intent.ACTION_GET_CONTENT);
            }
            else {
                selectImageintent= new Intent(Intent.ACTION_OPEN_DOCUMENT);
                selectImageintent.addCategory(Intent.CATEGORY_OPENABLE);
                selectImageintent.setType("image/*");
             }
            }

           if (selectImageintent.resolveActivity(getPackageManager())!=null) {
            startActivityForResult(selectImageintent, selecetRequestCode);
           }else{
              // Toast.makeText(MainActivity.this,getResources().getString(R.string.pick_failed),Toast.LENGTH_SHORT).show();
           }
        }

       private void startCropActivity(Uri mImageUri){
           Intent mCropIntent=new Intent(MainActivity.this,MyCropActivity.class);
           mCropIntent.setData(mImageUri);
           mCropIntent.putExtra(mycrop_shape,isCroppedOval);
           startActivityForResult(mCropIntent,cropImageRequestCode);
       }

    private void showImagePreview(Uri mImageUri){
        Bitmap mCropBm= BitmapFactory.decodeFile(mImageUri.getPath());
        mImagePreview.setImageBitmap(mCropBm);
        //mImagePreview.setImageURI(mImageUri);
        mImagePreview.setVisibility(View.VISIBLE);
        mMainview.setVisibility(View.GONE);
    }


    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ucrop_ic_cross);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    //Android 6.0+ 权限管理
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission_group.STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestPermissionCode);
        }
    }

    //FloatActingButton listener.
    private final View.OnClickListener fabOnclicklistener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_crop_path:
                if (isFabOpened) {
                    closeMenu(v);
                } else {
                    openMenu(v);
                }
                    break;
                case R.id.mycrop_crop_rect:
                    isCroppedOval=false;
                    selectImage();
                    break;
                case R.id.mycrop_crop_oval:
                    isCroppedOval=true;
                    selectImage();
                    break;
            }
        }
    };

    //fab open
    private void openMenu(View view){
        //ROTATE
        ObjectAnimator openAnmi=ObjectAnimator.ofFloat(view,"rotation",0,-65,-45);
        openAnmi.setDuration(300);
        openAnmi.start();
        isFabOpened=true;

        //Show menu.
        AnimUtils.anmiIn(mFabCropRect);
        AnimUtils.anmiIn(mFabCropOval);
    }

    //fab close
    private void closeMenu(View view){
        //ROTATE。
        ObjectAnimator closeAnmi=ObjectAnimator.ofFloat(view,"rotation",-45,20,0);
        closeAnmi.setDuration(300);
        closeAnmi.start();
        isFabOpened=false;

        //Hide menu.
        AnimUtils.animOut(mFabCropRect);
        AnimUtils.animOut(mFabCropOval);

    }


}