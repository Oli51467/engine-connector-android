package com.irlab.view.listener;

public interface WebSocketCallback {
    void onMessage(String text);

    void onOpen();

    void onClosed();
}
