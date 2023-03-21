package com.irlab.view.serial;

import static com.irlab.view.activity.PlayActivity.resign;

public class Serial extends Thread {

    @Override
    public void run() {
        while(true) {
            SerialManager.getInstance().send("EE30FCFF");
        }
    }
}
