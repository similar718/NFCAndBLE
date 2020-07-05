package com.nfc.cn.login;

import android.view.View;

import com.clc.baselibs.base.NFCBaseActivity;
import com.nfc.cn.R;
import com.nfc.cn.databinding.ActivityLoginBinding;
import com.nfc.cn.login.vm.LoginViewModel;
import com.nfc.cn.manager.IntentManager;

public class LoginActivity extends NFCBaseActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected int setLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected LoginViewModel createViewModel() {
        viewModel = new LoginViewModel();
        viewModel.setIView(this);
        return viewModel;
    }

    @Override
    protected void initData() {
        dataBinding.setModel(viewModel);
        dataBinding.setActivity(this);
//        StatusBarUtils.changeStatusBarColor(this, R.color.main_color);


        dataBinding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentManager.getInstance().goMainActivity(LoginActivity.this);
            }
        });
    }
}
