package com.clc.baselibs.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class NFCBaseActivity<VM extends BaseViewModel, DB extends ViewDataBinding> extends BaseActivity {

    protected VM viewModel;
    public DB dataBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, setLayoutId());
        if (viewModel == null) {
            viewModel = createViewModel();
        }
        viewModel.setIView(this);
        initData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract int setLayoutId();

    protected abstract VM createViewModel();

    protected abstract void initData();
}
