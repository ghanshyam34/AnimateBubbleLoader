package com.gs.mydottedloader;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import gs.com.mydottedloader.R;

/**
 * Created by Ghanshyam on 12/26/2016.
 */
public class MyCustomDotedLoader extends ProgressBar {

    private int mColor;

    private CustomDrawable mSprite;

    public MyCustomDotedLoader(Context context) {
        this(context, null);
    }

    public MyCustomDotedLoader(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyCustomDotedLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyCustomDotedLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mColor = Color.RED;
        init(context,attrs);
        setIndeterminate(true);
    }

    private void init(Context context,AttributeSet attrs) {

        if (attrs != null) {

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.mydottedLoader);
            mColor = typedArray.getInteger(R.styleable.mydottedLoader_dotcolor, Color.RED);
            dotcount = typedArray.getInteger(R.styleable.mydottedLoader_dotcount,5);
            typedArray.recycle();
        }
        CustomDrawable sprite = new Dotview();
        setIndeterminateDrawable(sprite);
    }

    @Override
    public void setIndeterminateDrawable(Drawable d) {
        setIndeterminateDrawable((CustomDrawable) d);
    }

    public void setIndeterminateDrawable(CustomDrawable d) {
        super.setIndeterminateDrawable(d);
        mSprite = d;
        if (mSprite.getColor() == 0) {
            mSprite.setColor(mColor);
        }
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
//        onSizeChanged(100,100,100,100);
        if (getVisibility() == VISIBLE) {
            mSprite.start();
        }
    }

    @Override
    public CustomDrawable getIndeterminateDrawable() {
        return mSprite;
    }

    public void setColor(int color) {
        this.mColor = color;
        if (mSprite != null) {
            mSprite.setColor(color);
        }
        invalidate();
    }

    @Override
    public void unscheduleDrawable(Drawable who) {
        super.unscheduleDrawable(who);
        if (who instanceof CustomDrawable) {
            ((CustomDrawable) who).stop();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (mSprite != null && getVisibility() == VISIBLE) {
                mSprite.start();
            }
        }
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (screenState == View.SCREEN_STATE_OFF) {
            if (mSprite != null) {
                mSprite.stop();
            }
        }
    }

    int dotcount = 5;
    public void setDotCount(int dotcount){
        this.dotcount = dotcount;
        invalidate();
    }


    private class Dotview extends DrawableContainer {

        @Override
        public CustomDrawable[] onCreateChild() {

            ArrayList<CustomDrawable> list = new ArrayList<>();
            for(int i=0;i<dotcount;i++){
                list.add(new Dot());
            }

            CustomDrawable[] array = new CustomDrawable[list.size()];
            list.toArray(array);
            return array;
        }

        @Override
        public void onChildCreated(CustomDrawable... sprites) {
            super.onChildCreated(sprites);

            for(int i=1;i<sprites.length;i++){
                sprites[i].setAnimationDelay(i*160);
            }
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            bounds = clipSquare(bounds);
            int radius = bounds.width() /12;
            int top = bounds.centerY() - radius;
            int bottom = bounds.centerY() + radius;

            for (int i = 0; i < getChildCount(); i++) {
                int left = bounds.width() * i / dotcount
                        + bounds.left;
                getChildAt(i).setDrawBounds(
                        left, top, left + radius * 2, bottom
                );
            }
        }

        private class Dot extends CircleDrawable {

            Dot() {
                setScale(0f);
            }

            @Override
            public ValueAnimator onCreateAnimation() {
                float fractions[] = new float[]{0f, 0.4f, 0.8f, 1f};
                return new DrawableAnimatorBuilder(this).scale(fractions, 0f, 1f, 0f, 0f).
                        duration(1400).
                        easeInOut(fractions)
                        .build();
            }
        }


        private class CircleDrawable extends ShapeDrawable {

            @Override
            public ValueAnimator onCreateAnimation() {
                return null;
            }

            @Override
            public void drawShape(Canvas canvas, Paint paint) {
                if (getDrawBounds() != null) {
                    int radius = Math.min(getDrawBounds().width(), getDrawBounds().height()) / 2;
                    canvas.drawCircle(getDrawBounds().centerX(),
                            getDrawBounds().centerY(),
                            radius, paint);
                }
            }
        }

        private abstract class ShapeDrawable extends CustomDrawable {

            private Paint mPaint;
            private int mUseColor;
            private int mBaseColor;

            public ShapeDrawable() {
                setColor(getSolidColor());
                mPaint = new Paint();
                mPaint.setAntiAlias(true);
                mPaint.setColor(mUseColor);
            }

            @Override
            public void setColor(int color) {
                mBaseColor = color;
                updateUseColor();
            }

            @Override
            public int getColor() {
                return mBaseColor;
            }

            @SuppressWarnings("unused")
            public int getUseColor() {
                return mUseColor;
            }

            @Override
            public void setAlpha(int alpha) {
                super.setAlpha(alpha);
                updateUseColor();
            }

            private void updateUseColor() {
                int alpha = getAlpha();
                alpha += alpha >> 7;
                final int baseAlpha = mBaseColor >>> 24;
                final int useAlpha = baseAlpha * alpha >> 8;
                mUseColor = (mBaseColor << 8 >>> 8) | (useAlpha << 24);
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
                mPaint.setColorFilter(colorFilter);
            }

            @Override
            protected final void drawSelf(Canvas canvas) {
                mPaint.setColor(mUseColor);
                drawShape(canvas, mPaint);
            }

            public abstract void drawShape(Canvas canvas, Paint paint);
        }
    }


    private abstract class DrawableContainer extends CustomDrawable {

        private CustomDrawable[] sprites;

        private int color;

        public DrawableContainer() {
            sprites = onCreateChild();
            initCallBack();
            onChildCreated(sprites);
        }

        private void initCallBack() {
            if (sprites != null) {
                for (CustomDrawable sprite : sprites) {
                    sprite.setCallback(this);
                }
            }
        }

        public void onChildCreated(CustomDrawable... sprites) {

        }

        public int getChildCount() {
            return sprites == null ? 0 : sprites.length;
        }

        public CustomDrawable getChildAt(int index) {
            return sprites == null ? null : sprites[index];
        }

        @Override
        public void setColor(int color) {
            this.color = color;
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setColor(color);
            }
        }

        @Override
        public int getColor() {
            return color;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            drawChild(canvas);
        }

        public void drawChild(Canvas canvas) {
            if (sprites != null) {
                for (CustomDrawable sprite : sprites) {
                    int count = canvas.save();
                    sprite.draw(canvas);
                    canvas.restoreToCount(count);
                }
            }
        }

        @Override
        protected void drawSelf(Canvas canvas) {
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            for (CustomDrawable sprite : sprites) {
                sprite.setBounds(bounds);
            }
        }

        @Override
        public void start() {
            super.start();
            MyCustomDotedLoader.start(sprites);
        }

        @Override
        public void stop() {
            super.stop();
            MyCustomDotedLoader.stop(sprites);
        }

        @Override
        public boolean isRunning() {
            return MyCustomDotedLoader.isRunning(sprites) || super.isRunning();
        }

        public abstract CustomDrawable[] onCreateChild();

        @Override
        public ValueAnimator onCreateAnimation() {
            return null;
        }
    }


    private abstract class CustomDrawable extends Drawable implements
            ValueAnimator.AnimatorUpdateListener
            , Animatable
            , Drawable.Callback {

        private float scale = 1;
        private float scaleX = 1;
        private float scaleY = 1;
        private float pivotX;
        private float pivotY;
        private int animationDelay;
        private int rotateX;
        private int rotateY;
        private int translateX;
        private int translateY;
        private int rotate;
        private float translateXPercentage;
        private float translateYPercentage;
        private ValueAnimator animator;
        private int alpha = 255;
        private final Rect ZERO_BOUNDS_RECT = new Rect();
        protected Rect drawBounds = ZERO_BOUNDS_RECT;
        private Matrix mMatrix;

        public CustomDrawable() {
            mMatrix = new Matrix();
        }

        public abstract int getColor();

        public abstract void setColor(int color);

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        @Override
        public int getAlpha() {
            return alpha;
        }

        @Override
        public int getOpacity() {
            return PixelFormat.RGBA_8888;
        }

        public float getTranslateXPercentage() {
            return translateXPercentage;
        }

        public void setTranslateXPercentage(float translateXPercentage) {
            this.translateXPercentage = translateXPercentage;
        }

        public float getTranslateYPercentage() {
            return translateYPercentage;
        }

        public void setTranslateYPercentage(float translateYPercentage) {
            this.translateYPercentage = translateYPercentage;
        }

        public int getTranslateX() {
            return translateX;
        }

        public void setTranslateX(int translateX) {
            this.translateX = translateX;
        }

        public int getTranslateY() {
            return translateY;
        }

        public void setTranslateY(int translateY) {
            this.translateY = translateY;
        }

        public int getRotate() {
            return rotate;
        }

        public void setRotate(int rotate) {
            this.rotate = rotate;
        }

        public float getScale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = scale;
            setScaleX(scale);
            setScaleY(scale);
        }

        public float getScaleX() {
            return scaleX;
        }

        public void setScaleX(float scaleX) {
            this.scaleX = scaleX;
        }

        public float getScaleY() {
            return scaleY;
        }

        public void setScaleY(float scaleY) {
            this.scaleY = scaleY;
        }

        public int getRotateX() {
            return rotateX;
        }

        public void setRotateX(int rotateX) {
            this.rotateX = rotateX;
        }

        public int getRotateY() {
            return rotateY;
        }

        public void setRotateY(int rotateY) {
            this.rotateY = rotateY;
        }

        public float getPivotX() {
            return pivotX;
        }

        public void setPivotX(float pivotX) {
            this.pivotX = pivotX;
        }

        public float getPivotY() {
            return pivotY;
        }

        public void setPivotY(float pivotY) {
            this.pivotY = pivotY;
        }

        @SuppressWarnings("unused")
        public int getAnimationDelay() {
            return animationDelay;
        }

        public CustomDrawable setAnimationDelay(int animationDelay) {
            this.animationDelay = animationDelay;
            return this;
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        public abstract ValueAnimator onCreateAnimation();

        @Override
        public void start() {
            if (isStarted(animator)) {
                return;
            }

            animator = obtainAnimation();
            if (animator == null) {
                return;
            }

            MyCustomDotedLoader.start(animator);
            invalidateSelf();
        }

        public ValueAnimator obtainAnimation() {
            if (animator == null) {
                animator = onCreateAnimation();
            }
            if (animator != null) {
                animator.addUpdateListener(this);
                animator.setStartDelay(animationDelay);
            }
            return animator;
        }

        @Override
        public void stop() {
            if (isStarted(animator)) {
                animator.removeAllUpdateListeners();
                animator.end();
                reset();
            }
        }

        protected abstract void drawSelf(Canvas canvas);

        public void reset() {
            scale = 1;
            rotateX = 0;
            rotateY = 0;
            translateX = 0;
            translateY = 0;
            rotate = 0;
            translateXPercentage = 0f;
            translateYPercentage = 0f;
        }

        @Override
        public boolean isRunning() {
            return MyCustomDotedLoader.isRunning(animator);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            setDrawBounds(bounds);
        }

        public void setDrawBounds(Rect drawBounds) {
            setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom);
        }

        public void setDrawBounds(int left, int top, int right, int bottom) {
            this.drawBounds = new Rect(left, top, right, bottom);
            setPivotX(getDrawBounds().centerX());
            setPivotY(getDrawBounds().centerY());
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {

        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {

        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final Callback callback = getCallback();
            if (callback != null) {
                callback.invalidateDrawable(this);
            }
        }

        public Rect getDrawBounds() {
            return drawBounds;
        }

        @Override
        public void draw(Canvas canvas) {
            int tx = getTranslateX();
            tx = tx == 0 ? (int) (getBounds().width() * getTranslateXPercentage()) : tx;
            int ty = getTranslateY();
            ty = ty == 0 ? (int) (getBounds().height() * getTranslateYPercentage()) : ty;
            canvas.translate(tx, ty);
            canvas.scale(getScaleX(), getScaleY(), getPivotX(), getPivotY());
            canvas.rotate(getRotate(), getPivotX(), getPivotY());

            drawSelf(canvas);
        }

        public Rect clipSquare(Rect rect) {
            int w = rect.width();
            int h = rect.height();
            int min = Math.min(w, h);
            int cx = rect.centerX();
            int cy = rect.centerY();
            int r = min / 2;
            return new Rect(
                    cx - r,
                    cy - r,
                    cx + r,
                    cy + r
            );
        }
    }


    private final Property<CustomDrawable, Integer> ROTATE_X = new IntProperty<CustomDrawable>("rotateX") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setRotateX(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getRotateX();
        }
    };

    private final Property<CustomDrawable, Integer> ROTATE = new IntProperty<CustomDrawable>("rotate") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setRotate(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getRotate();
        }
    };

    private final Property<CustomDrawable, Integer> ROTATE_Y = new IntProperty<CustomDrawable>("rotateY") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setRotateY(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getRotateY();
        }
    };

    @SuppressWarnings("unused")
    private final Property<CustomDrawable, Integer> TRANSLATE_X = new IntProperty<CustomDrawable>("translateX") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setTranslateX(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getTranslateX();
        }
    };

    @SuppressWarnings("unused")
    private final Property<CustomDrawable, Integer> TRANSLATE_Y = new IntProperty<CustomDrawable>("translateY") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setTranslateY(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getTranslateY();
        }
    };

    private final Property<CustomDrawable, Float> TRANSLATE_X_PERCENTAGE = new FloatProperty<CustomDrawable>("translateXPercentage") {
        @Override
        public void setValue(CustomDrawable object, float value) {
            object.setTranslateXPercentage(value);
        }

        @Override
        public Float get(CustomDrawable object) {
            return object.getTranslateXPercentage();
        }
    };

    private final Property<CustomDrawable, Float> TRANSLATE_Y_PERCENTAGE = new FloatProperty<CustomDrawable>("translateYPercentage") {
        @Override
        public void setValue(CustomDrawable object, float value) {
            object.setTranslateYPercentage(value);
        }

        @Override
        public Float get(CustomDrawable object) {
            return object.getTranslateYPercentage();
        }
    };

    @SuppressWarnings("unused")
    private final Property<CustomDrawable, Float> SCALE_X = new FloatProperty<CustomDrawable>("scaleX") {
        @Override
        public void setValue(CustomDrawable object, float value) {
            object.setScaleX(value);
        }

        @Override
        public Float get(CustomDrawable object) {
            return object.getScaleX();
        }
    };

    private final Property<CustomDrawable, Float> SCALE_Y = new FloatProperty<CustomDrawable>("scaleY") {
        @Override
        public void setValue(CustomDrawable object, float value) {
            object.setScaleY(value);
        }

        @Override
        public Float get(CustomDrawable object) {
            return object.getScaleY();
        }
    };

    private final Property<CustomDrawable, Float> SCALE = new FloatProperty<CustomDrawable>("scale") {
        @Override
        public void setValue(CustomDrawable object, float value) {
            object.setScale(value);
        }

        @Override
        public Float get(CustomDrawable object) {
            return object.getScale();
        }
    };

    private final Property<CustomDrawable, Integer> ALPHA = new IntProperty<CustomDrawable>("alpha") {
        @Override
        public void setValue(CustomDrawable object, int value) {
            object.setAlpha(value);
        }

        @Override
        public Integer get(CustomDrawable object) {
            return object.getAlpha();
        }
    };


    private static void start(Animator animator) {
        if (animator != null && !animator.isStarted()) {
            animator.start();
        }
    }

    private static void stop(Animator animator) {
        if (animator != null && !animator.isRunning()) {
            animator.end();
        }
    }

    private static void start(CustomDrawable... sprites) {
        for (CustomDrawable sprite : sprites) {
            sprite.start();
        }
    }

    private static void stop(CustomDrawable... sprites) {
        for (CustomDrawable sprite : sprites) {
            sprite.stop();
        }
    }

    private static boolean isRunning(CustomDrawable... sprites) {
        for (CustomDrawable sprite : sprites) {
            if (sprite.isRunning()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRunning(ValueAnimator animator) {
        return animator != null && animator.isRunning();
    }

    private static boolean isStarted(ValueAnimator animator) {
        return animator != null && animator.isStarted();
    }


    private abstract class IntProperty<T> extends Property<T, Integer> {

        public IntProperty(String name) {
            super(Integer.class, name);
        }

        public abstract void setValue(T object, int value);

        @Override
        final public void set(T object, Integer value) {
            setValue(object, value);
        }

    }


    private abstract class FloatProperty<T> extends Property<T, Float> {

        public FloatProperty(String name) {
            super(Float.class, name);
        }

        public abstract void setValue(T object, float value);

        @Override
        final public void set(T object, Float value) {
            setValue(object, value);
        }
    }


    private class DrawableAnimatorBuilder {

        private static final String TAG = "DrawableAnimatorBuilder";
        private CustomDrawable sprite;
        private android.view.animation.Interpolator interpolator;
        private int repeatCount = Animation.INFINITE;
        private long duration = 2000;
        private int startFrame = 0;
        private Map<String, FrameData> fds = new HashMap<>();


        class FrameData<T> {
            public FrameData(float[] fractions, Property property, T[] values) {
                this.fractions = fractions;
                this.property = property;
                this.values = values;
            }

            float[] fractions;
            Property property;
            T[] values;
        }

        class IntFrameData extends FrameData<Integer> {

            public IntFrameData(float[] fractions, Property property, Integer[] values) {
                super(fractions, property, values);
            }
        }

        class FloatFrameData extends FrameData<Float> {

            public FloatFrameData(float[] fractions, Property property, Float[] values) {
                super(fractions, property, values);
            }
        }

        private DrawableAnimatorBuilder(CustomDrawable sprite) {
            this.sprite = sprite;
        }

        private DrawableAnimatorBuilder scale(float fractions[], Float... scale) {
            holder(fractions, SCALE, scale);
            return this;
        }

        private DrawableAnimatorBuilder alpha(float fractions[], Integer... alpha) {
            holder(fractions, ALPHA, alpha);
            return this;
        }

        @SuppressWarnings("unused")
        private DrawableAnimatorBuilder scaleX(float fractions[], Float... scaleX) {
            holder(fractions, SCALE, scaleX);
            return this;
        }

        private DrawableAnimatorBuilder scaleY(float fractions[], Float... scaleY) {
            holder(fractions, SCALE_Y, scaleY);
            return this;
        }

        private DrawableAnimatorBuilder rotateX(float fractions[], Integer... rotateX) {
            holder(fractions, ROTATE_X, rotateX);
            return this;
        }

        private DrawableAnimatorBuilder rotateY(float fractions[], Integer... rotateY) {
            holder(fractions, ROTATE_Y, rotateY);
            return this;
        }

        @SuppressWarnings("unused")
        private DrawableAnimatorBuilder translateX(float fractions[], Integer... translateX) {
            holder(fractions, TRANSLATE_X, translateX);
            return this;
        }


        @SuppressWarnings("unused")
        private DrawableAnimatorBuilder translateY(float fractions[], Integer... translateY) {
            holder(fractions, TRANSLATE_Y, translateY);
            return this;
        }


        private DrawableAnimatorBuilder rotate(float fractions[], Integer... rotate) {
            holder(fractions, ROTATE, rotate);
            return this;
        }

        private DrawableAnimatorBuilder translateXPercentage(float fractions[], Float... translateXPercentage) {
            holder(fractions, TRANSLATE_X_PERCENTAGE, translateXPercentage);
            return this;
        }

        private DrawableAnimatorBuilder translateYPercentage(float[] fractions, Float... translateYPercentage) {
            holder(fractions, TRANSLATE_Y_PERCENTAGE, translateYPercentage);
            return this;
        }

        private void holder(float[] fractions, Property property, Float[] values) {
            ensurePair(fractions.length, values.length);
            fds.put(property.getName(), new FloatFrameData(fractions, property, values));
        }


        private void holder(float[] fractions, Property property, Integer[] values) {
            ensurePair(fractions.length, values.length);
            fds.put(property.getName(), new IntFrameData(fractions, property, values));
        }

        private void ensurePair(int fractionsLength, int valuesLength) {
            if (fractionsLength != valuesLength) {
                throw new IllegalStateException(String.format(
                        Locale.getDefault(),
                        "The fractions.length must equal values.length, " +
                                "fraction.length[%d], values.length[%d]",
                        fractionsLength,
                        valuesLength));
            }
        }


        private DrawableAnimatorBuilder interpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        private DrawableAnimatorBuilder easeInOut(float... fractions) {
            interpolator(easeInOutMy(
                    fractions
            ));
            return this;
        }


        private DrawableAnimatorBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        @SuppressWarnings("unused")
        public DrawableAnimatorBuilder repeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

        private DrawableAnimatorBuilder startFrame(int startFrame) {
            if (startFrame < 0) {
                Log.w(TAG, "startFrame should always be non-negative");
                startFrame = 0;
            }
            this.startFrame = startFrame;
            return this;
        }

        private ObjectAnimator build() {

            PropertyValuesHolder[] holders = new PropertyValuesHolder[fds.size()];
            int i = 0;
            for (Map.Entry<String, FrameData> fd : fds.entrySet()) {
                FrameData data = fd.getValue();
                Keyframe[] keyframes = new Keyframe[data.fractions.length];
                float[] fractions = data.fractions;
                float startF = fractions[startFrame];
                for (int j = startFrame; j < (startFrame + data.values.length); j++) {
                    int key = j - startFrame;
                    int vk = j % data.values.length;
                    float fraction = fractions[vk] - startF;
                    if (fraction < 0) {
                        fraction = fractions[fractions.length - 1] + fraction;
                    }
                    if (data instanceof IntFrameData) {
                        keyframes[key] = Keyframe.ofInt(fraction, (Integer) data.values[vk]);
                    } else if (data instanceof FloatFrameData) {
                        keyframes[key] = Keyframe.ofFloat(fraction, (Float) data.values[vk]);
                    } else {
                        keyframes[key] = Keyframe.ofObject(fraction, data.values[vk]);
                    }
                }
                holders[i] = PropertyValuesHolder.ofKeyframe(data.property, keyframes);
                i++;
            }

            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(sprite,
                    holders);
            animator.setDuration(duration);
            animator.setRepeatCount(repeatCount);
            animator.setInterpolator(interpolator);
            return animator;
        }
    }


    private FrameInterpolator easeInOutMy(float... fractions) {
        FrameInterpolator interpolator = new FrameInterpolator(PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f));
        interpolator.setFractions(fractions);
        return interpolator;
    }

    private FrameInterpolator pathInterpolator(float controlX1, float controlY1,
                                                 float controlX2, float controlY2,
                                                 float... fractions) {
        FrameInterpolator interpolator = new FrameInterpolator(PathInterpolatorCompat.create(controlX1, controlY1, controlX2, controlY2));
        interpolator.setFractions(fractions);
        return interpolator;
    }


    private class FrameInterpolator implements Interpolator {

        private TimeInterpolator interpolator;
        private float[] fractions;

        private FrameInterpolator(TimeInterpolator interpolator, float... fractions) {
            this.interpolator = interpolator;
            this.fractions = fractions;
        }

        private void setFractions(float... fractions) {
            this.fractions = fractions;
        }

        @Override
        public synchronized float getInterpolation(float input) {

            if (fractions.length > 1) {
                for (int i = 0; i < fractions.length - 1; i++) {
                    float start = fractions[i];
                    float end = fractions[i + 1];
                    float duration = end - start;
                    if (input >= start && input <= end) {
                        input = (input - start) / duration;
                        return start + (interpolator.getInterpolation(input)
                                * duration);
                    }
                }
            }
            return interpolator.getInterpolation(input);
        }
    }
}


