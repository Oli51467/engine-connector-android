package com.irlab.view.serial;

import static com.irlab.view.utils.SerialUtil.hexStr2bytes;

import android.serialport.SerialPort;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 串口处理类：SerialHandle ；简单概括这个类，就是通过串口对象去获取两个流(输入流、输出流)，通过者两个流来监听数据或者写入指令，硬件收到后执行。同时注意配置参数
 */
public class SerialHandler implements Runnable {
    private static final String TAG = "串口处理类";
    private String path = "";  // 串口地址
    private SerialPort mSerialPort;  // 串口对象
    private InputStream mInputStream;  // 串口的输入流对象
    private BufferedInputStream mBuffInputStream;  // 用于监听硬件返回的信息
    private OutputStream mOutputStream;  // 串口的输出流对象 用于发送指令
    private SerialInter serialInter;  // 串口回调接口
    private ScheduledFuture readTask;  // 串口读取任务

    /**
     * 添加串口回调
     *
     * @param serialInter 回调接口
     */
    public void addSerialInter(SerialInter serialInter) {
        this.serialInter = serialInter;
    }

    /**
     * 打开串口
     *
     * @param devicePath 串口地址(根据平板的说明说填写)
     * @param baudRate   波特率(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param isRead     是否持续监听串口返回的数据
     * @return 是否打开成功
     */
    public boolean open(String devicePath, int baudRate, boolean isRead) {
        return open(devicePath, baudRate, 8, 1, 0, isRead);
    }

    /**
     * 打开串口
     *
     * @param devicePath 串口地址(根据平板的说明说填写)
     * @param baudRate   波特率(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param dataBits   数据位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param stopBits   停止位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param parity     校验位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param isRead     是否持续监听串口返回的数据
     * @return 是否打开成功
     */
    public boolean open(String devicePath, int baudRate, int dataBits, int stopBits, int parity, boolean isRead) {
        boolean success;
        try {
            if (mSerialPort != null) close();
            File device = new File(devicePath);
            mSerialPort = SerialPort // 串口对象
                    .newBuilder(device, baudRate) // 串口地址地址，波特率
                    .dataBits(dataBits) // 数据位,默认8；可选值为5~8
                    .stopBits(stopBits) // 停止位，默认1；1:1位停止位；2:2位停止位
                    .parity(parity) // 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
                    .build(); // 打开串口并返回
            mInputStream = mSerialPort.getInputStream();
            mBuffInputStream = new BufferedInputStream(mInputStream);
            mOutputStream = mSerialPort.getOutputStream();
            success = true;
            path = devicePath;
            if (isRead) readData();  // 开启识别
        } catch (Throwable tr) {
            close();
            success = false;
        }
        return success;
    }

    // 读取数据
    private void readData() {
        if (readTask != null) {
            readTask.cancel(true);
            try {
                Thread.sleep(160);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 此处睡眠：当取消任务时 线程池已经执行任务，无法取消，所以等待线程池的任务执行完毕
            readTask = null;
        }
        readTask = SerialManager
                .getInstance()
                .getScheduledExecutor()  // 获取线程池
                .scheduleAtFixedRate(this, 0, 150, TimeUnit.MILLISECONDS);  // 执行一个循环任务
    }

    // 每隔 150 毫秒会触发一次run
    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) return;
        try {
            int available = mBuffInputStream.available();
            if (available == 0) return;
            byte[] received = new byte[1024];
            int size = mBuffInputStream.read(received);  // 读取以下串口是否有新的数据
            if (size > 0 && serialInter != null) serialInter.readData(path, received, size);
        } catch (IOException e) {
            Log.e(TAG, "串口读取数据异常:" + e);
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        try {
            if (mInputStream != null) mInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "串口输入流对象关闭异常：" + e);
        }
        try {
            if (mOutputStream != null) mOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "串口输出流对象关闭异常：" + e);
        }
        try {
            if (mSerialPort != null) mSerialPort.close();
            mSerialPort = null;
        } catch (Exception e) {
            Log.e(TAG, "串口对象关闭异常：" + e);
        }
    }

    /**
     * 向串口发送指令
     */
    public void send(final String msg) {
        byte[] bytes = hexStr2bytes(msg);  // 字符转成byte数组
        try {
            mOutputStream.write(bytes);  // 通过输出流写入数据
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
