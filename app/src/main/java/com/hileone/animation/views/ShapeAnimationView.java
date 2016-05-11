package com.hileone.animation.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hileone.animation.R;
import com.hileone.animation.utils.PullParseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The creator is Leone && E-mail: butleone@163.com
 *
 * @author Leone
 * @date 5/10/16
 * @description Edit it! Change it! Beat it! Whatever, just do it!
 */
public class ShapeAnimationView extends SurfaceView {

    private static final int STATE_INIT = 0x00;
    private static final int STATE_STOP = 0x01;
    private static final int STATE_DRAWING = 0x02;
    private static final int STATE_PAUSE = 0x03;
    private static final int STATE_DESTORY = 0x04;

    private static final int REFRESH_INTERVAL = 50; //ms
    private static final float DEFAULT_SCREEN_WIDTH = 720f;
    private static final float DEFAULT_DISPLAY_PER_MS = 8.0f;
    private static final int MAX_FRAME = 100;
    private static final int ALPHA_FRAME = 25;

    private DrawThread mDrawThread;
    private AtomicInteger mState;
    private float mScreenRatio;
    private float mBitmapScale;
    private Bitmap mBitmap;
    private int mInterval;
    private String mShapeName;
    private int mWidthPixels;
    private float mDPS;
    private int mCurWidth;
    private List<ShapePoint> mPointList;
    private List<ShapeModel> mModelList;
    private float mMinX = -1, mMaxX = -1, mMinY = -1, mMaxY = -1;
    private int mFrame;
    private int mBeginFrame;

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mDrawThread = new DrawThread(holder);
            if (mState.get() == STATE_DRAWING) {
                mDrawThread.start();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mState.set(STATE_DESTORY);
            if (mDrawThread != null) {
                mDrawThread.interrupt();
            }
        }
    };

    /**
     * ShapeAnimationView
     * @param context context
     */
    public ShapeAnimationView(Context context) {
        super(context);
        init(context);
    }

    /**
     * ShapeAnimationView
     * @param context context
     * @param attrs attrs
     */
    public ShapeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * ShapeAnimationView
     * @param context context
     * @param attrs attrs
     * @param defStyleAttr defStyleAttr
     */
    public ShapeAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context ctx) {
        getHolder().addCallback(mCallback);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        mScreenRatio = 1.0f;
        mBitmapScale = 1.0f;

        mInterval = REFRESH_INTERVAL;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mShapeName = "smile_face.xml";
        mDPS = DEFAULT_DISPLAY_PER_MS;
        mState = new AtomicInteger();
        mState.set(STATE_INIT);
        mPointList = new ArrayList<>();
        mModelList = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCurWidth = getMeasuredWidth();
        mScreenRatio = mCurWidth * 1.0f / mWidthPixels;
        mBitmapScale = mScreenRatio;
        mBitmapScale = (getResources().getDisplayMetrics().density / 2.0f) * mScreenRatio;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void setVisibility(int visibility) {
        stopDisplay();
        super.setVisibility(visibility);
    }

    /**
     * 设置展示图片
     * @param bitmap bitmap
     */
    public ShapeAnimationView setBitmap(Bitmap bitmap) {
        if (!isDisplaying()) {
            mBitmap = bitmap;
        }
        return this;
    }

    /**
     * 设置刷新间隔时间
     * @param interval interval
     */
    public ShapeAnimationView setInterval(int interval) {
        if (!isDisplaying()) {
            mInterval = interval;
        }
        return this;
    }

    /**
     * 设置xml文件名称（assets目录下的）
     * @param shapeName shapeName
     */
    public ShapeAnimationView setShapeName(String shapeName) {
        if (!isDisplaying()) {
            mShapeName = shapeName;
        }
        return this;
    }

    /**
     * 跑动画
     */
    public void display() {
        display(mState.get() == STATE_PAUSE ? mFrame : 0);
    }

    /**
     * 从特定帧开始跑动画
     * @param frame frame
     */
    public void display(int frame) {
        if (mInterval <= 10) {
            throw new IllegalArgumentException("Refresh interval must >= 10ms!");
        }
        if (mBitmap == null) {
            throw new IllegalArgumentException("Shape bitmap must not be null!");
        }
        if (TextUtils.isEmpty(mShapeName)) {
            throw new IllegalArgumentException("Shape xml path name must not be empty!");
        }
        InputStream inputStream = null;
        try {
            inputStream = getResources().getAssets().open(mShapeName);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(String.format(
                    "Can't load shap-points message from %s, IOException throwed!", mShapeName));
        }
        mPointList.clear();
        mModelList.clear();
        mPointList = PullParseUtils.parse(inputStream, mCurWidth / DEFAULT_SCREEN_WIDTH);
        if (mPointList == null || mPointList.size() <= 0) {
            throw new IllegalArgumentException("Illegal shap-points file, check it, please!");
        }
        mBeginFrame = frame;
        initialShapeModel();
        startDrawThread();
    }

    private void initialShapeModel() {
        for (ShapePoint p : mPointList) {
            if (mMinX == -1) {
                mMinX = p.mX;
            }
            mMinX = Math.min(mMinX, p.mX);
            mMaxX = Math.max(mMaxX, p.mX);

            if (mMinY == -1) {
                mMinY = p.mY;
            }
            mMinY = Math.min(mMinY, p.mY);
            mMaxY = Math.max(mMaxY, p.mY);

            ShapeModel shape = new ShapeModel();
            shape.setDestPoint(new ShapePoint(p.mX, p.mY));
            mModelList.add(shape);
        }

        //获取中心点
        float mCentX = (mMinX + mMaxX) / 2f;
        float mCentY = (mMinY + mMaxY) / 2f;

        //获取两点间直线距离，弧长公式= n * Pi * r / 180
        for (ShapeModel model : mModelList) {
            model.getCurPoint().mX = mCentX;
            model.getCurPoint().mY = mCentY;
            float dx = model.getDestPoint().mX - mCentX;
            float dy = model.getDestPoint().mY - mCentY;

            final float half = 180;
            float degrees = (float) (Math.atan2(dy, dx) * half / Math.PI);
            model.setDeltaX((float) (mDPS * Math.cos(degrees * Math.PI / half)));
            model.setDeltaY((float) (mDPS * Math.sin(degrees * Math.PI / half)));
        }
    }

    private void startDrawThread() {
        if (mDrawThread != null) {
            if (mState.get() == STATE_INIT) {
                mState.set(STATE_DRAWING);
                mDrawThread.start();
            } else {
                mState.set(STATE_DRAWING);
            }
        }
    }

    /**
     * 是否正在跑动画
     * @return boolean
     */
    public boolean isDisplaying() {
        return mState != null &&
                (mState.get() == STATE_DRAWING || mState.get() == STATE_PAUSE);
    }

    /**
     * 是否暂停跑动画
     * @return boolean
     */
    public boolean isDisplayPausing() {
        return mState != null && mState.get() == STATE_PAUSE;
    }

    /**
     * 暂停跑动画
     */
    public void pauseDislay() {
        if (mState.get() == STATE_INIT || mState.get() == STATE_DRAWING) {
            mState.set(STATE_PAUSE);
        }
    }

    /**
     * 停止跑动画
     */
    public void stopDisplay() {
        mState.set(STATE_STOP);
    }

    /**
     * 当动画处于暂停状态则恢复
     */
    public void resumeDisplay() {
        if (isDisplayPausing()) {
            mState.set(STATE_DRAWING);
        }
    }

    /**
     * 帧动画绘制线程
     */
    private class DrawThread extends Thread {
        private SurfaceHolder mHolder;
        private boolean mAleadyClear;
        private long mLastRefTime = 0L;
        private Paint mPaint = new Paint();
        private Matrix mMatrix = new Matrix();

        /**
         * DrawThread
         * @param holder holder
         */
        public DrawThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            while(true) {
                if (mState.get() == STATE_DRAWING) {
                    drawShape();
                    mAleadyClear = false;
                } else if (mState.get() == STATE_PAUSE) {
                    try {
                        Thread.sleep(REFRESH_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (mState.get() == STATE_STOP){
                    if (!mAleadyClear) {
                        try {
                            Canvas canvas = mHolder.lockCanvas(null);
                            clearCanvas(canvas);
                            mHolder.unlockCanvasAndPost(canvas);
                            mAleadyClear = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (mState.get() == STATE_DESTORY) {
                    break;
                }
            }
        }

        private void drawShape() {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (canvas != null) {
                clearCanvas(canvas);

                final long curTime = System.currentTimeMillis();
                if ((curTime - mLastRefTime) >= REFRESH_INTERVAL) {
                    mLastRefTime = curTime;
                } else {
                    try {
                        long sleepTime = REFRESH_INTERVAL - curTime + mLastRefTime;
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int delta = mFrame - mBeginFrame;
                if (mBitmap != null && delta < MAX_FRAME) {
                    for (ShapeModel shape : mModelList) {
                        shape.update();
                        mMatrix.reset();
                        mPaint.reset();
                        final int max = 255;
                        int alpha = (int) (max * Math.min(ALPHA_FRAME
                                , (MAX_FRAME - delta)) / (float) ALPHA_FRAME);
                        mPaint.setAlpha(alpha);
                        mMatrix.postTranslate(shape.getCurPoint().mX / mBitmapScale
                                , shape.getCurPoint().mY / mBitmapScale);
                        mMatrix.postScale(mBitmapScale, mBitmapScale);
                        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
                    }
                }

                try {
                    mHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mFrame >= MAX_FRAME) {
                    mState.set(STATE_STOP);
                    mFrame = 0;
                } else {
                    mFrame++;
                }
            }
        }

        private void clearCanvas(Canvas canvas) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    }
}

