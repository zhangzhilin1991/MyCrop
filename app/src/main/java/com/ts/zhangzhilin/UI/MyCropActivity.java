package com.ts.zhangzhilin.UI;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.ts.zhangzhilin.Constant.UCrop;
import com.ts.zhangzhilin.callback.BitmapCropCallback;
import com.ts.zhangzhilin.mycrop.R;
import com.ts.zhangzhilin.view.CropImageView;
import com.ts.zhangzhilin.view.GestureCropImageView;
import com.ts.zhangzhilin.view.OverlayView;
import com.ts.zhangzhilin.view.TransformImageView;
import com.ts.zhangzhilin.view.UCropView;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */

public class MyCropActivity extends AppCompatActivity {

    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;


    public static final int NONE = 0;
    public static final int SCALE = 1;
    public static final int ROTATE = 2;
    public static final int ALL = 3;

    @IntDef({NONE, SCALE, ROTATE, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {

    }

    private static final String TAG = "MyCropActivity";

    private static final int TABS_COUNT = 3;
    private static final int SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000;
    private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;

    private String mToolbarTitle;

    // Enables dynamic coloring
    private int mToolbarColor;
    private int mStatusBarColor;
    private int mActiveWidgetColor;
    private int mToolbarWidgetColor;
    private int mLogoColor;

    private boolean mShowBottomControls;
    private boolean  mShowLoader=false;
    private boolean mCrop = false;

    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private View mBlockingView;
    private ImageView mNoImageView;

    private Uri mOutputUri;

    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
    private int[] mAllowedGestures = new int[]{SCALE, ROTATE, ALL};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ucrop_activity_photobox);


       // final Intent intent = getIntent();

        setupViews();
//        setImageData(intent);
        setInitialState();
//        addBlockingView();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.ucrop_menu_activity, menu);

        // Change crop & loader menu icons color to match the rest of the UI colors

//        MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
//        Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
//        if (menuItemLoaderIcon != null) {
//            try {
//                menuItemLoaderIcon.mutate();
//                menuItemLoaderIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
//                menuItemLoader.setIcon(menuItemLoaderIcon);
//            } catch (IllegalStateException e) {
//                Log.e(TAG, String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
//            }
//            ((Animatable) menuItemLoader.getIcon()).start();
//        }
//
//        MenuItem menuItemCrop = menu.findItem(R.id.menu_crop);
//        Drawable menuItemCropIcon = menuItemCrop.getIcon();
//        if (menuItemCropIcon != null) {
//            menuItemCropIcon.mutate();
//            menuItemCropIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
//            menuItemCrop.setIcon(menuItemCropIcon);
//        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_crop).setVisible(mCrop && !mShowLoader);
        menu.findItem(R.id.menu_loader).setVisible(mCrop && mShowLoader);
        menu.findItem(R.id.menu_add).setVisible(!mCrop);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_crop) {
            cropAndSaveImage();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }else if (item.getItemId()==R.id.menu_add){
            selectImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGestureCropImageView != null) {
            mGestureCropImageView.cancelAllAnimations();
        }
    }

    /**
     * This method setups views properly.
     */
    private void setImageData(Uri inputUri) {

        Log.d(TAG, "setImageData: inputUri="+inputUri.toString());

        mOutputUri = Uri.parse(Environment.getExternalStorageDirectory().toString()+File.pathSeparator+inputUri.getLastPathSegment()+"_"+System.currentTimeMillis());

        Log.d(TAG, "setImageData: outputUri="+mOutputUri.toString());


        if (inputUri != null && mOutputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, mOutputUri);
            } catch (Exception e) {
                //exception ?
                e.printStackTrace();
            }
        }
    }

    /**
     * reset view property to default
     *
     */
    private void resetViewProperty() {
        // Bitmap compression options
        mCompressFormat =  DEFAULT_COMPRESS_FORMAT;
        mCompressQuality = DEFAULT_COMPRESS_QUALITY;

        // Crop image view options
        mGestureCropImageView.setMaxBitmapSize(CropImageView.DEFAULT_MAX_BITMAP_SIZE);
        mGestureCropImageView.setMaxScaleMultiplier(CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER);
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION);

        // Overlay view options
        mOverlayView.setFreestyleCropEnabled(OverlayView.DEFAULT_FREESTYLE_CROP_ENABLED);
        mOverlayView.setDimmedColor(getResources().getColor(R.color.ucrop_color_default_dimmed));
        mOverlayView.setOvalDimmedLayer(OverlayView.DEFAULT_OVAL_DIMMED_LAYER);

        mOverlayView.setShowCropFrame(OverlayView.DEFAULT_SHOW_CROP_FRAME);
        mOverlayView.setCropFrameColor(getResources().getColor(R.color.ucrop_color_default_crop_frame));
        mOverlayView.setCropFrameStrokeWidth(getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width));

        mOverlayView.setShowCropGrid(OverlayView.DEFAULT_SHOW_CROP_GRID);
        mOverlayView.setCropGridRowCount(OverlayView.DEFAULT_CROP_GRID_ROW_COUNT);
        mOverlayView.setCropGridColumnCount(OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT);
        mOverlayView.setCropGridColor(getResources().getColor(R.color.ucrop_color_default_crop_grid));
        mOverlayView.setCropGridStrokeWidth(getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width));

    }


    /**
     * 根据当前状态 更新View 状态。
     */
    private void updateViewState(){
        //更新menu状态
        supportInvalidateOptionsMenu();

        //更新View 显示状态。
        if(mCrop) {
            mUCropView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mNoImageView.setVisibility(View.GONE);
           // BLOCKING VIEW STATE
            if (mShowLoader){
                mBlockingView.setVisibility(View.VISIBLE);
            }else{
                mBlockingView.setVisibility(View.GONE);
           }
        }else{
            mUCropView.setVisibility(View.GONE);
            mNoImageView.setVisibility(View.VISIBLE);
            mBlockingView.setVisibility(View.GONE);
        }
    }

    private void setupViews() {

        setupAppBar();
        initiateRootViews();
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {
        //setStatusBarColor(mStatusBarColor);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initiateRootViews() {
        mUCropView = (UCropView) findViewById(R.id.ucrop);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();
        mBlockingView=(View)findViewById(R.id.block_view);
        mNoImageView=(ImageView) findViewById(R.id.no_image_view);

    mGestureCropImageView.setTransformImageListener(mImageListener);

       // ((ImageView) findViewById(R.id.image_view_logo)).setColorFilter(mLogoColor, PorterDuff.Mode.SRC_ATOP);
    }

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {

        }

        @Override
        public void onScale(float currentScale) {

        }

        @Override
        public void onLoadComplete() {
            mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
            mBlockingView.setClickable(false);
            mShowLoader = false;
            supportInvalidateOptionsMenu();
          ///  updateViewState();
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
            e.printStackTrace();
            mCrop=false;
            updateViewState();
        }

    };

    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getWindow() != null) {
                getWindow().setStatusBarColor(color);
            }
        }
    }

    private void resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void rotateByAngle(int angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void setInitialState() {

            setAllowedGestures(2);

    }

    private void setAllowedGestures(int tab) {
        mGestureCropImageView.setScaleEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == SCALE);
        mGestureCropImageView.setRotateEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == ROTATE);
    }


    protected void cropAndSaveImage() {
        //mBlockingView.setClickable(true);
        mShowLoader = true;
        //supportInvalidateOptionsMenu();
        updateViewState();

        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, mOutputUri,
                new BitmapCropCallback() {
                    @Override
                    public void onBitmapCropped(Uri uri) {
                        //setResultUri(mOutputUri, mGestureCropImageView.getTargetAspectRatio());
                        //finish();
                        Toast.makeText(MyCropActivity.this,"裁剪成功！",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onBitmapCropped: outPutUri="+uri);
                        startResultActivity(uri);
                    }

                    @Override
                    public void onCropFailure(@NonNull Exception bitmapCropException) {
                        //setResultException(bitmapCropException);
                        //finish();
                        mShowLoader=false;
                        Toast.makeText(MyCropActivity.this,"裁剪失败！",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCropFailure: exception="+bitmapCropException);
                    }
                });
    }

    private void startResultActivity(Uri imageUri){
        Intent intent=new Intent(MyCropActivity.this,CropResultActivity.class);
        intent.setData(imageUri);
        startActivity(new Intent());
    }

    protected void setResultUri(Uri uri, float resultAspectRatio) {
        setResult(RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio));
    }

    protected void setResultException(Throwable throwable) {
        setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
    }


    //-------------------------------------------------------------------------------------

    /**
     * 选择图片后，获取图片Uri
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK ){
            if(requestCode==selecetRequestCode){
                //图片选取成功 setImageData
                //OverLayView GestureCropView 状态Reset并显示。默认界面隐藏
                Toast.makeText(MyCropActivity.this,getResources().getString(R.string.pick_success),Toast.LENGTH_SHORT).show();

                Uri imageUri = data.getData();
                mCrop=true;
                resetViewProperty();
                updateViewState();
                setImageData(imageUri);

                //
            }

        }else{
            //图片选取失败
            mCrop=false;
            mShowLoader=false;
            Toast.makeText(MyCropActivity.this,getResources().getString(R.string.pick_failed),Toast.LENGTH_SHORT).show();
        }
    }

    //图片选择Intent
    private  Intent selectImageintent;
    private int selecetRequestCode=0x1000;


    /**
     * 选择要裁剪的图片
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
                selectImageintent.setType("image/jpeg");
            }
        }

        if (selectImageintent.resolveActivity(getPackageManager())!=null) {
            startActivityForResult(selectImageintent, selecetRequestCode);
        }else{
            //

        }
    }

}
