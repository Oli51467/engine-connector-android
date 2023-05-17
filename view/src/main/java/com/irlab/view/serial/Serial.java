package com.irlab.view.serial;

import static com.irlab.view.activity.PlayActivity.playing;

public class Serial extends Thread {

    @Override
    public void run() {
        while (playing) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (playing) SerialManager.getInstance().send("EE30FCFF");
            }
        }
        this.interrupt();
    }
}
