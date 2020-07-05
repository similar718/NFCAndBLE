package com.clj.fastble.libs;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.libs.config.Constants;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.util.List;

import androidx.core.app.ActivityCompat;

public class BlePluginManager {
    String mData = "0201060502c0ffe0ff12ff";

    // 单例模式
    private BlePluginManager() {

    }

    public static synchronized BlePluginManager getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {
        private static BlePluginManager instance = new BlePluginManager();
    }

    private LocationManager locationManager = null;
    String mProviderName = "";
    private Context mContext = null;

    public void initBlueToothPlugin(Application context, Activity activity, BlueToothPluginListener listener) {
        // 蓝牙插件的监听事件
        mBlueToothListener = listener;
        mContext = context;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mBlueToothListener.initFailed((byte) 0x0001); // 位置权限未打开
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) { // 蓝牙未打开
            mBlueToothListener.initFailed((byte) 0x0010);
            return;
        }
        // 初始化控件
        BleManager.getInstance().init(context);
        // 设置扫描设备配置
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        // 设置默认RSSI值
        setMinRssi(Constants.mMinRssi);
        // 定位信息相关信息
        setLocationInfo(context);
        mBlueToothListener.initSuccess();
    }

    private void setLocationInfo(Application activity) {
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) activity.getSystemService(serviceName);
        // 查找到服务信息
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置是否需要方位信息connNotDesDevice
        criteria.setBearingRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        // 为获取地理位置信息时设置查询条件
        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息

        Location lastKnownLocation = null;
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mProviderName = LocationManager.GPS_PROVIDER;
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mProviderName = LocationManager.NETWORK_PROVIDER;
        }
        if (mProviderName != null && !"".equals(mProviderName)) {
            locationManager.requestLocationUpdates(mProviderName, 1000, 1, locationListener);
        }
    }

    public void setMinRssi(long minRssi){
        Constants.mMinRssi = minRssi;
    }

    public long getMinRssi(){
        return Constants.mMinRssi;
    }

    private BlueToothPluginListener mBlueToothListener;

    public void getDeviceInfo(){
        setScanRule();
        startScan();
    }

    public void onPauseBlueToothPlugin(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private boolean mIsScanDes = false;

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if ("BLE_KEY".equals(bleDevice.getName())){
                    if (!BleManager.getInstance().isConnected(bleDevice)) {
                        if (bleDevice.getRssi() >= getMinRssi()){ // 在规定的rssi范围内进行连接
                            mIsScanDes = true;
                            mBlueToothListener.scanDevice();
                            BleManager.getInstance().cancelScan();
                            connect(bleDevice);
                        } else { // 未在rssi的范围内不进行操作
                            mBlueToothListener.scanDeviceMinRSSI();
                        }
                    }
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if (!mIsScanDes){
                    mBlueToothListener.scanNotDevice();
                } else {
                    mIsScanDes = false;
                }
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                mBlueToothListener.startConnDevice((BleDevice) bleDevice);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                mBlueToothListener.connFailedDevice((BleDevice) bleDevice);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mBlueToothListener.connSuccesDevice((BleDevice) bleDevice);
                String scancord =  HexUtil.formatHexString(bleDevice.getScanRecord());
                // 成功获取到数据进行判断当前数据的是否是我要的格式
                // 广播指示 02 01 06  数据固定 2 bytes
                // UUID 05 02 c0 ff e0 ff 数据固定 6 bytes
                /**
                 *  自定义数据
                 *      长度 12 数据固定 1 bytes
                 *      类型 FF 数据固定 1 bytes
                 *      DevID 0D 00 数据固定 2 bytes
                 *      电量 2 bytes
                 *      芯片温度 2 bytes
                 *      按键次数 4 bytes
                 *      mac地址 6bytes
                 *      发射功率 c5 数据固定 1bytes
                 */
                // 02 01 06 05 02 c0 ff e0 ff 12 ff 0d 00 61 03 36 00 02 00 00 00 15 12 29 f8 e6 a0 c5 08 09 42 4c 45 5f 4b 45 59 05 12 0a 00 14 00 02 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
                if (bleDevice.getRssi() < getMinRssi()){ // TODO 只要获取的RSSI的值大于当前设置的RSSI的值就可以正常的操作蓝牙设备
                    // 当前所获取的信息不符合 断开连接
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
                        setCloseConn(bleDevice);
                        gatt.disconnect();
                        gatt.close();
                        mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
                    }
                    return;
                }

                String datas = scancord.substring(0, mData.length());
                if (mData.equals(datas)){
                    // TODO 判断获取的DevID与mac的处理情况
                    String devId_b4 = scancord.substring(22,24);
                    String devId_b1 = scancord.substring(24,26);
                    String scanMac = scancord.substring(42,54);
                    String mac_b1 = scanMac.substring(4,6);
                    String mac_b4 = scanMac.substring(6,8);

                    if (scanMac.contains("000000000000")) {
                        setOptionsData(scancord,bleDevice.getRssi());
                        // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
                        setWriteData(bleDevice);
                    } else if (devId_b1.equals(mac_b1) && devId_b4.equals(mac_b4)){
                        setOptionsData(scancord,bleDevice.getRssi());
                        // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
                        setWriteData(bleDevice);
                    } else {
                        // 当前所获取的信息不符合 断开连接
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
                            setCloseConn(bleDevice);
                            gatt.disconnect();
                            gatt.close();
                            mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
                        }
                    }
                } else {
                    // 当前所获取的信息不符合 断开连接
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
                        setCloseConn(bleDevice);
                        gatt.disconnect();
                        gatt.close();
                        mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
                    }
                }
            }
            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mBlueToothListener.disConnDevice((BleDevice) bleDevice);
            }
        });
    }

    private void setReadData(final BleDevice bleDevice){
        BleManager.getInstance().read(
                bleDevice,
                uuid_service,
                characteristic_uuid,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        // 读特征值数据成功
                        Log.e("ooooooooo","data = " + HexUtil.encodeHexStr(data));
                        String scancord =  HexUtil.formatHexString(bleDevice.getScanRecord());
                        // 成功获取到数据进行判断当前数据的是否是我要的格式
                        // 广播指示 02 01 06  数据固定 2 bytes
                        // UUID 05 02 c0 ff e0 ff 数据固定 6 bytes
                        /**
                         *  自定义数据
                         *      长度 12 数据固定 1 bytes
                         *      类型 FF 数据固定 1 bytes
                         *      DevID 0D 00 数据固定 2 bytes
                         *      电量 2 bytes
                         *      芯片温度 2 bytes
                         *      按键次数 4 bytes
                         *      mac地址 6bytes
                         *      发射功率 c5 数据固定 1bytes
                         */
                        // 02 01 06 05 02 c0 ff e0 ff 12 ff 0d 00 61 03 36 00 02 00 00 00 15 12 29 f8 e6 a0 c5 08 09 42 4c 45 5f 4b 45 59 05 12 0a 00 14 00 02 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00

                        String datas = scancord.substring(0, mData.length());
                        if (mData.equals(datas)){
                            setOptionsData(scancord,bleDevice.getRssi());
                            // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
                            setWriteData(bleDevice);
                        }
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        // 读特征值数据失败
                    }
                });
    }

    public void setCloseConn(BleDevice bleDevice){
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

    private void setOptionsData(String data,int rssi){
        BleDeviceInfo info = new BleDeviceInfo();
        info.setBroadcast(data.substring(0,6));
        info.setUuid(data.substring(6,18));
        info.setLength(data.substring(18,20));
        info.setType(data.substring(20,22));
        info.setDevId(data.substring(22,26));
        // 电量
        String powerstr = data.substring(26,30);
        if (!"".equals(powerstr) && null != powerstr){
            String start = powerstr.substring(0,2);
            String end = powerstr.substring(2,4);
            String datainfo = Integer.parseInt(end)+"."+start+"V";
            info.setPower(datainfo);
            double power = Double.parseDouble(Integer.parseInt(end)+"."+start);
            if (power < Constants.mWarningPower){
                mBlueToothListener.warningPower();
            }
        }
        // 芯片温度
        String Tempstr = data.substring(30,34);
        if (!"".equals(Tempstr) && null != Tempstr){
            String start = Tempstr.substring(0,2);
            String end = Tempstr.substring(2,4);
            String datainfo = start+"."+end+"℃";
            info.setTemperature(datainfo);
        }
        // 按键次数
        String numstr = data.substring(34,42);
        String numstr4 = data.substring(34,36);
        String numstr3 = data.substring(36,38);
        String numstr2 = data.substring(38,40);
        String numstr1 = data.substring(40,42);
        // 小端格式
        String num = numstr1+numstr2+numstr3+numstr4;// 转换
        int tapnum = Integer.parseInt(num,16);
        info.setNum(String.valueOf(tapnum));
        if (tapnum > Constants.mTapNum){
            mBlueToothListener.warningTapNum();
        }

        // mac 地址
        String mac6 = data.substring(42,44);
        String mac5 = data.substring(44,46);
        String mac4 = data.substring(46,48);
        String mac3 = data.substring(48,50);
        String mac2 = data.substring(50,52);
        String mac1 = data.substring(52,54);
        String mac = mac1+mac2+mac3+mac4+mac5+mac6;
        info.setMac(data.substring(42,54));
        info.setSend(data.substring(54,56));
        info.setOther(data.substring(56,data.length()));
        info.setRssi(rssi);
        mBlueToothListener.getDeviceInfo(info);
    }

    String uuid_service = "0000FFE0-0000-1000-8000-00805F9B34FB";
    String characteristic_uuid = "0000FFF1-0000-1000-8000-00805F9B34FB";
    private void setWriteData(BleDevice bleDevice){
        byte[] data = {0x01};
        BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                characteristic_uuid,
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        mBlueToothListener.replyDataToDeviceSuccess();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                        mBlueToothListener.replyDataToDeviceFailed();
                    }
                });
    }

    public void destroyBlueToothPlugin(){
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            Constants.mLatitude = location.getLatitude();
            Constants.mLongitude = location.getLongitude();
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    // 当前GPS状态为可见状态
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    // 当前GPS状态为服务区外状态
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    // 当前GPS状态为暂停服务状态
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            if (
                    ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(mContext,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            Constants.mLatitude = location.getLatitude();
            Constants.mLongitude = location.getLongitude();
        }
        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
        }
    };
}
