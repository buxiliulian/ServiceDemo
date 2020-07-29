package com.uni.servicedemo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageUtil {
    private static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean saveImage(String name, byte[] bytes) {
        if (isExternalStorageMounted()) {
            File file = new File(Environment.getExternalStorageDirectory(), name);
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Bitmap getImage(String name) {
        Bitmap bitmap = null;
        if (isExternalStorageMounted()) {
            File file = new File(Environment.getExternalStorageDirectory(), name);
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                bitmap = BitmapFactory.decodeStream(fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
