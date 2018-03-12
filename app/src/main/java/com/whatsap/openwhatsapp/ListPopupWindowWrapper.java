package com.whatsap.openwhatsapp;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

public class ListPopupWindowWrapper {
    private static final boolean debugSpinner = false;
    private ListPopupWindow list;
    private ArrayAdapter<String> private_adapter;
    private Context private_context;
    private OnItemClickListener private_listener;

    public ListPopupWindowWrapper(Context context, View view) {
        if (VERSION.SDK_INT >= 11) {
            this.list = new ListPopupWindow(context);
            this.list.setAnchorView(view);
            this.list.setModal(true);
            this.list.setPromptPosition(1);
            this.list.setInputMethodMode(2);
            return;
        }
        this.private_context = context;
    }

    public void setAdapter(ArrayAdapter<String> adapter) {
        if (VERSION.SDK_INT >= 11) {
            this.list.setAdapter(adapter);
        } else {
            this.private_adapter = adapter;
        }
    }

    public void dismiss() {
        if (VERSION.SDK_INT >= 11) {
            this.list.dismiss();
        }
    }

    public void setOnItemClickListener(OnItemClickListener o) {
        if (VERSION.SDK_INT >= 11) {
            this.list.setOnItemClickListener(o);
        } else {
            this.private_listener = o;
        }
    }

    public void show() {
        if (VERSION.SDK_INT >= 11) {
            this.list.show();
            return;
        }
        CharSequence[] items = new CharSequence[this.private_adapter.getCount()];
        for (int i = 0; i < this.private_adapter.getCount(); i++) {
            items[i] = (CharSequence) this.private_adapter.getItem(i);
        }
        new Builder(this.private_context).setItems(items, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ListPopupWindowWrapper.this.private_listener.onItemClick(null, null, i, 0);
            }
        }).show();
    }
}
