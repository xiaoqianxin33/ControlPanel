package com.linkyview.controlpanellibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaoqianxin
 * @date 2018/4/23
 */

public class ControlPanel extends View {


    private Paint mLinePaint;
    private Path mPath;
    private Paint mCirclePanit;
    private Region right;
    private List<Matrix> mMatrices = new ArrayList<>();
    int touchFlag = -1;
    private MenuListener mMenuListener;

    public void setMenuListener(MenuListener menuListener) {
        mMenuListener = menuListener;
    }

    public ControlPanel(Context context) {
        super(context);
        init();
    }

    public ControlPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //定义画笔
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mCirclePanit = new Paint();
        mCirclePanit.setColor(Color.parseColor("#8c68ff"));
        mCirclePanit.setStyle(Paint.Style.STROKE);
        mCirclePanit.setAntiAlias(true);
        //定义路径
        mPath = new Path();
        //定义Region
        right = new Region();
        //初始化矩阵
        for (int i = 0; i < 8; i++) {
            mMatrices.add(new Matrix());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (Matrix matrix : mMatrices) {
            matrix.reset();
        }
        Region globalRegion = new Region(-w, -h, w, h);
        int radius = w / 2;
        int angleWidth = radius / 6;
        mPath.moveTo(radius - 2 * angleWidth, -angleWidth / 2);
        mPath.lineTo(radius - angleWidth, 0);
        mPath.lineTo(radius - 2 * angleWidth, angleWidth / 2);
        mPath.close();
        right.setPath(mPath, globalRegion);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取控件宽高
        int width = getWidth();
        int height = getHeight();
        //画最层的圆
        int radius = width / 2;
        mCirclePanit.setColor(Color.WHITE);
        mCirclePanit.setStyle(Paint.Style.FILL);
        mCirclePanit.setStrokeWidth(1);
        mCirclePanit.setShadowLayer(width / 2, 5, 5, Color.parseColor("#cdcbdb"));
        canvas.drawCircle(width / 2, height / 2, radius, mCirclePanit);
        //画基本空心三角
        drawAngle(canvas, width, height, radius);
        //绘制触摸点实心三角
    }

    private void drawAngle(Canvas canvas, int width, int height, int radius) {
        canvas.save();
        //移动画布至中心点
        canvas.translate(width / 2, height / 2);
        // 获取测量矩阵(逆矩阵)
        if (mMatrices.get(0).isIdentity()) {
            canvas.getMatrix().invert(mMatrices.get(0));
        }
        //绘制八个三角形
        mLinePaint.setColor(Color.YELLOW);
        mLinePaint.setStyle(Paint.Style.STROKE);
        if (touchFlag == 0) {
            mLinePaint.setStyle(Paint.Style.FILL);
        }
        mLinePaint.setStrokeWidth(2);
        Path localPath = mPath;
        canvas.drawPath(localPath, mLinePaint);
        for (int i = 0; i < 7; i++) {
            canvas.rotate(45);
            if (touchFlag == i + 1) {
                mLinePaint.setStyle(Paint.Style.FILL);
            } else {
                mLinePaint.setStyle(Paint.Style.STROKE);
            }
            canvas.drawPath(localPath, mLinePaint);
            if (mMatrices.get(i + 1).isIdentity()) {
                canvas.getMatrix().invert(mMatrices.get(i + 1));
            }
        }
        canvas.restore();
    }

    /**
     * 判断点击事件
     *
     * @param event 事件
     * @return 点击区域
     */
    public int getTouchFlag(MotionEvent event) {
        int size = mMatrices.size();
        for (int i = 0; i < size; i++) {
            float[] pts = getFloat(event);
            mMatrices.get(i).mapPoints(pts);
            int x = (int) pts[0];
            int y = (int) pts[1];
            if (right.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private float[] getFloat(MotionEvent event) {
        float[] pts = new float[2];
        pts[0] = event.getX();
        pts[1] = event.getY();
        return pts;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchFlag = getTouchFlag(event);
                if (mMenuListener != null && touchFlag != -1) {
                    mMenuListener.onAnglePress(touchFlag);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int flag = getTouchFlag(event);
                if (flag != touchFlag) {
                    if (mMenuListener != null && flag != -1) {
                        mMenuListener.onAnglePress(flag);
                    }
                    touchFlag = flag;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                touchFlag = -1;
                break;
            case MotionEvent.ACTION_UP:
                touchFlag = -1;
                if (mMenuListener != null) {
                    mMenuListener.onAnglePress(touchFlag);
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高一致
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSize < heightSize) {
            heightSize = widthSize;
        } else {
            widthSize = heightSize;
        }
        setMeasuredDimension(widthSize,
                heightSize);
    }

    //点击事件监听器
    public interface MenuListener {
        /**
         * 三角点击监听
         *
         * @param positon 位置
         */
        void onAnglePress(int positon);
    }
}
