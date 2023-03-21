package com.irlab.view.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;

public class FileUtil {

    public static File getDownDirs(){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 将字符串写入到文本文件中
     * @param str
     * @param fileName
     */
    public static void writeTxtToFile(String str, String fileName) {
        // 每次写入时，都换行写
        String strContent = str + "\r\n";
        //生成文件夹之后，再生成文件，不然会出错
        File file = null;
        try {
            file = new File(getDownDirs(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }else{
                //清空文本内容
                FileWriter fileWriter =new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
