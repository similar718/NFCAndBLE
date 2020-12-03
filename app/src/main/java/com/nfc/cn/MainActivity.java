package com.nfc.cn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.clc.baselibs.base.NFCBaseActivity;
import com.clc.baselibs.utils.ToastUtils;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.libs.config.Constants;
import com.clj.fastble.libs.utils.GPSUtils;
import com.clj.fastble.nfc.BleNFCListener;
import com.clj.fastble.nfc.BleNFCManager;
// import com.nfc.cn.application.NFCBleApplication;
// import com.nfc.cn.ble.BleDeviceManager;
import com.nfc.cn.bean.NotifyBLEDataConstructerBean;
import com.nfc.cn.databinding.ActivityMainBinding;
import com.nfc.cn.listener.SocketListener;
import com.nfc.cn.nfcres.NfcHandler;
import com.nfc.cn.nfcres.NfcView;
import com.nfc.cn.service.GPSService;
import com.nfc.cn.service.KeepAppLifeService;
import com.nfc.cn.udp.UDPThread;
import com.nfc.cn.utils.NetWorkUtils;
import com.nfc.cn.vm.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends NFCBaseActivity<MainViewModel, ActivityMainBinding> {

    private boolean mIsOpenGPS = false;
    private boolean mIsOpenBT = false;

    public static boolean mIsInitBleHandler = false;

    private final int HANDLER_INIT_IMAGEVIEW = 0x0101;
    private final int HANDLER_INIT_IMAGEVIEW_NFC = 0x0102;
    private final int HANDLER_SEND_SERVER = 0x0103;
    private final int HANDLER_SEND_SERVER_UDP_STATUS = 0x0104;

    private Context mContext;

    private String  mBleName = "";

    private boolean mInitSuccess = false; // 是否默认蓝牙插件初始化成功
    private boolean mClickInit = false; // 是否默认点击扫描初始化

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private boolean mIsActiity = false;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private static int count = 0;
    private boolean isPause = false;
    private boolean isStop = true;
    private static int delay = 1000;  //1s
    private static int period = 1000;  //1s

    private String TAG = MainActivity.class.getSimpleName();


    // NFC 硬件相关东西
    private NfcHandler mNfcHandler;
    private boolean mIsRequestNFCUid = false;
    private boolean mIsOpenNFC = true;

    String url = "http://119.23.226.237:9099/dataReception";

    String mServerData = "";

    private UDPThread udpThread;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected MainViewModel createViewModel() {
        viewModel = new MainViewModel();
        viewModel.setIView(this);
        return viewModel;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void initData() {
        dataBinding.setModel(viewModel); // 初始化数据
        dataBinding.setActivity(this);
        mContext = this;

        dataBinding.tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleName = dataBinding.etBleName.getText().toString().trim(); // 去掉前后的空格
                if (!mBleName.isEmpty()){
                    Constants.mBleName = mBleName;
//                    SPUtils.putString(SPUtils.BLE_NAME,mBleName); // 将数据进行保存
                }
                StartConnectListener();
            }
        });

        // 初始化蓝牙设备的状态
        initBlueTooth();

        // 初始化NFC数据
        mNfcHandler = new NfcHandler(mNFCView);
        mNfcHandler.init(this);

        udpThread = new UDPThread();
        udpThread.setSocketListener(mSockestListener);
        udpThread.start();
    }

    private void startTimer() {
        if (mTimer != null && mTimerTask != null) {
            stopTimer();
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "count: " + String.valueOf(count));
                    do {
                        try {
                            Log.i(TAG, "sleep(1000)...");
                            Thread.sleep(1000);
                            if (isStop) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dataBinding.tvStatus.setText("当前状态：正在搜索设备");
                                    }
                                });
                                isStop = false;
                                startThread();
                            }
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);
                    count++;
                }
            };
        }
        if (mTimer != null && mTimerTask != null) {
            mTimer.schedule(mTimerTask, delay, period);
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        count = 0;
    }

    // 开始连接的操作
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void StartConnectListener(){
        if (GPSUtils.isOPen(mContext)) {
            if (NetWorkUtils.isNetConnected(mContext)) {
                checkPermissions();
            } else {
                Toast.makeText(mContext, "需要网络才能正常使用哦", Toast.LENGTH_LONG).show();
            }
        } else {
            mIsOpenGPS = false;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW);
            Toast.makeText(mContext, "请打开GPS定位权限", Toast.LENGTH_LONG).show();
        }
    }

    // 主线程的Handler用来刷新界面
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void handleMessage(@NonNull Message msg) {
            // 由主线程中的Looper不断的loop将handler里面的信息不断的轮询，将符合要求的数据dispatchMessage分发
            // 到主线程的handlerMessage进行更新界面的数据
            switch (msg.what){
                case HANDLER_INIT_IMAGEVIEW:
                    mIsOpenGPS = GPSUtils.isOPen(mContext);
                    mIsOpenBT = BleNFCManager.getInstance().BleIsOpen(); // TODO
//                    mIsOpenBT = BleDeviceManager.getInstance().BleIsOpen(); // TODO

                    dataBinding.ivFlagOpenBt.setImageResource(mIsOpenBT ? R.drawable.ic_bluetooth_black_24dp : R.drawable.ic_bluetooth_disabled_black_24dp);
                    dataBinding.ivFlagOpenGps.setImageResource(mIsOpenGPS ? R.drawable.ic_location_place_black_24dp : R.drawable.ic_location_no_place_black_24dp);
//                    mBleName = SPUtils.getString(SPUtils.BLE_NAME);
//                    if (!mBleName.isEmpty()) {
//                        dataBinding.etBleName.setText(mBleName + "");
//                    }
                    break;
                case HANDLER_INIT_IMAGEVIEW_NFC:
                    dataBinding.ivFlagOpenNfc.setImageResource(mIsOpenNFC ? R.drawable.ic_nfc_black_24dp : R.drawable.ic_nfc_no_black_24dp);

                    if (mIsOpenNFC) { // 如果出现就开始获取nfc数据
                        getNFCInfo();
                    }
                    break;
                case HANDLER_SEND_SERVER:
                    updateServerData(mServerData);
                    break;
                case HANDLER_SEND_SERVER_UDP_STATUS:
                    dataBinding.tvServerStatus.setText(mUDPStatusStr);
                    break;
            }
        }
    };

    private void startThread() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                BleNFCManager.getInstance().getBleNFCInfo(); // TODO
