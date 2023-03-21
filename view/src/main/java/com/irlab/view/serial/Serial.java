package com.irlab.view.serial;

import static com.irlab.view.activity.PlayActivity.resign;

public class Serial extends Thread {

    @Override
    public void run() {
        while (!resign) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SerialManager.getInstance().send("EE30FCFF");
        }
    }
}
