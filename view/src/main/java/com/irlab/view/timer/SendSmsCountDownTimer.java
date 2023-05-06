package com.irlab.view.timer;

import android.os.Handler;
import android.widget.Button;

/**
 * 自定义倒计时类，实现Runnable接口
 */
public class SendSmsCountDownTimer implements Runnable {

    private final Handler handler;
    private final Button button;
    private Integer T = 10;

    public SendSmsCountDownTimer(Handler handler, Button button) {
        this.handler = handler;
        this.button = button;
    }

    @Override
    public void run() {

        //倒计时开始，循环
        while (T > 0) {
            handler.post(() -> {
                button.setClickable(false);
                button.setEnabled(false);
                button.setText(String.valueOf(T));
            });
            try {
                Thread.sleep(1000); //强制线程休眠1秒，就是设置倒计时的间隔时间为1秒。
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            T--;
        }

        //倒计时结束，也就是循环结束
        handler.post(() -> {
            button.setClickable(true);
            button.setEnabled(true);
            button.setText("发送");
        });
        T = 10; //最后再恢复倒计时时长
    }
}
