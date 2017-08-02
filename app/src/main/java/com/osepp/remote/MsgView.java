package com.osepp.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MsgView extends AppCompatTextView {
    Paint mTextPaint;
    ArrayList<String> mMessages;

    public MsgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint =new Paint();
        mTextPaint.setColor(this.getTextColors().getDefaultColor());
        mTextPaint.setTextSize(this.getTextSize());
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mMessages=new ArrayList<>();
        mMessages.add("");
        appendText(this.getText().toString());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width=getWidth();
        int height=getHeight();
        int curY=height;
        curY-=getPaddingBottom();
        int istr=mMessages.size();

        while(curY>getPaddingTop()){
            istr--;
            if(istr<0)break;
            String s=mMessages.get(istr);
            if(s.trim()=="")continue;
            Rect r=new Rect();
            mTextPaint.getTextBounds(s,0,s.length(),r);

            canvas.drawText(s,getPaddingLeft(),curY-r.height(),mTextPaint);
            curY-=r.height();

        }
        if(istr>100)for(;istr>0;istr--)mMessages.remove(0);
    }

    public void appendText(String text) {
        if(text==null)return;
        char str[]=text.toCharArray();
        String lastLine=mMessages.get(mMessages.size()-1);
        int len=str.length;
        for(int i=0;i<len;i++){
            if(str[i]=='\n'){
                if(lastLine.trim().length()>0) {
                    mMessages.set(mMessages.size() - 1, lastLine);
                    mMessages.add("");
                    lastLine = "";
                }
            }else{
                lastLine+=str[i];
            }
        }

        mMessages.set(mMessages.size()-1,lastLine);
        this.invalidate();
    }
}
