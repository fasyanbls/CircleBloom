package com.example.circlebloom_branch.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.circlebloom_branch.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Utility class for creating and managing loading dialogs
 */
public class LoadingDialog {

    private AlertDialog dialog;
    private Context context;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    /**
     * Show loading dialog
     */
    public void show() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    /**
     * Show loading dialog with custom message
     */
    public void show(String message) {
        show();
        // Can be enhanced to show custom message
    }

    /**
     * Dismiss loading dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Check if dialog is showing
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
