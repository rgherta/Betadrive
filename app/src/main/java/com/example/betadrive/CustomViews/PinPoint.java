package com.example.betadrive.CustomViews;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.betadrive.R;


public class PinPoint extends RelativeLayout {

    //public OnDragListener mOnDragListener = null;
    private ImageView mMarkImageView = null;
    private View mShadowView = null;
    RelativeLayout.LayoutParams params = null;

//    interface OnDragListener {
//        public void onDragStart();
//        public void onDragEnd();
//    }

    public PinPoint(Context context) {
        super(context);
        init(context);
    }

    public PinPoint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        removeView(mMarkImageView);
        removeView(mShadowView);
        addView(mMarkImageView, -1 , params);
        addView(mShadowView, -1, params);

    }


    private void init(Context context){
        mMarkImageView = new ImageView(context);
        mMarkImageView.setImageResource(R.drawable.ic_marker_centered_small);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        mShadowView = new View(context);
        mShadowView.setBackgroundResource(R.drawable.map_pin_shadow);

    }

    private void animateUp(){

        if(mMarkImageView != null && mShadowView != null) {
            ObjectAnimator translateY = ObjectAnimator.ofFloat(mMarkImageView,
                    "translationY",
                    -((float) mMarkImageView.getHeight())/10);

            ObjectAnimator alphaShadow = ObjectAnimator.ofFloat(mShadowView,
                    "alpha",
                    1f,
                    0.6f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(translateY, alphaShadow);
            animatorSet.start();
        }

    }

    private void animateDown(){

        if(mMarkImageView != null && mShadowView != null) {
            ObjectAnimator translateYInverse = ObjectAnimator.ofFloat(mMarkImageView,
                    "translationY",
                    ((float) mMarkImageView.getHeight())/25);

            ObjectAnimator alphaShadowInverse = ObjectAnimator.ofFloat(mShadowView,
                    "alpha",
                    0.6f,
                    1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(translateYInverse, alphaShadowInverse);
            animatorSet.start();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch(ev.getAction()) {
            case 0:
                this.animateUp();

                break;
            case 1:
                this.animateDown();
        }
        return super.dispatchTouchEvent(ev);
    }
//
//    public void setmOnDragListener(OnDragListener mOnDragListener) {
//        this.mOnDragListener = mOnDragListener;
//    }
}
