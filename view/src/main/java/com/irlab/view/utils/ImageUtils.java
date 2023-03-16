package com.irlab.view.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

public class ImageUtils {

    public static final int ORTHOGONAL_BOARD_IMAGE_SIZE = 500;

    /**
     * 分割位图
     *
     * @param rawBitmap 原始Bitmap
     * @param piece     分割的参数
     * @return 分割后的Bitmap数组
     */
    public static Bitmap[][] splitImage(Bitmap rawBitmap, int piece, Bitmap[][] bitmapMatrix) {
        int unitHeight = rawBitmap.getHeight() / 19;
        int unitWidth = rawBitmap.getWidth() / 19;
        Bitmap unitBitmap;
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                unitBitmap = Bitmap.createBitmap(rawBitmap, j * unitWidth, i * unitHeight, unitWidth, unitHeight);
                bitmapMatrix[i + 1][j + 1] = unitBitmap;
            }
        }
        return bitmapMatrix;
    }

    /**
     * 基于角坐标的围棋透视变换
     *
     * @param originImage  原始图像
     * @param cornerPoints 角点
     * @return 透视变化后的图像
     */
    public static Mat imagePerspectiveTransform(Mat originImage, Mat cornerPoints) {
        int x = 4256;
        int y = 4256;

        Mat resultImage = new Mat(y, x, originImage.type());
        Mat resultCorners = new Mat(4, 1, CvType.CV_32FC2);
        // 添加四个点，左下-左上-右上-右下
        resultCorners.put(0, 0, 0, 0, x, 0, x, y, 0, y);

        Mat transformationMatrix = Imgproc.getPerspectiveTransform(cornerPoints, resultCorners);
        Imgproc.warpPerspective(originImage, resultImage, transformationMatrix, resultImage.size());

        return resultImage;
    }

    /**
     * 正交变换
     *
     * @param originalImage        原始图像
     * @param boardPositionInImage 图像中的棋盘位置
     * @return 正交变化后的图像
     */
    public static Mat transformOrthogonally(Mat originalImage, Mat boardPositionInImage) {
        Mat orthogonalBoard = new Mat(ORTHOGONAL_BOARD_IMAGE_SIZE, ORTHOGONAL_BOARD_IMAGE_SIZE, originalImage.type());

        Mat orthogonalBoardCorners = new Mat(4, 1, CvType.CV_32FC2);
        orthogonalBoardCorners.put(0, 0,
                0, 0,
                ORTHOGONAL_BOARD_IMAGE_SIZE, 0,
                ORTHOGONAL_BOARD_IMAGE_SIZE, ORTHOGONAL_BOARD_IMAGE_SIZE,
                0, ORTHOGONAL_BOARD_IMAGE_SIZE);

        Mat transformationMatrix = Imgproc.getPerspectiveTransform(boardPositionInImage, orthogonalBoardCorners);
        Imgproc.warpPerspective(originalImage, orthogonalBoard, transformationMatrix, orthogonalBoard.size());

        return orthogonalBoard;
    }

    public static MatOfPoint convertToMatOfPoint(Mat boardPositionInImage) {
        Point[] corners = {
                new Point(boardPositionInImage.get(0, 0)[0], boardPositionInImage.get(0, 0)[1]),
                new Point(boardPositionInImage.get(1, 0)[0], boardPositionInImage.get(1, 0)[1]),
                new Point(boardPositionInImage.get(2, 0)[0], boardPositionInImage.get(2, 0)[1]),
                new Point(boardPositionInImage.get(3, 0)[0], boardPositionInImage.get(3, 0)[1])
        };
        MatOfPoint boardContour = new MatOfPoint(corners);
        return boardContour;
    }

    public static Bitmap matToBitmap(Mat inputFrame) {
        Bitmap bitmap = Bitmap.createBitmap(inputFrame.cols(), inputFrame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputFrame, bitmap);
        return bitmap;
    }

    /**
     * 保存图像信息
     *
     * @param bitmap   要保存的Bitmap
     * @param fileName 文件名
     */
    public static void save_bitmap(Bitmap bitmap, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory() + "/recoder");
        if (!file.exists()) file.mkdirs();
        file = new File(file + File.separator, fileName + ".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 相机Intent
     */
    public static Intent getTakePhotoIntent(Context context, File outputImagePath) {
        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            //兼容android7.0 使用共享文件的形式
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, outputImagePath.getAbsolutePath());
            Uri uri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        return intent;
    }

    /**
     * 相册Intent
     */
    public static Intent getSelectPhotoIntent() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        return intent;
    }

    /**
     * 判断sdcard是否被挂载
     */
    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 4.4及以上系统处理图片的方法
     */
    public static String getImageOnKitKatPath(Intent data, Context context) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("uri=intent.getData :", "" + uri);
        if (DocumentsContract.isDocumentUri(context, uri)) {
            //数据表里指定的行
            String docId = DocumentsContract.getDocumentId(uri);
            Log.d("getDocumentId(uri) :", "" + docId);
            Log.d("uri.getAuthority() :", "" + uri.getAuthority());
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null, context);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null, context);
        }
        return imagePath;
    }

    /**
     * 通过uri和selection来获取真实的图片路径,从相册获取图片时要用
     */
    @SuppressLint("Range")
    public static String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 比例压缩
     */
    public static Bitmap compression(Bitmap image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (outputStream.toByteArray().length / 1024 > 1024) {
            //重置outputStream即清空outputStream
            outputStream.reset();
            //这里压缩50%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        BitmapFactory.Options options = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        options.inJustDecodeBounds = false;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float height = 800f;//这里设置高度为800f
        float width = 480f;//这里设置宽度为480f

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int zoomRatio = 1;//be=1表示不缩放
        if (outWidth > outHeight && outWidth > width) {//如果宽度大的话根据宽度固定大小缩放
            zoomRatio = (int) (options.outWidth / width);
        } else if (outWidth < outHeight && outHeight > height) {//如果高度高的话根据宽度固定大小缩放
            zoomRatio = (int) (options.outHeight / height);
        }
        if (zoomRatio <= 0) {
            zoomRatio = 1;
        }
        options.inSampleSize = zoomRatio;//设置缩放比例
        options.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        //压缩好比例大小后再进行质量压缩
        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        return bitmap;
    }

    /**
     * 逆时针旋转,但是图片宽高不用变,此方法与rotateLeft和rotateRight不兼容
     *
     * @param src    源
     * @param angele 旋转的角度
     * @return 旋转后的对象
     */
    public static Mat rotate(Mat src, double angele) {
        Mat dst = src.clone();
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, angele, 1.0);
        Imgproc.warpAffine(src, dst, affineTrans, dst.size(), Imgproc.INTER_NEAREST);
        return dst;
    }

    public static Mat rotate(Mat src, int flipCode) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        /*flipCode = 0 绕x轴旋转180,也就是关于x轴对称
        flipCode = 1 绕y轴旋转180,也就是关于y轴对称
        flipCode = -1 关于原点对称*/
        Core.flip(tmp, result, flipCode);
        return result;
    }

    public static byte[] JPEGImageToByteArray(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        buffer.clear();
        return bytes;
    }

    public static Bitmap JPEGImageToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        return bitmapImage;
    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
    }

    public static Bitmap convertImageProxyToBitmap(ImageProxy image) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    /**
     * API29 中的最新保存图片到相册的方法
     */
    public static void saveImage2Gallery(Bitmap toBitmap, Context context) {
        //开始一个新的进程执行保存图片的操作
        Uri insertUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        //使用use可以自动关闭流
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(insertUri, "rw");
            if (toBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                Log.e("save_gallery", "保存成功");
            } else {
                Log.e("save_gallery", "保存失败");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    private static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    //把bitmap转换成String
    public static String bitmapToString(String filePath) {
        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //1.5M的压缩后在100Kb以内，测试得值,压缩后的大小=94486字节,压缩后的大小=74473字节
        //这里的JPEG 如果换成PNG，那么压缩的就有600kB这样
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        Log.d("d", "压缩后的大小=" + b.length);
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
