package com.irlab.view.serial;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 串口管理类 串口统一管理SerialManage
 * 简单概括一下这个类，用于管理串口的连接以及发送等功能
 * 尤其是发送指令，极短时间内发送多个指令(例如：1毫秒内发送10个指令)，多个指令之间会相互干扰。可能执行了第一个指令，可能一个都没执行。
 */
public class SerialManager {

    private static SerialManager instance;
    private final ScheduledExecutorService scheduledExecutor;  // 线程池 同一管理保证只有一个
    private SerialHandler serialHandle;  // 串口连接 发送 读取处理对象
    private final Queue<String> queueMsg = new ConcurrentLinkedQueue<>();  // 线程安全到队列
    private ScheduledFuture sendStrTask;  // 循环发送任务
    private boolean isConnect = false;  // 串口是否连接

    private SerialManager() {
        scheduledExecutor = Executors.newScheduledThreadPool(8);  //  初始化8个线程
    }

    public static SerialManager getInstance() {
        if (instance == null) {
            synchronized (SerialManager.class) {
                if (instance == null) {
                    instance = new SerialManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取线程池
     *
     * @return 实例化的线程池
     */
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * 串口初始化
     *
     * @param serialInter 串口回调接口
     */
    public void init(SerialInter serialInter) {
        if (serialHandle == null) {
            serialHandle = new SerialHandler();
            startSendTask();
        }
        serialHandle.addSerialInter(serialInter);

    }

    /**
     * 打开串口
     */
    public boolean open() {
        isConnect = serialHandle.open("/dev/ttyS1", 115200, true);  // 设置地址，波特率，开启读取串口数据
        return isConnect;
    }

    /**
     * 发送指令
     *
     * @param msg 指令字符串
     */
    public void send(String msg) {
        /*
         此处没有直接使用 serialHandle.send(msg); 方法去发送指令
         因为 某些硬件在极短时间内只能响应一个指令,232通讯一次发送多个指令会有物理干扰，
         让硬件接收到指令不准确；所以 此处将指令添加到队列中，排队执行，确保每个指令一定执行.
         若不相信可以试试用serialHandle.send(msg)方法循环发送10个不同的指令，看看10个指令
         的执行结果。
         */
        queueMsg.offer(msg);  // 向队列添加指令
    }

    /**
     * 关闭串口
     */
    public void close() {
        serialHandle.close();  // 关闭串口
    }

    // 启动发送任务
    private void startSendTask() {
        cancelSendTask();  // 先检查是否已经启动了任务 ？ 若有则取消
        // 每隔100毫秒检查一次 队列中是否有新的指令需要执行
        sendStrTask = scheduledExecutor.scheduleAtFixedRate(() -> {
            if (!isConnect) return;  // 串口未连接 退出
            if (serialHandle == null) return;  // 串口未初始化 退出
            String msg = queueMsg.poll();  // 取出指令
            if (msg == null || msg.equals("")) return;  // 无效指令 退出
            serialHandle.send(msg);  // 发送指令
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    // 取消发送任务
    private void cancelSendTask() {
        if (sendStrTask == null) return;
        sendStrTask.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendStrTask = null;
    }
}
