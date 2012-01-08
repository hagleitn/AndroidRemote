package com.barbermot.remoteui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class AndroidRemote extends Activity implements View.OnTouchListener {
    
    private static final String TAG         = "AndroidRemote";
    private Joystick            left;
    private Joystick            right;
    private Thread              uiThread;
    private Thread              ioThread;
    private Map<Integer, View>  viewMap     = new HashMap<Integer, View>();
    private Map<View, Joystick> joystickMap = new HashMap<View, Joystick>();
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Log.e(TAG, "onCreate");
        
        // Bundle extras = getIntent().getExtras();
        
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        SurfaceView leftView = (SurfaceView) findViewById(R.id.surface_control_left);
        left = new Joystick(leftView, Joystick.Type.STICKY,
                Joystick.Type.SPRING_MID);
        joystickMap.put(leftView, left);
        
        SurfaceView rightView = (SurfaceView) findViewById(R.id.surface_control_right);
        right = new Joystick(rightView, Joystick.Type.SPRING_MID,
                Joystick.Type.SPRING_MID);
        joystickMap.put(rightView, right);
        
        findViewById(R.id.layout).setOnTouchListener(this);
        
        uiThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    left.draw();
                    right.draw();
                    Thread.yield();
                }
            }
        });
        uiThread.start();
        
        try {
            ioThread = new Thread(new Writer("172.16.0.24", 6001, left, right));
            ioThread.start();
        } catch (IOException e) {
            Log.e(TAG, "No connection", e);
        }
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        uiThread.interrupt();
        ioThread.interrupt();
    }
    
    private View findByXY(int x, int y) {
        ViewGroup vg = (ViewGroup) findViewById(R.id.layout);
        Rect rect = new Rect();
        
        for (int i = 0; i < vg.getChildCount(); ++i) {
            View v = vg.getChildAt(i);
            
            if (!(v instanceof SurfaceView))
                continue;
            
            v.getHitRect(rect);
            if (rect.contains(x, y)) {
                return v;
            }
        }
        return null;
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.d(TAG, "Action: " + event.getAction());
        int index = 0;
        
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                // fall through
            case MotionEvent.ACTION_DOWN:
                View v = findByXY((int) event.getX(index),
                        (int) event.getY(index));
                if (v == null) {
                    return true;
                }
                viewMap.put(event.getPointerId(index), v);
                joystickMap.get(v).setXY(event.getX(index) - v.getLeft(),
                        event.getY(index) - v.getTop());
                
                break;
            case MotionEvent.ACTION_MOVE:
                for (int pid : viewMap.keySet()) {
                    index = event.findPointerIndex(pid);
                    v = viewMap.get(pid);
                    joystickMap.get(v).setXY(event.getX(index) - v.getLeft(),
                            event.getY(index) - v.getTop());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                for (Joystick j : joystickMap.values()) {
                    j.spring();
                }
                viewMap.clear();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pid = event.getPointerId(index);
                v = viewMap.get(pid);
                joystickMap.get(v).spring();
                viewMap.remove(pid);
                break;
        }
        return true;
    }
}