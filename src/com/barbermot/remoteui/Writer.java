package com.barbermot.remoteui;

import java.io.IOException;

public class Writer implements Runnable {
    
    SocketConnection conn;
    Joystick         left;
    Joystick         right;
    byte[]           buffer = new byte[4];
    byte[]           last   = new byte[4];
    
    public Writer(String url, int port, Joystick left, Joystick right)
            throws IOException {
        conn = new SocketConnection(url, port);
        conn.reEstablishConnection();
        this.left = left;
        this.right = right;
        
    }
    
    private byte map(float value, float max, boolean inv) {
        float mul = inv ? -1 : 1;
        return (byte) (mul * (value * 200 / max - 100));
    }
    
    private boolean changed() {
        for (int i = 0; i < buffer.length; ++i) {
            if (buffer[i] != last[i]) {
                return true;
            }
        }
        return false;
    }
    
    private void swap() {
        byte[] tmp = buffer;
        buffer = last;
        last = tmp;
    }
    
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                buffer[0] = map(right.getY(), right.getMaxY(), true);
                buffer[1] = map(right.getX(), right.getMaxX(), false);
                buffer[2] = map(left.getY(), right.getMaxY(), true);
                buffer[3] = map(left.getX(), right.getMaxX(), false);
                
                if (changed()) {
                    conn.getOutputStream().write(buffer);
                    conn.getOutputStream().flush();
                }
                
                swap();
            } catch (IOException e) {
                try {
                    conn.reconnect();
                } catch (InterruptedException e1) {
                    break;
                }
            }
            Thread.yield();
        }
    }
}
