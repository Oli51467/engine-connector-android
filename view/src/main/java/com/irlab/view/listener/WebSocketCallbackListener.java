package com.irlab.view.listener;

public interface WebSocketCallbackListener {
    void onMessage(String text);

    void onOpen();

    void onClosed();
}
