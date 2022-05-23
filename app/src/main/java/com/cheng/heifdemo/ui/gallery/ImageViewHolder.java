package com.cheng.heifdemo.ui.gallery;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheng.heifdemo.HeifUtils;
import com.cheng.heifdemo.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View rootView;
    public ImageView imageView;
    private String mimeType;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        rootView = itemView;
        imageView = itemView.findViewById(R.id.image);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        ImageBean imageBean = (ImageBean) rootView.getTag();
        mimeType = imageBean.mimetype;
        new MaterialAlertDialogBuilder(view.getContext())
                .setMessage(imageBean.toString())
                .setTitle(R.string.imageinfo)
                .setPositiveButton(
                        mimeType.equals("image/heic") ? R.string.heic2jpg : R.string.jpg2heic,
                        (var1, var2) -> {
                            if (mimeType.equals("image/heic")) {
                                HeifUtils.convertHeifToJgp(view.getContext(), imageBean.contentUri);
                            } else {
                                HeifUtils.convertJgpToHeic(view.getContext(), imageBean.contentUri);
                            }
                        })
                .setNegativeButton(R.string.app_name,
                        (var1, var2) -> var1.dismiss())
                .show();
    }
}
