package com.irlab.view.jni;

public class GpioJni {
    static {
        System.loadLibrary("native-lib");
    }
    public native int SetGpio();
}
