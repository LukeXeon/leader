package com.dl7.player.widgets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dl7.player.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by long on 2016/11/17.
 */

public class ImageDialogFragment extends DialogFragment
{
    private Bitmap mBitmap;

    private Dialog.OnDismissListener listener;

    public void setListener(Dialog.OnDismissListener listener)
    {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setWindowAnimations(R.style.AnimateDialog);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_share, container);
        final ImageView photo = view.findViewById(R.id.iv_screenshot_photo);
        ViewGroup.LayoutParams layoutParams = photo.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels * 7 / 10;
        layoutParams.height = getResources().getDisplayMetrics().heightPixels * 7 / 10;
        photo.setLayoutParams(layoutParams);
        if (mBitmap != null)
        {
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo.setImageBitmap(mBitmap);
        }
        return view;
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        listener.onDismiss(dialog);
    }

    public void setScreenshotPhoto(Bitmap bitmap)
    {
        mBitmap = bitmap;
    }
}
