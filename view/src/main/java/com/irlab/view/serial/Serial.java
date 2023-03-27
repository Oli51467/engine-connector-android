package com.irlab.view.serial;

import static com.irlab.view.activity.PlayActivity.playing;
import static com.irlab.view.activity.PlayActivity.send;

public class Serial extends Thread {

    @Override
    public void run() {
        while (playing) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (send) SerialManager.getInstance().send("EE30FCFF");
        }
    }
}
