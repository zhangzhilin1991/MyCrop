package com.ts.zhangzhilin.util;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.ts.zhangzhilin.mycrop.R;

/**
 * Created by zhangzhilin on 11/8/16.
 * Email:zhangzhilin1991@sina.com
 */

public class AnimUtils {

    //view scale in anim.
    public static void anmiIn(View view){
        view.setVisibility(View.VISIBLE);
        Animation animIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.mycrop_anmi_fade_in);
        animIn.setInterpolator(new DecelerateInterpolator());
        animIn.setDuration(300);
        //anim.setInterpolator(INTERPOLATOR);
        view.startAnimation(animIn);
    }

    //View scale out anim.
    public static void animOut(View view){
        final View dstView=view;
        Animation animOut=AnimationUtils.loadAnimation(view.getContext(),R.anim.mycrop_anmi_fade_out);
        animOut.setInterpolator(new AccelerateInterpolator());
        animOut.setDuration(300);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dstView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dstView.startAnimation(animOut);
    }
}
