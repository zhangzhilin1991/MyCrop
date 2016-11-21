package com.ts.zhangzhilin.Constant;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;

/**
 * Created by zhangzhilin on 6/14/16.
 */
public class MyCrop {

    //bitmap result config constant.
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    public static final String mycrop_folder= Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Mycrop";

    public static final String mycrop_shape="mycrop_shape";

    //Cropview option constant
    public static final int DEFAULT_MAX_BITMAP_SIZE = 0;
    public static final int DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = 500;
    public static final float DEFAULT_MAX_SCALE_MULTIPLIER = 10.0f;
    public static final float SOURCE_IMAGE_ASPECT_RATIO = 0f;


    //Overlayview option constant
    public static final boolean DEFAULT_SHOW_CROP_FRAME = true;
    public static final boolean DEFAULT_SHOW_CROP_GRID = false;
    public static final boolean DEFAULT_OVAL_DIMMED_LAYER = false;
    public static final boolean DEFAULT_FREESTYLE_CROP_ENABLED =true;
    public static final int DEFAULT_CROP_GRID_ROW_COUNT = 2;
    public static final int DEFAULT_CROP_GRID_COLUMN_COUNT = 2;
    public static final float DEFAULT_ASPECT_RATIO=1/1;

    //crop path.
    public enum CropPatch{
        rectancle,
        oval
    }

}
