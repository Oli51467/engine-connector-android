package com.irlab.view.service;

import static com.irlab.view.common.Constants.RECONNECT_TIMEOUT;
import static com.irlab.view.common.Constants.WEBSOCKET_SERVER;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.irlab.base.utils.SPUtils;
import com.irlab.view.listener.WebSocketCallbackListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends Service {

    private static final String TAG = WebSocketService.class.getName();

    private WebSocket webSocket;
    private WebSocketCallbackListener webSocketCallbackListener;
    private boolean connected = false;

    private final Handler handler = new Handler();

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        webSocket = connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            close();
        }
    }

    private WebSocket connect() {
        Log.d(TAG, "connect " + WEBSOCKET_SERVER);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(WEBSOCKET_SERVER + SPUtils.getString("jwt")).build();
        return client.newWebSocket(request, new WebSocketHandler());
    }

    public void send(String text) {
        Log.d(TAG, "send " + text);
        if (webSocket != null) {
            webSocket.send(text);
        }
    }

    public void close() {
        if (webSocket != null) {
            boolean shutDownFlag = webSocket.close(1000, "manual close");
            Log.d(TAG, "shutDownFlag " + shutDownFlag);
            webSocket = null;
        }
    }

    private void reconnect() {
        handler.postDelayed(() -> {
            Log.d(TAG, "reconnect...");
            if (!connected) {
                connect();
            }
        }, RECONNECT_TIMEOUT);
    }

    private class WebSocketHandler extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            if (webSocketCallbackListener != null) {
                webSocketCallbackListener.onOpen();
            }
            connected = true;
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            if (webSocketCallbackListener != null) {
                webSocketCallbackListener.onMessage(text);
            }
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            if (webSocketCallbackListener != null) {
                webSocketCallbackListener.onClosed();
            }
            connected = false;
        }

        /**
         * 当WebSocket由于读取或写入错误而关闭时调用
         * 传出和传入的消息可能都已丢失。该接口将不会进一步监听
         */
        @Override
        public void onFailure(@NonNull WebSocket webSocket, Throwable t, Response response) {
            Log.d(TAG, "onFailure " + t.getMessage());
            connected = false;
            reconnect();
        }
    }

    public void setWebSocketCallback(WebSocketCallbackListener webSocketCallbackListener) {
        this.webSocketCallbackListener = webSocketCallbackListener;
    }
}

