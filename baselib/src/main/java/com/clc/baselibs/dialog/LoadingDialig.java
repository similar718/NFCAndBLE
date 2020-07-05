package com.clc.baselibs.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.clc.baselibs.R;

/**
 * 加载dialog
 */
public class LoadingDialig extends AlertDialog{
    private String content="";
    private Context mContext;
    private TextView tips;
    private ImageView spinnerImageView;

    public LoadingDialig(Context context) {
        super(context);
        mContext = context;
    }

    public LoadingDialig(Context context, String s) {
        super(context);
        content = s;
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        tips = findViewById(R.id.message);
        spinnerImageView = findViewById(R.id.spinnerImageView);
        if (!TextUtils.isEmpty(content)){
            tips.setText(content);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT ;
        getWindow().setAttributes(params);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
//        Glide.with(mContext).load(R.drawable.loading).into(spinnerImageView);
    }

    public void setTipsText(String tipsText){
        content=tipsText;
        if (tips != null){
            tips.setText(content);
        }
    }
}
