package com.android.settings.morningstar;

import android.graphics.drawable.AnimationDrawable;

public class ExposedAnimationDrawable extends AnimationDrawable {

    private OnAnimationFinishedListener mOnAnimationFinishedListener;

    public interface OnAnimationFinishedListener {
        void onAnimationFinished();
    }

    public void setOnAnimationFinishedListener(OnAnimationFinishedListener l) {
        mOnAnimationFinishedListener = l;
    }
}