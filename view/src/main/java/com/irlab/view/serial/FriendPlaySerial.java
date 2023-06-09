package com.irlab.view.serial;

import static com.irlab.view.activity.FriendsPlayActivity.playing;

public class FriendPlaySerial extends Thread {

    @Override
    public void run() {
        while (playing) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (playing) SerialManager.getInstance().send("EE30FCFF");
            }
        }
        this.interrupt();
    }
}
