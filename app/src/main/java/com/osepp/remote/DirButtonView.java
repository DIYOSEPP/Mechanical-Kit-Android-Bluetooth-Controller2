package com.osepp.remote;

import android.graphics.Path;
import android.support.v7.widget.AppCompatTextView;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


public class DirButtonView extends AppCompatTextView {
    protected boolean mPressed=false;
    protected static float dp2px=1;
    public DirButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dp2px=context.getResources().getDisplayMetrics().density;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Paint p=new Paint();
        p.setAntiAlias(true);
        if(mPressed){
            p.setColor(Color.YELLOW);
        }else {
            p.setColor(Color.GREEN);
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeMiter(2.3f);
        p.setStrokeWidth(6*dp2px);
        p.setShadowLayer(2*dp2px, dp2px, 0, Color.RED);
        Path path=new Path();
        path.moveTo(0,0);
        path.lineTo(getWidth()-getHeight()/2,0);
        path.lineTo(getWidth(),getHeight()/2);
        path.lineTo(getWidth()-getHeight()/2,getHeight());
        path.lineTo(0,getHeight());
        path.close();
        canvas.drawPath(path,p);

        String text=this.getText().toString();
        float r=this.getRotation();
        canvas.rotate(-r,getWidth()/2,getHeight()/2);
        p.setColor(this.getTextColors().getDefaultColor());
        p.setTextSize(this.getTextSize());
        p.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (getHeight()/2- top/2 - bottom/2);
        p.setShadowLayer(0, 0, 0, Color.RED);
        p.setStyle(Paint.Style.FILL);
        canvas.drawText(text,getWidth()/2,baseLineY,p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getAction();
        if(action == MotionEvent.ACTION_POINTER_DOWN ||action == MotionEvent.ACTION_DOWN){
            mPressed=true;
            if(mChangeListener != null) {
                mChangeListener.report(mPressed);
            }
            this.postInvalidate();
        }else if(action == MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP){
            mPressed=false;
            if(mChangeListener != null) {
                mChangeListener.report(mPressed);
            }
            this.invalidate();
        }
        return true;
    }
    JoyButtonChangeListener mChangeListener = null;
    public void setButtonChangeListener(JoyButtonChangeListener joyButtonChangeListener) {
        mChangeListener = joyButtonChangeListener;
    }
    public interface JoyButtonChangeListener {
        public void report(boolean pressed);
    }
}
