package com.gs.mydottedloader;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.gs.mydottedloaderlib.R;

import java.util.ArrayList;


/**
 * Created by Ghanshyam on 12/31/2016.
 */
public class HorizontalDottedProgress extends View{

    private int  mDotPosition;

    private int mDotAmount = 5;

    private int color = Color.GREEN;

    ArrayList<bounce> list = new ArrayList<>();

    public HorizontalDottedProgress(Context context) {
        super(context);
//        init(context,null);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs){

        if (attrs != null) {

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalDottedProgress);
            color = typedArray.getInteger(R.styleable.HorizontalDottedProgress_dotcolor, Color.RED);
            mDotAmount = typedArray.getInteger(R.styleable.HorizontalDottedProgress_dotcount,5);
            typedArray.recycle();
        }

        for(int i = 0; i < mDotAmount; i++ ){
            bounce b = new bounce();
            b.x = 10+(i*50);
            b.y = 30;
            b.radius = 10;
            b.scaleX = 0;
            b.scaleY = 0;
            b.isBounced = false;
            list.add(b);

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(getSolidColor());
        Paint paint = new Paint();

        paint.setColor(color);

        for(int i = 0; i < list.size(); i++ ){
            paint.setAlpha(list.get(i).alpha);
            bounce bou = list.get(i);
            canvas.drawCircle(bou.x,bou.y, bou.radius, paint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startAnimation();
    }

    public class bounce{

        int alpha = 250;
        int x,y;
        int scaleX = 0;
        int scaleY = 0;
        boolean isBounced = false;
        int radius;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;

        //calculate the view width
        int calculatedWidth = (50*list.size());

        width = calculatedWidth;
        height = (30*2);
        setMeasuredDimension(width, height);
    }

    private void startAnimation() {
        BounceAnimation bounceAnimation = new BounceAnimation();
        bounceAnimation.setDuration(100);
        bounceAnimation.setRepeatCount(Animation.INFINITE);
        bounceAnimation.setInterpolator(new LinearInterpolator());
        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                for(int i=0;i<list.size();i++){
                    if(i==mDotPosition){

                        bounce bou = list.get(i);
                        bou.radius = 24;
                        bou.alpha = 250;
                        list.set(i,bou);

                    }else{

                        bounce bou = list.get(i);
                        if(bou.radius > 4) {

                            bou.radius = bou.radius - 4;
                            bou.alpha = bou.alpha - 20;
                            if(bou.alpha < 0)
                                bou.alpha = 250;

                        }else{

                            bou.radius = 0;
                        }

                        list.set(i,bou);
                    }
                }

                mDotPosition++;

                if (mDotPosition == list.size()) {
                    mDotPosition = 0;
                }
                Log.d("com.gs.HorizontalDotted","----On Animation Repeat----");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        startAnimation(bounceAnimation);
    }


    private class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            invalidate();
        }
    }
}