package com.ts.zhangzhilin.task;

import android.content.Context;
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

    private final BitmapCropCallback mCropCallback;

    public BitmapCropTask(@NonNull Context context, @Nullable Bitmap viewBitmap,
                          @NonNull RectF cropRect, @NonNull RectF currentImageRect,
                          float currentScale, float currentAngle,
                          int maxResultImageSizeX, int maxResultImageSizeY,
                          @NonNull Bitmap.CompressFormat compressFormat, int compressQuality,
                          @NonNull Uri outputUri, @Nullable BitmapCropCallback cropCallback) {

        mContext = context;

        mViewBitmap = viewBitmap;
        mCropRect = cropRect;
        mCurrentImageRect = currentImageRect;

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

//        OutputStream outputStream = null;
//        try {
//            ContentValues values=new ContentValues();
//            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
//            Uri uri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
//            outputStream = mContext.getContentResolver().openOutputStream(uri);
//            mViewBitmap.compress(mCompressFormat, mCompressQuality, outputStream);
//            outputStream.flush();
//            outputStream.close();
//        } catch (Exception e) {
//            return e;
//        } finally {
//            mViewBitmap.recycle();
//            mViewBitmap = null;
//            BitmapLoadUtils.close(outputStream);
//        }
        //存入数据库
        try {
            ConvertToPng();
            mCropedImageUri= MediaStore.Images.Media.insertImage(mContext.getContentResolver(), mViewBitmap,mOutputUri.getLastPathSegment(), null);
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
        ////mViewBitmap= Bitmap.createBitmap(mViewBitmap, left, top, width, height).copy(Bitmap.Config.ARGB_8888, true);
        //draw oval
        Bitmap mOutput=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mOutput);
       //// int color = 0xff424242;
        Paint paint = new Paint();
        ////RECT
        Rect rect = new Rect(0,0,width,height);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setFilterBitmap( true);
        paint.setDither( true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
//      ////;
//
        canvas.drawOval(rectF,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mViewBitmap,rect,rect,paint);
        mViewBitmap=mOutput;

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

    /**
     *
     */
    private void SaveImage(){

    }



}