//                BleDeviceManager.getInstance().scanDevice(); // TODO
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBlueTooth() {
        BleNFCManager.getInstance().initBleNFC(getApplication(),MainActivity.this,mListener); // TODO
//        BleDeviceManager.getInstance().initBleNFC(NFCBleApplication.getInstance(),MainActivity.this,mScanConnectDeviceCallback); // TODO
    }


    /*private ScanConnectDeviceCallback mScanConnectDeviceCallback = new ScanConnectDeviceCallback() {
        @Override
        public void initFailed(byte data) {// TODO  初始化失败 需要配合相关操作之后再重新初始化
            if (data == (byte) 0x0001){ //没有打开GPS的情况
                mIsOpenGPS = false;
                Toast.makeText(mContext,"请打开GPS位置信息",Toast.LENGTH_LONG).show();
            } else if (data == (byte) 0x0010) { // 判断是否打开蓝牙设备
                mIsOpenBT  = false;
                Toast.makeText(mContext,"请打开蓝牙",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext,"初始化失败，其他情况",Toast.LENGTH_LONG).show();
            }
            mInitSuccess = false;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW);
        }

        @Override
        public void initSuccess() {
            // 初始化成功 可以正常的扫描设备
            mInitSuccess = true;
            if (mClickInit){
                mClickInit = false;
                initBleAndStartScan();
            }
        }

        @Override
        public void scanDevice() {// 扫描到目标设备
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "搜索到目标设备", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "搜索到目标设备");
                    dataBinding.tvStatus.setText("当前状态：搜索到目标设备正在连接中");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });

        }

        @Override
        public void scanNotDevice() { // 未扫描到目标设备
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "未搜索到目标设备", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "未搜索到目标设备");
                    dataBinding.tvStatus.setText("当前状态：未搜索到目标设备 请打开设备之后重试");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });

        }

        @Override
        public void startConnDevice() { // 开始连接
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "开始连接");
                    dataBinding.tvStatus.setText("当前状态：开始连接");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void startConnNoSupport() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：开始连接 不支持连接");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void connSuccesDevice() { // 连接成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "连接成功", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接成功");
                    dataBinding.tvStatus.setText("当前状态：连接成功 正准备获取数据");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void getDeviceDataOriginal(final String scanDeviceData) { // 拿取到原始的数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据（原始）");
                    dataBinding.tvOriginal.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void checkDataIsFailure(String mac, String devID, String calDevID, String data) { // 校验广播信息失败 devID 与mac不匹配
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据Mac与DevID校验失败");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    // 拼接校验结果和数据信息
                    StringBuilder checkData = new StringBuilder();
                    checkData
                            .append("mac: " + mac + "\n")
                            .append("devId: " + devID + "\n")
                            .append("cal devId: " + calDevID + "\n")
                            .append("all Data: " + data + "\n")
                    ;
                    dataBinding.tvCheckData.setText(checkData.toString());
                }
            });
        }

        @Override
        public void getDeviceData(final String scanDeviceData) {// 拿取到数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据(已替换数据)");
                    dataBinding.tvServer.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
//                    mServerData = scanDeviceData; // 不做上传
//                    mHandler.sendEmptyMessage(HANDLER_SEND_SERVER);
                }
            });

        }

        @Override
        public void getConnDeviceData(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的数据(已连接设备)");
                    dataBinding.tvServer.setText(scanDeviceData);
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
//                    mServerData = scanDeviceData;
//                    mHandler.sendEmptyMessage(HANDLER_SEND_SERVER);
                }
            });
        }

        @Override
        public void getNotifyConnDeviceSuccess(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的打开通知成功");
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void getNotifyConnDeviceFail(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的打开通知失败~~~~~~~");
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        private StringBuilder dataText = new StringBuilder();

        @Override
        public void getNotifyConnDeviceData(String scanDeviceData) {
            Log.e("oooooooooooooo","data = " + dataText.toString() + " scandata = " + scanDeviceData);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的通知成功获取数据信息");
                    dataText.append(scanDeviceData + "\n");
                    dataBinding.tvCheckData.setText(dataText.toString());
                    dataBinding.tvServer.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void connFailedDevice() { // 连接失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "连接失败", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接失败");
                    dataBinding.tvStatus.setText("当前状态：连接失败");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void disConnDevice() { // 断开连接
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "断开连接", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "断开连接");
                    dataBinding.tvStatus.setText("当前状态：设备 断开连接");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void connNotDesDevice() { // 断开连接 连接的设备不是我需要的数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "断开连接 连接的设备不是我需要的数据", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接的设备不是我需要的数据");
                    dataBinding.tvStatus.setText("当前状态：设备 断开连接 连接的设备不是我需要的数据");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void replyDataToDeviceSuccess(String data) {  // 回复硬件蓝牙成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：回复设备（" + data +" ）成功");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    dataBinding.tvReplyDev.setText(data + "------回复成功");
                }
            });
        }

        @Override
        public void replyDataToDeviceFailed(String data) {  // 回复硬件蓝牙失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：回复设备（" + data +" ）失败----呜呜呜");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    dataBinding.tvReplyDev.setText(data + "------回复失败");
                }
            });
        }
    };*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            mIsOpenBT = false;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW);
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WAKE_LOCK};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    if (mInitSuccess) {
                        initBleAndStartScan();
//                        startGPSService();
                    } else {
                        mClickInit = true;
                        initBlueTooth();
                    }
                }
                break;
        }
    }

    private void initBleAndStartScan(){
        dataBinding.tvStatus.setText("当前状态：正在搜索设备");
        isStop = false;
        startThread();
        startTimer();
        String mLocation = "蓝牙插件定位信息\n经度：" + Constants.mLatitude + "\n纬度：" + Constants.mLongitude;
        dataBinding.tvLocation.setText(mLocation);
    }

    private void setStopGPSService() {
        this.getApplicationContext().stopService(new Intent(this, GPSService.class));
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) { // 位置设置之后的调用
            if (checkGPSIsOpen()) {
                initBleAndStartScan();
            }
        }
    }

    private BleNFCListener mListener = new BleNFCListener() {
        @Override
        public void initFailed(byte data) {// TODO  初始化失败 需要配合相关操作之后再重新初始化
            if (data == (byte) 0x0001){ //没有打开GPS的情况
                mIsOpenGPS = false;
                Toast.makeText(mContext,"请打开GPS位置信息",Toast.LENGTH_LONG).show();
            } else if (data == (byte) 0x0010) { // 判断是否打开蓝牙设备
                mIsOpenBT  = false;
                Toast.makeText(mContext,"请打开蓝牙",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext,"初始化失败，其他情况",Toast.LENGTH_LONG).show();
            }
            mInitSuccess = false;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW);
        }

        @Override
        public void initSuccess() {
            // 初始化成功 可以正常的扫描设备
            mInitSuccess = true;
            if (mClickInit){
                mClickInit = false;
                initBleAndStartScan();
            }
        }

        @Override
        public void scanDevice(String names) {
            dataText.append(names + "\n");
            dataBinding.tvCheckData.setText(dataText.toString());
        }

        @Override
        public void scanDevice() {// 扫描到目标设备
            dataText.append("");
            dataBinding.tvCheckData.setText(dataText.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvReplyDev.setText("");
                    mIsParseSuccess = false;
                    Toast.makeText(mContext, "搜索到目标设备", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "搜索到目标设备");
                    dataBinding.tvStatus.setText("当前状态：搜索到目标设备正在连接中");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });

        }

        @Override
        public void scanNotDevice() { // 未扫描到目标设备
            dataText.append("");
            dataBinding.tvCheckData.setText(dataText.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvReplyDev.setText("");
                    mIsParseSuccess = false;
                    Toast.makeText(mContext, "未搜索到目标设备", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "未搜索到目标设备");
                    dataBinding.tvStatus.setText("当前状态：未搜索到目标设备 请打开设备之后重试");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });

        }

        @Override
        public void startConnDevice(BleDevice bleDevice) { // 开始连接
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "开始连接");
                    dataBinding.tvStatus.setText("当前状态：开始连接");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void startConnNoSupport() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：开始连接 不支持连接");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void connSuccesDevice(BleDevice bleDevice) { // 连接成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "连接成功", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接成功");
                    dataBinding.tvStatus.setText("当前状态：连接成功 正准备获取数据");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void getDeviceDataOriginal(final String scanDeviceData) { // 拿取到原始的数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据（原始）");
                    dataBinding.tvOriginal.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void checkDataIsFailure(String mac, String devID, String calDevID, String data) { // 校验广播信息失败 devID 与mac不匹配
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据Mac与DevID校验失败");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    // 拼接校验结果和数据信息
                    StringBuilder checkData = new StringBuilder();
                    checkData
                            .append("mac: " + mac + "\n")
                            .append("devId: " + devID + "\n")
                            .append("cal devId: " + calDevID + "\n")
                            .append("all Data: " + data + "\n")
                            ;
                    dataBinding.tvCheckData.setText(checkData.toString());
                }
            });
        }

        @Override
        public void getDeviceData(final String scanDeviceData) {// 拿取到数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙广播数据(已替换数据)");
                    dataBinding.tvServer.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
//                    mServerData = scanDeviceData; // 不做上传
//                    mHandler.sendEmptyMessage(HANDLER_SEND_SERVER);
                }
            });

        }

        @Override
        public void getConnDeviceData(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "获取到蓝牙原始数据");
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的数据(已连接设备)");
                    dataBinding.tvServer.setText(scanDeviceData);
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
//                    mServerData = scanDeviceData;
//                    mHandler.sendEmptyMessage(HANDLER_SEND_SERVER);
                }
            });
        }

        @Override
        public void getNotifyConnDeviceSuccess(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的打开通知成功");
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void getNotifyConnDeviceFail(String scanDeviceData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的打开通知失败~~~~~~~");
                    dataBinding.tvCheckData.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        private StringBuilder dataText = new StringBuilder();

        private StringBuilder dataText1 = new StringBuilder();

        @Override
        public void getNotifyConnDeviceData(String scanDeviceData) { // TODO FastBle 的监听
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：获取到蓝牙连接之后的通知成功获取数据信息");
                    dataText.append(scanDeviceData + "\n");
                    dataBinding.tvCheckData.setText(dataText.toString());
                    dataBinding.tvServer.setText(scanDeviceData);
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    if (scanDeviceData.startsWith("FF") || scanDeviceData.startsWith("ff")){
                        dataText1 = new StringBuilder();
                        dataText1.append(scanDeviceData);
                    } else if (scanDeviceData.endsWith("9c") || scanDeviceData.endsWith("9C")){
                        dataText1.append(scanDeviceData);
                    }
                    Log.e("ooooooooooo","data = " + dataText1.toString());
                    if(dataText1.toString().length() > 40/* && !mIsParseSuccess*/) {
                        // TODO 开启线程解析数据
                        parseData(dataText1.toString());
                        dataText1 = new StringBuilder();
                    }
                }
            });
        }

        @Override
        public void connFailedDevice(BleDevice bleDevice) { // 连接失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "连接失败", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接失败");
                    dataBinding.tvStatus.setText("当前状态：连接失败");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void disConnDevice(BleDevice bleDevice) { // 断开连接
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsParseSuccess = false;
                    Toast.makeText(mContext, "断开连接", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "断开连接");
                    dataBinding.tvStatus.setText("当前状态：设备 断开连接");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void connNotDesDevice(BleDevice bleDevice) { // 断开连接 连接的设备不是我需要的数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsParseSuccess = false;
                    Toast.makeText(mContext, "断开连接 连接的设备不是我需要的数据", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "连接的设备不是我需要的数据");
                    dataBinding.tvStatus.setText("当前状态：设备 断开连接 连接的设备不是我需要的数据");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                }
            });
        }

        @Override
        public void replyDataToDeviceSuccess(String data) {  // 回复硬件蓝牙成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataBinding.tvStatus.setText("当前状态：回复设备（" + data +" ）成功");
                    isStop = true;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    dataBinding.tvReplyDev.setText(data + "------回复成功");
                    dataText = new StringBuilder();
                    dataText.append("");
                    dataBinding.tvCheckData.setText(dataText.toString());
                }
            });
        }

        @Override
        public void replyDataToDeviceFailed(String data) {  // 回复硬件蓝牙失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsParseSuccess = false;
                    dataBinding.tvStatus.setText("当前状态：回复设备（" + data +" ）失败----呜呜呜");
                    isStop = false;
                    String mLocation = "蓝牙插件定位信息\n经度："+ Constants.mLatitude +"\n纬度："+ Constants.mLongitude;
                    dataBinding.tvLocation.setText(mLocation);
                    dataBinding.tvReplyDev.setText(data + "------回复失败");
                }
            });
        }
    };

    // 8F EB 0B 8E 68 DB 0E B0 6F 80 04 00 77 17 E2 25 80 23 29 9C
    // 8F 0B 8E 68 DB 0E B0 6F 80 04 00 77 17 E2 25 80 23 29 9C
    byte[] version_data = new byte[]{
            (byte) 0x8F,(byte) 0xEB,(byte) 0xEF,(byte) 0x60,(byte) 0x68,
            (byte) 0xDB,(byte) 0x0E,(byte) 0xB0,(byte) 0x6F,(byte) 0x80,
            (byte) 0x04,(byte) 0x00,(byte) 0x77,(byte) 0x17,(byte) 0xE2,
            (byte) 0x25,(byte) 0x80,(byte) 0x23,(byte) 0x02,(byte) 0x9C};

    byte[] reply_data = new byte[]{(byte)0x8E,(byte)0x9C};
    byte reply_data1 = (byte) 0x02;

    private boolean mIsParse = false;
    private boolean mIsParseSuccess = false;
    private void parseData(final String datas) {
        if (!mIsParse) {
            mIsParse = true;
            String data = datas.toUpperCase();
            String content = ""; // 装所有数据的字符串
            // 判断当前数据有头有尾
            if (data.contains("FF") && data.contains("9C")) {
                // 开始截取头部之后的数据 判断是否是以FF 或者 ff 开始
                if (data.startsWith("FF")) {
                    content = data;
                } else {
                    // 需要进行截取
                    String[] splitFF = data.split("FF");
                    if (splitFF.length > 1) {
                        content = "FF" + splitFF[1];
                    } else {
                        content = "";
                    }
                }
                // 开始截取尾部之前的位置 判断是否是以9C 或者 9c 结尾
                if (content.endsWith("9C")) {
                } else {
                    // 需要进行截取
                    String[] split9C = content.split("9C");
                    if (split9C.length > 0) {
                        content = split9C[0] + "9C";
                    } else {
                        content = "";
                    }
                }
            }
            // 上面的数据表示 58 表示数据是全的
            if (content.startsWith("FF") && content.endsWith("9C") && content.length() == 60) {
                NotifyBLEDataConstructerBean bean = new NotifyBLEDataConstructerBean();
                bean.setBaotou(content.substring(0, 2));
                bean.setKehudaima(content.substring(2, 4));
                bean.setShujubaoType(content.substring(4, 6));
                String ipandport = content.substring(6, 18);
                bean.setIpAndPort(ipandport);
                String devId = content.substring(18, 22);
                bean.setDevId(devId);
                bean.setPower(content.substring(22, 24));
                bean.setLatlng(content.substring(24, 40));
                bean.setLatlngType(content.substring(40, 42));
                bean.setWeixingnum(content.substring(42, 44));
                String macStr = content.substring(44, 56);
                bean.setMac(macStr);
                bean.setVersion(content.substring(56, 58));
                bean.setBaowei(content.substring(58, 60));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoadFail("数据解析成功：" + bean.toString() + "准备发送关闭命令");
                    }
                });
                if (bean.checkMacAndDevId(macStr, devId)) {
                    // TODO 判断设备当前是否未激活
                    if (bean.getShujubaoType().equals("04")) {
                        StringBuilder devIds = new StringBuilder()
                                .append(macStr.substring(7, 8))
                                .append(macStr.substring(3, 4))
                                .append(macStr.substring(10, 11))
                                .append(macStr.substring(5, 6));
                        StringBuilder dataBle = new StringBuilder();
                        dataBle.append("8D"); // 0x8F/0x8D/0x8C	用于标明配置包/通知终端激活/休眠
                        dataBle.append(bean.getKehudaima()); // 客户代码
                        dataBle.append(devIds.toString()); // DevId
                        dataBle.append(macStr); // 设备mac地址
                        dataBle.append("03");//停止事件的判断时间	1 Byte 0x03 停止运动超过设置时间，则判断事件有效，开启GPS。单位：分钟，0A代表10分钟。默认3分钟。
                        dataBle.append("05");//终端休眠	1 Byte	0x05	禁用4G，GPS的小时数；默认0小时；单位小时，05代表5小时。
                        dataBle.append(ipandport);//IP在前，设备4G上报的IP和端口。
                        dataBle.append("02");//用于标注配置的版本号，设备应保存。
                        dataBle.append("9C");//结束字符
                        String dataSend = "8D0000000000000000000000000000000000009C";
                        BleNFCManager.getInstance().sendOffLine(hexStrToByteArray(dataSend));
                    } else {
                        // TODO 判断设备版本与服务器版本是否一致
                        if (Byte.parseByte(bean.version, 16) == reply_data1) {
                            // 一致 返回8E 9C
                            BleNFCManager.getInstance().sendOffLine(reply_data);
                        } else {
                            // 8F EB EF60 68DB0EB06F80 04 00 7717E2258023 02 9C
                            StringBuilder devIds = new StringBuilder()
                                    .append(macStr.substring(7, 8))
                                    .append(macStr.substring(3, 4))
                                    .append(macStr.substring(10, 11))
                                    .append(macStr.substring(5, 6));
                            StringBuilder dataBle = new StringBuilder();
                            dataBle.append("8F"); // 0x8F/0x8D/0x8C	用于标明配置包/通知终端激活/休眠
                            dataBle.append("EB"); // 客户代码
                            dataBle.append(devIds.toString()); // DevId
                            dataBle.append(macStr); // 设备mac地址
                            dataBle.append("03");//停止事件的判断时间	1 Byte 0x03 停止运动超过设置时间，则判断事件有效，开启GPS。单位：分钟，0A代表10分钟。默认3分钟。
                            dataBle.append("05");//终端休眠	1 Byte	0x05	禁用4G，GPS的小时数；默认0小时；单位小时，05代表5小时。
                            dataBle.append(ipandport);//IP在前，设备4G上报的IP和端口。
                            dataBle.append("02");//用于标注配置的版本号，设备应保存。
                            dataBle.append("9C");//结束字符
                            BleNFCManager.getInstance().sendOffLine(hexStrToByteArray(dataBle.toString()));
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoadFail("设备验证失败，可能是因为大小端的问题 mac = " + macStr + " dev = " + devId);
                        }
                    });
                }
                mIsParseSuccess = true;
            } else {
                String showContent = content;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoadFail("数据不全 解析之后的数据：" + showContent + "\n 解析之前的数据：" + datas);
                    }
                });
                mIsParseSuccess = false;
            }
            mIsParse = false;
        }
    }

    public static byte[] hexStrToByteArray(String data) {
        if (TextUtils.isEmpty(data)) {
            return  null;
        }
        byte[] bytes = new byte[data.length() / 2];
        for (int i = 0; i < bytes.length; i++){
            String subStr = data.substring(2*i,2*i+2);
            bytes[i] = (byte) Integer.parseInt(subStr,16);
        }
        return bytes;
    }

    public String IP_SOCKET = "119.23.226.237";
    public int PORT_SOCKET = 9088;

    /**
     * 将ip地址和端口信息进行转为16进制的字符串
     * @return
     */
    public static String getIpAndPortToHexStr(String IP, int port){
        StringBuilder data = new StringBuilder();
        String[] ips = IP.split(".");
        for(String item : ips){
            data.append(Integer.valueOf(item,16));
        }
        data.append(Integer.toString(port,16));
        return data.toString();
    }

    private void updateServerData(String data){
        if (data.isEmpty()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showText(mContext,"获取服务器的数据为空");
                }
            });
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                upService(data);
            }
        }).start();
    }

    /**
     * 上报服务器
     * @param data
     */
    private void upService(String data) {  // TODO 119.23.226.237：9088 使用UDP发送数据信息
        if (TextUtils.isEmpty(data)){
            return;
        }
        if (udpThread == null) {
            return;
        }
        udpThread.sendSocketData(data);
    }

    private String mUDPStatusStr = "";
    /**
     * UDP服务器的监听
     */
    private SocketListener mSockestListener = new SocketListener() {
        @Override
        public void receiveSocketData(String socketData) {
            // TODO 接收到服务端的数据
            mUDPStatusStr = "接收到服务端信息：" +socketData;
            mHandler.sendEmptyMessage(HANDLER_SEND_SERVER_UDP_STATUS);
        }

        @Override
        public void sendSocketData(String packs) {
            // TODO 已发送数据
            mUDPStatusStr = "已经发送到服务端信息：" +packs;
            mHandler.sendEmptyMessage(HANDLER_SEND_SERVER_UDP_STATUS);
        }

        @Override
        public void error(Throwable e) {
            // TODO 收发数据出现异常
            mUDPStatusStr = "接收出现异常：" +e.toString();
            mHandler.sendEmptyMessage(HANDLER_SEND_SERVER_UDP_STATUS);
        }
    };


    private NfcView mNFCView = new NfcView() {
        @Override
        public void appendResponse(final String response) {
            Log.e(TAG, "appendResponse: data______________________________" + response);
            // TODO NFC相关信息的回调事件
            if (TextUtils.isEmpty(response)){
                return;
            }
            mIsRequestNFCUid = true; // 从线程中读取到NFC的相关数据
            Vibrator vibrator = (Vibrator) MainActivity.this.getSystemService(MainActivity.this.VIBRATOR_SERVICE);
            vibrator.vibrate(1000); // 获取成功只有震动1秒的钟
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showText(MainActivity.this,response);
                    StringBuilder data = new StringBuilder();
                    data.append("NFC相关信息：").append(response);
                    dataBinding.tvNfc.setText(data.toString());
                }
            });
