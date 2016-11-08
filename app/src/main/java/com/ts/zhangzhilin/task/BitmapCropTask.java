package com.ts.zhangzhilin.task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ts.zhangzhilin.callback.BitmapCropCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ts.zhangzhilin.Constant.MyCrop.mycrop_folder;

/**
 * Crops part of image that fills the crop bounds.
 * <p/>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
public class BitmapCropTask extends AsyncTask<Void, Void, Exception> {

    private final Context mContext;

    private Bitmap mViewBitmap;

    private final RectF mCropRect;
    private final RectF mCurrentImageRect;
    private final Matrix mTempMatrix = new Matrix();

    private float mCurrentScale, mCurrentAngle;
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;

    private final Uri mOutputUri;
    private String mCropedImageUri;
    private boolean mCropOvalIamge=false;

    private final BitmapCropCallback mCropCallback;

    public BitmapCropTask(@NonNull Context context, @Nullable Bitmap viewBitmap,
                          @NonNull RectF cropRect, @NonNull RectF currentImageRect,
                          boolean isCropOval,float currentScale, float currentAngle,
                          int maxResultImageSizeX, int maxResultImageSizeY,
                          @NonNull Bitmap.CompressFormat compressFormat, int compressQuality,
                          @NonNull Uri outputUri, @Nullable BitmapCropCallback cropCallback) {

        mContext = context;

        mViewBitmap = viewBitmap;
        mCropRect = cropRect;
        mCurrentImageRect = currentImageRect;
        mCropOvalIamge=isCropOval;

        mCurrentScale = currentScale;
        mCurrentAngle = currentAngle;
        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;

        mCompressFormat = compressFormat;
        mCompressQuality = compressQuality;

        mOutputUri = outputUri;

        mCropCallback = cropCallback;
    }

    @Override
    @Nullable
    protected Exception doInBackground(Void... params) {
        if (mViewBitmap == null || mViewBitmap.isRecycled()) {
            return new NullPointerException("ViewBitmap is null or already recycled");
        }
        if (mCurrentImageRect.isEmpty()) {
            return new NullPointerException("CurrentImageRect is empty");
        }

        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            resize();
        }

        if (mCurrentAngle != 0) {
            rotate();
        }

        crop();

        //存入数据库
        try {
            saveMyBitmap();

        }catch (Exception e){
            return e;
        }
        return null;
    }

    private void resize() {
        float cropWidth = mCropRect.width() / mCurrentScale;
        float cropHeight = mCropRect.height() / mCurrentScale;

        if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

            float scaleX = mMaxResultImageSizeX / cropWidth;
            float scaleY = mMaxResultImageSizeY / cropHeight;
            float resizeScale = Math.min(scaleX, scaleY);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(mViewBitmap,
                    Math.round(mViewBitmap.getWidth() * resizeScale),
                    Math.round(mViewBitmap.getHeight() * resizeScale), false);
            if (mViewBitmap != resizedBitmap) {
                mViewBitmap.recycle();
            }
            mViewBitmap = resizedBitmap;

            mCurrentScale /= resizeScale;
        }
    }

    private void rotate() {
        mTempMatrix.reset();
        mTempMatrix.setRotate(mCurrentAngle, mViewBitmap.getWidth() / 2, mViewBitmap.getHeight() / 2);

        Bitmap rotatedBitmap = Bitmap.createBitmap(mViewBitmap, 0, 0, mViewBitmap.getWidth(), mViewBitmap.getHeight(),
                mTempMatrix, true);
        if (mViewBitmap != rotatedBitmap) {
            mViewBitmap.recycle();
        }
        mViewBitmap = rotatedBitmap;
    }

    private void crop() {
        int top = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
        int left = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
        int width = Math.round(mCropRect.width() / mCurrentScale);
        int height = Math.round(mCropRect.height() / mCurrentScale);

        ////
        ////mViewBitmap = Bitmap.createBitmap(mViewBitmap, left, top, width, height);
        mViewBitmap= Bitmap.createBitmap(mViewBitmap, left, top, width, height).copy(Bitmap.Config.ARGB_8888, true);
        //draw oval
        if(mCropOvalIamge) {
            Bitmap mOutput = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mOutput);
            Paint paint = new Paint();
            ////RECT
            Rect rect = new Rect(0, 0, width, height);
            RectF rectF = new RectF(rect);
            ////config paint
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            ////draw oval
            canvas.drawOval(rectF, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(mViewBitmap, rect, rect, paint);
            mViewBitmap = mOutput;
        }

    }

    @Override
    protected void onPostExecute(@Nullable Exception result) {
        if (mCropCallback != null) {
            if (result == null &&  mCropedImageUri!=null) {
                mCropCallback.onBitmapCropped(Uri.parse(mCropedImageUri));
            } else {
                mCropCallback.onCropFailure(result);
            }
        }
    }

    /**
     * 将图片转为PNG格式。
     */
    private void ConvertToPng(){
        if(mViewBitmap==null){
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mViewBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        //把压缩后的数据baos存放到ByteArrayInputStream中
        //BitmapFactory.d
        mViewBitmap= BitmapFactory.decodeStream( new ByteArrayInputStream(baos.toByteArray()), null, null);//把ByteArrayInputStream数据生成图片\

    }

    public void saveMyBitmap() throws IOException {
        mCropedImageUri=null;
        File dir=new File(mycrop_folder);
        if(!dir.exists()) dir.mkdirs();
        File f = new File(mycrop_folder,System.currentTimeMillis() + ".png");
        f.createNewFile();
        FileOutputStream fOut = new FileOutputStream(f);
        ////Save image to sdcard.
        mViewBitmap.compress(mCompressFormat,mCompressQuality, fOut);
        fOut.flush();
        fOut.close();
        ////put image into system media provider.
        mCropedImageUri=f.getPath();
        if(mCropedImageUri != null){
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    mCropedImageUri, f.getName(), null);
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
        }
    }

}
