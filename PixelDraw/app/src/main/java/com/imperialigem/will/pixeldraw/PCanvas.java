package com.imperialigem.will.pixeldraw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Rect;
import java.util.Arrays;

public class PCanvas extends View {



    private Paint paint;
    private int width;
    private int height;
    private int brushstate = 1;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Rect[][] rects;
    private Patt pattern;
    public Bitmap bitmap;
    private Canvas canvas;

    public PCanvas(Context context){
        this(context, null);
    }

    public PCanvas(Context context, AttributeSet attrs){
        super(context, attrs);
        rects = new Rect[10][10];
        pattern = new Patt();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics){
        height = metrics.widthPixels;
        int fracheight = height/10;
        width = metrics.widthPixels;
        int fracwidth = width/10;
        bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        for (int i = 0;i<10;i++){
            for(int j=0;j<10;j++){
                rects[i][j] = new Rect(i*fracwidth,j*fracheight,(i+1)*fracwidth,(j+1)*fracheight);
            }
        }
    }

    public void clear(){
        pattern.setData(new int[10][10]);
        invalidate();
    }

    public void setBrush(){
        brushstate = 1;
    }
    public void setEraser(){
        brushstate = 0;
    }
    public void brushOff(){
        brushstate = 2;
    }
    public void setPattern(Patt p){
        pattern = p;
        invalidate();
    }
    public void setPattern(int[][] d, int c){
        pattern.setData(d);
        pattern.setColor(c);
        invalidate();
    }
    public void setColor(int c){
        pattern.setColor(c);
    }

    public String sendData(){//Converts data to 100-long String and sends
        //String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        int data[][] = pattern.getData();
        for (int i = 0;i < 10; i++){
            for(int j = 0; j < 10; j++){
                sb.append((char)(data[j][i]+48));
            }
        }
        sb.append(">");
        return sb.toString();
    }

    public int[] returnindex(float x, float y){
        float xfrac = x/(float)width;
        float yfrac = y/(float)height;
        int[] index = {(int)(10*xfrac), (int)(10*yfrac)};
        return index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.save();
        for (int i = 0;i<10;i++){
            for(int j = 0;j<10;j++){
                if(pattern.getData()[i][j] == 1){
                    paint.setColor(pattern.getColor());
                }
                else{
                    paint.setColor(Color.WHITE);
                }
                canvas.drawRect(rects[i][j],paint);
            }
        }

        canvas.drawBitmap(bitmap,0,0,bitmapPaint);
        //canvas.restore();
    }




    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        if (x < (float)width && x > 0 && y < (float)height && y > 0) {
            int[] index = returnindex(x, y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (pattern.getData()[index[0]][index[1]] == 0) {
                        if (brushstate == 1) {
                            pattern.setData(index, 1);
                            invalidate();
                        }
                    } else if (pattern.getData()[index[0]][index[1]] == 1) {
                        if (brushstate == 0) {
                            pattern.setData(index, 0);
                            invalidate();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (pattern.getData()[index[0]][index[1]] == 0 && brushstate == 1) {
                        pattern.setData(index, 1);
                        invalidate();
                    } else if (pattern.getData()[index[0]][index[1]] == 1 && brushstate == 0) {
                        pattern.setData(index, 0);
                        invalidate();
                    }
                    break;
            }
        }

        return true;
    }


}