//        mRevDataEt.append(response);
            // TODO NFC的相关信息的显示之后需要上传服务器
        }

        @Override
        public void notNfcDevice() {
            Toast.makeText(mContext, "未找到NFC设备！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void notOpenNFC() {
            mIsOpenNFC = false;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW_NFC);
            Toast.makeText(mContext, "请在设置中打开NFC开关！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void getNFCStatusOk() {
            mIsOpenNFC = true;
            mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW_NFC);
        }
    };

    // 开始查看NFC是否被读取
    private void getNFCInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsRequestNFCUid) {
                    try {
                        Thread.sleep(1000);
                        // 循环读取数据
                        mNfcHandler.readCardId(getIntent());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //处理界面的初始值的状态
    @Override
    protected void onStart() {
        super.onStart();
    }

    // 处理界面的图标显示问题
    @Override
    protected void onResume() {
        super.onResume();
        mIsActiity = true;
        mHandler.sendEmptyMessage(HANDLER_INIT_IMAGEVIEW);

        // 开始使用NFC
        mNfcHandler.enableNfc(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mNfcHandler.disableNfc(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 保持前台进程 离开界面就开始服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, KeepAppLifeService.class));
        } else {
            startService(new Intent(this,KeepAppLifeService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsActiity = false;
        BleNFCManager.getInstance().destroyBlueToothPlugin(); // TODO
//        BleDeviceManager.getInstance().onStopBlueToothPlugin();  // TODO
        stopTimer();
        setStopGPSService();

        // NFC关闭
        mNfcHandler.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) { // TODO nfc必须要使用的
        Log.d(TAG, "onNewIntent()! action is:" + intent.getAction());
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
