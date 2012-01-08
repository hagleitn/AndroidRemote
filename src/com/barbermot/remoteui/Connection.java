package com.barbermot.remoteui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Connection {
    
    boolean isReconnecting = false;
    
    public final void reconnect() throws InterruptedException {
        synchronized (this) {
            if (!isReconnecting) {
                isReconnecting = true;
            } else {
                wait();
                return;
            }
        }
        
        while (true) {
            try {
                reEstablishConnection();
            } catch (IOException e) {
                Thread.sleep(1000);
                continue;
            }
            break;
        }
        
        synchronized (this) {
            isReconnecting = false;
            notifyAll();
        }
    }
    
    protected abstract void reEstablishConnection() throws IOException;
    
    public abstract OutputStream getOutputStream() throws IOException;
    
    public abstract InputStream getInputStream() throws IOException;
    
}
