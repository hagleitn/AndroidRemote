package com.barbermot.remoteui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

public class SocketConnection extends Connection {
    
    private String             url;
    private int                port;
    private Socket             socket;
    public final static String TAG = "SocketConnection";
    
    public SocketConnection(String url, int port) {
        this.url = url;
        this.port = port;
    }
    
    @Override
    protected void reEstablishConnection() throws IOException {
        
        if (socket != null) {
            try {
                socket.shutdownInput(); // workaround to get exception on read()
                socket.close();
            } catch (IOException io) {
                Log.d(TAG, "Couldn't close socket.", io);
            } finally {
                socket = null;
            }
        }
        
        socket = new Socket(url, port);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
    
}
