package com.cheng.heifdemo.ui.gallery;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.cheng.heifdemo.R;

public class GalleryAdapter extends ListAdapter<ImageBean, ImageViewHolder> {
    protected GalleryAdapter(@NonNull DiffUtil.ItemCallback<ImageBean> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("cheng", "viewType " + viewType);

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageBean imageBean = getItem(position);
        holder.itemView.setTag(imageBean);

        Glide.with(holder.imageView)
                .load(imageBean.contentUri)
                .thumbnail(0.4f)
                .centerCrop()
                .into(holder.imageView);
    }
}


