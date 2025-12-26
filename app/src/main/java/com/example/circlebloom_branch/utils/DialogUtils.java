package com.example.circlebloom_branch.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;

import com.example.circlebloom_branch.databinding.DialogConfirmationBinding;

public class DialogUtils {

    /**
     * Show a confirmation dialog
     */
    public static void showConfirmationDialog(Context context, String title, String message,
            String positiveText, String negativeText,
            OnConfirmListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogConfirmationBinding binding = DialogConfirmationBinding.inflate(
                LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        // Set transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set content
        binding.tvTitle.setText(title);
        binding.tvMessage.setText(message);
        binding.btnPositive.setText(positiveText);
        binding.btnNegative.setText(negativeText);

        // Set click listeners
        binding.btnPositive.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm();
            }
            dialog.dismiss();
        });

        binding.btnNegative.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Show a simple alert dialog
     */
    public static void showAlertDialog(Context context, String title, String message) {
        showConfirmationDialog(context, title, message, "OK", null, null);
    }

    public interface OnConfirmListener {
        void onConfirm();

        default void onCancel() {
        }
    }
}
