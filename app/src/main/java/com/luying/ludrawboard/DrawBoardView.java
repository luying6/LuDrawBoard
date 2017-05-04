package com.luying.ludrawboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建人：luying
 * 创建时间：2017/5/4
 * 类说明：
 */

public class DrawBoardView extends View{
    private Paint mPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private float mDrawSize = 20;
    private float mEraserSize = 40;

    private float mLastX;
    private float mLastY;

    private List<DrawingInfo> mDrawingList;
    private List<DrawingInfo> mRemovedList;

    private Xfermode mClearMode;
    private boolean mCanEraser;
    private Callback callback;

    private static final int MAX_CACHE_STEP = 20;

    public enum Mode{
        DRAW,
        ERASER
    }
    private Mode mMode = Mode.DRAW;

    //设置保存路径回掉
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public DrawBoardView(Context context) {
        this(context, null);
    }

    public DrawBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setDrawingCacheEnabled(true);
        initView();
    }

    private void initView() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFilterBitmap(true); //对bitmap进行滤波处理
        mPaint.setStrokeJoin(Paint.Join.ROUND);//线条圆滑
        mPaint.setStrokeCap(Paint.Cap.ROUND);//线段连接处弄成圆形
        mPaint.setStrokeWidth(mDrawSize);//写死，后期可扩展为set方法暴露出去给用户自己调用
        mPaint.setColor(0XFF000000);
        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);//绘制的东西不提交到画布上

    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null){
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;//设置ACTION_MASK处理多点触碰
        final float x = event.getX();
        final float y = event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                if (mPath == null){
                    mPath = new Path();
                }
                mPath.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                //这里终点设为两点的中心点的目的在于使绘制的曲线更平滑，如果终点直接设置为x,y，效果和lineto是一样的,实际是折线效果
                mPath.quadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
                if (mBitmap == null){
                    initBitmap();
                }
                if (mMode == Mode.ERASER && ! mCanEraser){
                    break;
                }
                mCanvas.drawPath(mPath, mPaint);
                invalidate();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mMode == Mode.DRAW || mCanEraser){
                    saveDrawingPath();
                }
                mPath.reset();
                break;
        }
        return true;
    }


    private void initBitmap() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }


    private void saveDrawingPath() {
        if (mDrawingList == null){
            mDrawingList = new ArrayList<>(MAX_CACHE_STEP);
        }else if (mDrawingList.size() == MAX_CACHE_STEP){
            mDrawingList.remove(0);
        }
        Path cachePath = new Path(mPath);
        Paint cachePaint = new Paint(mPaint);
        PathDrawingInfo info = new PathDrawingInfo();
        info.path = cachePath;
        info.paint = cachePaint;
        mDrawingList.add(info);
        if (callback != null){
            callback.onUndoRedoStatusChanged();
        }
    }


    //设置模式，画笔／橡皮(默认画笔)
    public void setMode(Mode mode){
        if (mode != null){
            mMode = mode;
            if (mMode == Mode.DRAW){
                mPaint.setXfermode(null);//置空图像混合取消橡皮擦
                mPaint.setStrokeWidth(mDrawSize);
            }else {
                mPaint.setXfermode(mClearMode);
                mPaint.setStrokeWidth(mEraserSize);
            }
        }
    }

    //撤销
    public void undo() {
        int size = mDrawingList == null ? 0 : mDrawingList.size();
        if (size > 0) {
            DrawingInfo info = mDrawingList.remove(size - 1);
            if (mRemovedList == null) {
                mRemovedList = new ArrayList<>(MAX_CACHE_STEP);
            }

            if (size == 1) {
                mCanEraser = false;
            }
            mRemovedList.add(info);
            reDraw();
            if (callback != null){
                callback.onUndoRedoStatusChanged();
            }
        }

    }
    //撤销上一次撤销
    public void redo(){
        int size = mRemovedList == null ? 0 : mRemovedList.size();
        if (size > 0){
            DrawingInfo info = mRemovedList.remove(size - 1);
            mDrawingList.add(info);
            mCanEraser = true;
            reDraw();
            if (callback != null){
                callback.onUndoRedoStatusChanged();
            }
        }
    }

    public void reDraw() {
        if (mDrawingList != null){
            mBitmap.eraseColor(Color.TRANSPARENT);
            for (DrawingInfo drawingInfo : mDrawingList){
                drawingInfo.draw(mCanvas);
            }
            invalidate();
        }
    }

    //清除所有
    public void clear(){
        if (mBitmap != null){
            if (mDrawingList != null){
                mDrawingList.clear();
            }
            if (mRemovedList != null){
                mRemovedList.clear();
            }

            mCanEraser = false;
            mBitmap.eraseColor(Color.TRANSPARENT);
            invalidate();;
            if (callback != null){
                callback.onUndoRedoStatusChanged();
            }
        }
    }

    public boolean canRedo(){
        return mRemovedList != null && mRemovedList.size() > 0;
    }

    public boolean canUndo(){
        return mDrawingList != null && mDrawingList.size() > 0;
    }

    //canvas撸出个bitmap用于保存路径
    public Bitmap buildBitmap(){
        Bitmap bm = getDrawingCache();
        Bitmap result = Bitmap.createBitmap(bm);
        destroyDrawingCache();
        return result;
    }

    private abstract static class DrawingInfo {
        Paint paint;
        abstract void draw(Canvas canvas);
    }

    private static class PathDrawingInfo extends DrawingInfo{

        Path path;

        @Override
        void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }
    }


    public interface Callback {
        //每次手指抬起时调用此方法，监听保存
        void onUndoRedoStatusChanged();
    }
}
