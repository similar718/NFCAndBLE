package com.nfc.cn.spashinfo;

import com.clc.baselibs.base.NFCBaseActivity;
import com.nfc.cn.R;
import com.nfc.cn.databinding.ActivitySpashBinding;
import com.nfc.cn.manager.IntentManager;
import com.nfc.cn.spashinfo.vm.SpashViewModel;

public class SpashActivity extends NFCBaseActivity<SpashViewModel, ActivitySpashBinding> {

    @Override
    protected int setLayoutId() {
        return R.layout.activity_spash;
    }

    @Override
    protected SpashViewModel createViewModel() {
        viewModel = new SpashViewModel();
        viewModel.setIView(this);
        return viewModel;
    }

    @Override
    protected void initData() {
        dataBinding.setModel(viewModel);
        dataBinding.setActivity(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
//                    IntentManager.getInstance().goLoginActivity(SpashActivity.this);
                    IntentManager.getInstance().goMainActivity(SpashActivity.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}