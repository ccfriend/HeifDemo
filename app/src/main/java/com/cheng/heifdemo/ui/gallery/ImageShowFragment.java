package com.cheng.heifdemo.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheng.heifdemo.databinding.ImageShowFragmentBinding;

public class ImageShowFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String[] TAB_IMAGES = {
            "image/heic",
            "image/jpeg"
    };

    private ImageShowFragmentBinding binding;
    private ImageViewModel viewModel;

    GalleryAdapter galleryAdapter;

    public static ImageShowFragment newInstance(int index) {
        ImageShowFragment fragment = new ImageShowFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        galleryAdapter = new GalleryAdapter(ImageBean.DIFF_CALLBACK);

        viewModel = new ViewModelProvider(this).get(ImageViewModel.class);
        viewModel.setImageType(TAB_IMAGES[index - 1]);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ImageShowFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.gallery;
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.imageBeanList.observe(this, (images) -> galleryAdapter.submitList(images));
        viewModel.loadImages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
