package com.osepp.remote;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

public class JoyView extends View {

    //固定摇杆背景圆形的X,Y坐标以及半径
    private float mBg_X;
    private float mBg_Y;
    private float mBg_R;
    //摇杆的X,Y坐标以及摇杆的半径
    private float mBtn_X;
    private float mBtn_Y;
    private float mRockerBtn_R;

    private PointF mCenterPoint;
    private static float dp2px=1;
    public JoyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        // 获取bitmap
        //mBmpJoyBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.joy_bg);
        //mBmpJoyBtn = BitmapFactory.decodeResource(context.getResources(), R.drawable.joybtn);
        dp2px=context.getResources().getDisplayMetrics().density;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            // 调用该方法时可以获取view实际的宽getWidth()和高getHeight()
            @Override
            public boolean onPreDraw() {
                // TODO Auto-generated method stub
                getViewTreeObserver().removeOnPreDrawListener(this);

                mCenterPoint = new PointF(getWidth() / 2, getHeight() / 2);
                mBg_X = mCenterPoint.x;
                mBg_Y = mCenterPoint.y;

                mBtn_X = mCenterPoint.x;
                mBtn_Y = mCenterPoint.y;

                mBg_R = Math.min(getWidth(),getHeight()) / 3;
                mRockerBtn_R = mBg_R / 2;

                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p=new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);

        p.setStrokeWidth(6*dp2px);
        p.setColor(Color.BLUE);
        canvas.drawCircle(mCenterPoint.x,mCenterPoint.y,mBg_R,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(200,255,0,0));
        canvas.drawCircle(mBtn_X, mBtn_Y,mRockerBtn_R,p);
    }

    private boolean moveing=false;
    private long lastTime_Update=0;
    private String TAG="JoyView";
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getAction();
        if (action == MotionEvent.ACTION_POINTER_DOWN ||action == MotionEvent.ACTION_DOWN){
            moveing=true;
        }
        if (action == MotionEvent.ACTION_MOVE&&moveing) {
            // 当触屏区域不在活动范围内
            if (Math.sqrt(Math.pow((mBg_X - (int) event.getX()), 2) + Math.pow((mBg_Y - (int) event.getY()), 2)) >= mBg_R) {
                //得到摇杆与触屏点所形成的角度
                double tempRad = getRad(mBg_X, mBg_Y, event.getX(), event.getY());
                //保证内部小圆运动的长度限制
                getXY(mBg_X, mBg_Y, mBg_R, tempRad);
            } else {//如果小球中心点小于活动区域则随着用户触屏点移动即可
                mBtn_X = (int) event.getX();
                mBtn_Y = (int) event.getY();
            }
            if(mJoyChangeListener != null) {
                mJoyChangeListener.report((mBtn_X - mCenterPoint.x)/mBg_R, (mBtn_Y - mCenterPoint.y)/mBg_R);
            }
        } else if (action == MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP) {
            moveing=false;
            //当释放按键时摇杆要恢复摇杆的位置为初始位置
            mBtn_X = mCenterPoint.x;
            mBtn_Y = mCenterPoint.y;
            if(mJoyChangeListener != null) {
                mJoyChangeListener.report(0, 0);
            }
            this.invalidate();
        }
        long now=event.getEventTime();
        if(now-lastTime_Update>(1000/50)){
            this.invalidate();
            lastTime_Update=now;
        }
        return true;
    }

    /***
     * 得到两点之间的弧度
     */
    public double getRad(float px1, float py1, float px2, float py2) {
        //得到两点X的距离
        float x = px2 - px1;
        //得到两点Y的距离
        float y = py1 - py2;
        //算出斜边长
        float xie = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        //得到这个角度的余弦值（通过三角函数中的定理 ：邻边/斜边=角度余弦值）
        float cosAngle = x / xie;
        //通过反余弦定理获取到其角度的弧度
        float rad = (float) Math.acos(cosAngle);
        //注意：当触屏的位置Y坐标<摇杆的Y坐标我们要取反值-0~-180
        if (py2 < py1) {
            rad = -rad;
        }
        return rad;
    }

    /**
     * @param R  圆周运动的旋转点
     * @param centerX 旋转点X
     * @param centerY 旋转点Y
     * @param rad 旋转的弧度
     */
    public void getXY(float centerX, float centerY, float R, double rad) {
        //获取圆周运动的X坐标
        mBtn_X = (float) (R * Math.cos(rad)) + centerX;
        //获取圆周运动的Y坐标
        mBtn_Y = (float) (R * Math.sin(rad)) + centerY;
    }

    JoyChangeListener mJoyChangeListener = null;
    public void setJoyChangeListener(JoyChangeListener joyChangeListener) {
        mJoyChangeListener = joyChangeListener;
    }
    public interface JoyChangeListener {
        public void report(float x, float y);
    }
}
