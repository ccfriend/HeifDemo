package com.cheng.heifdemo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import androidx.heifwriter.HeifWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HeifUtils {
    private static final String TAG = "HeifUtils";
    private static MediaScannerConnection connection;

    public static Uri convertHeifToJgp(Context context, Uri heifUri) {
        InputStream heifStream = null;
        try {
            // open file
            heifStream = context.getContentResolver().openInputStream(heifUri);
            if (heifStream == null) {
                return heifUri;
            }

            // decode to Bitmap
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeStream(heifStream, null, option);
            if (bitmap == null) {
                return heifUri;
            }

            // encode to Jpg
            String fileName = getFileNameFromUri(context, heifUri);
            String jpgFilePath =
                    generateJpgFile(context, fileName + ".jpg", bitmap);

            // trigger scan
            scanFile(context, jpgFilePath, "image/jpeg");

        } catch (Exception e) {
            Log.e("cheng", "e " + e.getMessage());
        } finally {
            try {
                if (heifStream != null) {
                    heifStream.close();
                }
            } catch (IOException e) {
                Log.e("cheng", "close heifStream stream fail");
            }
        }

        return null;
    }

    @SuppressLint("Range")
    private static String getFileNameFromUri(Context context, Uri sourceUri) {
        Log.d(TAG, "Origin Uri : " + sourceUri);
        String fileName = null;
        Cursor cursor = null;
        try {
            String[] projection = new String[]{
                    MediaStore.Files.FileColumns.TITLE
            };
            cursor = context.getContentResolver().query(
                    sourceUri,
                    projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                Log.d(TAG, "fileName = " + fileName);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "query name", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "File name : " + fileName);
        return fileName + "_" + getStringDate();
    }

    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @SuppressLint("Range")
    private static String getFilePathFromUri(Context context, Uri myUri) {
        Log.d(TAG, "Origin Uri : " + myUri);
        String filePath = null;
        Cursor cursor = null;
        try {
            String[] projection = new String[]{
                    MediaStore.Files.FileColumns.DATA
            };
            cursor = context.getContentResolver().query(
                    myUri,
                    projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "query name", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "File name : " + filePath);
        return filePath;
    }


    private static String generateJpgFile(Context context, String jpgFileName, Bitmap heifBitmap) {
        OutputStream jpgStream = null;
        try {
            // 应用目录
            Uri testCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            ContentValues testDetails = new ContentValues();
            testDetails.put(MediaStore.Images.Media.DISPLAY_NAME, jpgFileName);
//            testDetails.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ScreenShots");
            Uri dstUri = context.getContentResolver().insert(testCollection, testDetails);
            if (dstUri == null) {
                return null;
            }

            jpgStream = context.getContentResolver().openOutputStream(dstUri, "rw");
            heifBitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgStream);
            jpgStream.flush();
            return getFilePathFromUri(context, dstUri);
        } catch (IOException e) {
            Log.e(TAG, "generateJpgFileUri", e);
        } finally {
            try {
                if (jpgStream != null) {
                    jpgStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close jpgStream stream", e);
            }
        }
        return null;
    }

    private static void scanFile(Context context, String path, String mediaType) {
        if (connection == null) {
            connection = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    Log.d(TAG, "onMediaScannerConnected");
                    connection.scanFile(path, mediaType);
                }

                @Override
                public void onScanCompleted(String data, Uri uri) {
                    Log.d(TAG,"onScanCompleted " + uri);
                    connection.disconnect();
                }
            });
        }
        connection.connect();
    }

    private static String generateHeicFile(Context context, String dstFileName, Bitmap sourceBitmap) {
        ParcelFileDescriptor fileDescriptor = null;
        try {
            // 公共目录
            Uri testCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            ContentValues testDetails = new ContentValues();
            testDetails.put(MediaStore.Images.Media.DISPLAY_NAME, dstFileName);
            Uri dstUri = context.getContentResolver().insert(testCollection, testDetails);
            if (dstUri == null) {
                return null;
            }

            fileDescriptor = context.getContentResolver().openFileDescriptor(dstUri, "rw");

            // Create HEIF Writer
            HeifWriter.Builder builder = new HeifWriter.Builder(fileDescriptor.getFileDescriptor(),
                    sourceBitmap.getWidth(),
                    sourceBitmap.getHeight(), HeifWriter.INPUT_MODE_BITMAP);
            HeifWriter heifWriter = builder.setQuality(100)
                    .setRotation(0)
                    .build();

            heifWriter.start();
            heifWriter.addBitmap(sourceBitmap);
            heifWriter.stop(2000);
            heifWriter.close();
            return getFilePathFromUri(context, dstUri);
        } catch (Exception e) {
            Log.e(TAG, "generateJpgFileUri", e);
            e.printStackTrace();
        } finally {
            try {
                if (fileDescriptor != null) {
                    Os.close(fileDescriptor.getFileDescriptor());
                }
            } catch (ErrnoException e) {
                Log.e(TAG, "close jpgStream stream", e);
            }
        }
        return null;
    }

    public static Uri convertJgpToHeic(Context context, Uri jpegUri) {
        InputStream jpgStream = null;
        try {
            // open file
            jpgStream = context.getContentResolver().openInputStream(jpegUri);
            if (jpgStream == null) {
                return jpegUri;
            }

            // decode to Bitmap
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeStream(jpgStream, null, option);
            if (bitmap == null) {
                return jpegUri;
            }

            // encode to heic
            String fileName = getFileNameFromUri(context, jpegUri);
            String heicFilePath =
                    generateHeicFile(context, fileName + ".heic", bitmap);

            // trigger scan
            scanFile(context, heicFilePath, "image/heic");

        } catch (Exception e) {
            Log.e("cheng", "e " + e.getMessage());
        } finally {
            try {
                if (jpgStream != null) {
                    jpgStream.close();
                }
            } catch (IOException e) {
                Log.e("cheng", "close stream fail");
            }
        }

        return null;
    }
}
