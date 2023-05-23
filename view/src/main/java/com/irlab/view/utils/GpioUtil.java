package com.irlab.view.utils;

import android.util.Log;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GpioUtil {

    public static final String Logger = GpioUtil.class.getName();

    private static FileWriter fileIn;

    public static void enableSpeaker(String targetGpio) {
        try {
            fileIn = new FileWriter(targetGpio);
            fileIn.write("1");
            fileIn.close();
            Thread.sleep(200);
            Log.d(Logger, "Enable Speaker successfully");
        } catch (Exception e) {
            Log.e(Logger, e.getMessage());
        }
    }

    public static void disableSpeaker(String targetGpio) {
        try {
            fileIn = new FileWriter(targetGpio);
            fileIn.write("0");
            fileIn.close();
            Log.d(Logger, "Disable Speaker successfully");
        } catch (IOException e) {
            Log.e(Logger, e.getMessage());
        }
    }

    public static char getSpeakerStatus(String targetGpio) {
        try {
            char[] a = new char[10];
            FileReader fileOut = new FileReader(targetGpio);
            fileOut.read(a);
            fileOut.close();
            return a[0];
        } catch (IOException e) {
            Log.e(Logger, e.getMessage());
            return ' ';
        }
    }
}
