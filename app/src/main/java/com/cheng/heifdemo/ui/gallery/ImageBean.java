package com.cheng.heifdemo.ui.gallery;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Date;

public class ImageBean {
    long id;
    String displayName;
    String path;
    Uri contentUri;
    String mimetype;
    int size;
    int width;
    int height;

    public ImageBean(long identity, String data, Uri uri, String name, String type, int imgSize,
                     int w, int h) {
        id = identity;
        path = data;
        contentUri = uri;
        displayName = name;
        mimetype = type;
        size = imgSize;
        width = w;
        height = h;
    }

    @NonNull
    @Override
    public String toString() {
        return "id: " + id
                + "\npath: " + path
                + "\nmimetype: " + mimetype
                + "\nwidth: " + width
                + "\nheight: " + height
                + "\nsize: " + size;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public static DiffUtil.ItemCallback<ImageBean> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ImageBean>() {
                @Override
                public boolean areItemsTheSame(@NonNull ImageBean oldItem, @NonNull ImageBean newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ImageBean oldItem, @NonNull ImageBean newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
