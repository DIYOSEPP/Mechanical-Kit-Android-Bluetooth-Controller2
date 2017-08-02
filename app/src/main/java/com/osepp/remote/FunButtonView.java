package com.osepp.remote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class FunButtonView extends DirButtonView {
    public FunButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Paint p=new Paint();
        p.setAntiAlias(true);
        if(this.mPressed){
            p.setColor(Color.YELLOW);
        }else {
            p.setColor(Color.GREEN);
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeMiter(2.3f);
        p.setStrokeWidth(6*dp2px);
        p.setShadowLayer(2*dp2px, dp2px, 0, Color.RED);
        canvas.drawCircle(getWidth()/2,getHeight()/2,Math.min(getWidth(),getHeight())/2,p);

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

}
