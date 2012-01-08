package com.barbermot.remoteui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Joystick implements SurfaceHolder.Callback {
    
    private static final String TAG    = "AndroidRemote";
    private final static int    RADIUS = 20;
    
    public enum Type {
        STICKY, SPRING_ZERO, SPRING_MID
    };
    
    SurfaceView     view;
    SurfaceHolder   holder;
    
    float           verticalValue;
    float           horizontalValue;
    
    float           width;
    float           height;
    
    Type            verticalType;
    Type            horizontalType;
    
    Paint           boxPaint;
    Paint           linePaint;
    Paint           circlePaint;
    private boolean started;
    
    public Joystick(SurfaceView view, Type vertical, Type horizontal) {
        this.view = view;
        this.verticalType = vertical;
        this.horizontalType = horizontal;
        
        holder = view.getHolder();
        holder.addCallback(this);
        
        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Style.STROKE);
        linePaint.setStrokeWidth(1);
        
        boxPaint = new Paint();
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Style.STROKE);
        boxPaint.setStrokeWidth(2);
        
        circlePaint = new Paint();
        circlePaint.setColor(Color.YELLOW);
        circlePaint.setStyle(Style.FILL);
        
        spring();
    }
    
    public void draw() {
        if (started) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            canvas.drawLine(0, height / 2, width, height / 2, linePaint);
            canvas.drawLine(width / 2, 0, width / 2, height, linePaint);
            canvas.drawRect(boxPaint.getStrokeWidth(),
                    height - boxPaint.getStrokeWidth(),
                    width - boxPaint.getStrokeWidth(),
                    boxPaint.getStrokeWidth(), boxPaint);
            canvas.drawCircle(horizontalValue, verticalValue, RADIUS,
                    circlePaint);
            holder.unlockCanvasAndPost(canvas);
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d(TAG, "SurfaceChanged: " + width + ", " + height);
        this.width = width;
        this.height = height;
        if (verticalType == Type.STICKY) {
            this.verticalValue = height;
        } else {
            this.verticalValue = height / 2;
        }
        if (horizontalType == Type.STICKY) {
            this.horizontalValue = 0;
        } else {
            this.horizontalValue = width / 2;
        }
        spring();
        started = true;
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {}
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        started = false;
    }
    
    public void spring() {
        switch (verticalType) {
            case STICKY:
                break;
            case SPRING_MID:
                verticalValue = height / 2;
                break;
            case SPRING_ZERO:
                verticalValue = height;
                break;
        }
        
        switch (horizontalType) {
            case STICKY:
                break;
            case SPRING_MID:
                horizontalValue = width / 2;
                break;
            case SPRING_ZERO:
                horizontalValue = width;
                break;
        }
    }
    
    public float getX() {
        return horizontalValue;
    }
    
    public float getY() {
        return verticalValue;
    }
    
    public float getMaxX() {
        return width;
    }
    
    public float getMaxY() {
        return height;
    }
    
    public void setXY(float x, float y) {
        Log.d(TAG, "x: " + x + "y: " + y);
        verticalValue = y;
        horizontalValue = x;
    }
    
}
