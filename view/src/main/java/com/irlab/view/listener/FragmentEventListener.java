package com.irlab.view.listener;

public interface FragmentEventListener {

    void process(String str, Long requestId);

    void event(int eventCode, int x, int y, String msg);
}
