package com.ts.zhangzhilin.callback;

import android.net.Uri;
import android.support.annotation.NonNull;

public interface BitmapCropCallback {

    void onBitmapCropped(Uri uri);

    void onCropFailure(@NonNull Exception bitmapCropException);

}