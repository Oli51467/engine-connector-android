package com.irlab.view.serial;

import static com.irlab.view.activity.PlayActivity.playing;

public class Serial extends Thread {

    @Override
    public void run() {
        while (playing) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SerialManager.getInstance().send("EE30FCFF");
        }
    }
}
