package com.cheng.heifdemo.ui.gallery;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ImageViewModel extends AndroidViewModel {

    public MutableLiveData<List<ImageBean>> imageBeanList = new MutableLiveData<>();

    ContentObserver contentObserver;
    String imageType;

    public ImageViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (contentObserver != null) {
            getApplication().getContentResolver().unregisterContentObserver(contentObserver);
        }
    }

    public void setImageType(String type) {
        imageType = type;
    }

    public void loadImages() {
        new Thread(() -> {
            List<ImageBean> imageBeans = queryImages();
            imageBeanList.postValue(imageBeans);
        }).start();

        if (contentObserver == null) {
            contentObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    loadImages();
                }
            };
            ContentResolver contentResolver = getApplication().getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true, contentObserver);
        }
    }

    @SuppressLint("Range")
    List<ImageBean> queryImages() {
        List<ImageBean> images = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.SIZE,
                };
        String selection = MediaStore.Images.Media.MIME_TYPE + " =?";
        String[] selectionArgs = new String[]{imageType};
        String order = MediaStore.Images.Media.DATE_ADDED + " DESC";

        ContentResolver contentResolver = getApplication().getContentResolver();

        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, order);

        StringBuilder stringBuilder = new StringBuilder();
        while(cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
            int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
            int w = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH));
            int h = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT));
            stringBuilder.append(id);
            stringBuilder.append(", ");
            stringBuilder.append(name);
            stringBuilder.append("; ");
            stringBuilder.append(path);
            stringBuilder.append("; ");
            stringBuilder.append(mime);
            stringBuilder.append("; ");
            stringBuilder.append(size);
            stringBuilder.append("; ");
            stringBuilder.append(w);
            stringBuilder.append("; ");
            stringBuilder.append(h);
            stringBuilder.append("; ");

            Uri contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
            );

            ImageBean imageBean = new ImageBean(id, path, contentUri, name, mime, size, w, h);
            images.add(imageBean);
        }
        System.out.println("******MediaLibrary " + stringBuilder);
        cursor.close();
        return images;
    }

}
