package com.hileone.animation.views;

/**
 * The creator is Leone && E-mail: butleone@163.com
 *
 * @author Leone
 * @date 5/10/16
 * @description Edit it! Change it! Beat it! Whatever, just do it!
 */
public class ShapeModel {

    //当前point
    private ShapePoint mCurPoint;
    //目的地point
    private ShapePoint mDestPoint;
    //每一小段长度
    private float mDeltaX;
    private float mDeltaY;

    public ShapeModel() {
        mCurPoint = new ShapePoint();
        mDestPoint = new ShapePoint();
    }

    public ShapePoint getCurPoint() {
        return mCurPoint;
    }

    public void setCurPoint(ShapePoint curPoint) {
        mCurPoint = curPoint;
    }

    public ShapePoint getDestPoint() {
        return mDestPoint;
    }

    public void setDestPoint(ShapePoint destPoint) {
        mDestPoint = destPoint;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float deltaX) {
        mDeltaX = deltaX;
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float deltaY) {
        mDeltaY = deltaY;
    }

    /**
     * 更新每一小段
     */
    public void update() {
        mCurPoint.mX += mDeltaX;
        mCurPoint.mY += mDeltaY;
        if ((mDeltaX > 0 && mCurPoint.mX > mDestPoint.mX)
                || (mDeltaX < 0 && mCurPoint.mX < mDestPoint.mX)) {
            mCurPoint.mX = mDestPoint.mX;
        }

        if ((mDeltaY > 0 && mCurPoint.mY > mDestPoint.mY)
                || (mDeltaY < 0 && mCurPoint.mY < mDestPoint.mY)) {
            mCurPoint.mY = mDestPoint.mY;
        }
    }
}
