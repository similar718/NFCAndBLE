package com.nfc.cn.ble;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.clc.baselibs.base.BaseApplication;
import com.clj.fastble.libs.config.Constants;
import com.clj.fastble.utils.HexUtil;

import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class BleDevice {
    private final String TAG = BleDevice.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothManager bluetoothManager;
    private ScanCallback scanCallback;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");//服务UUID
    private UUID READ_CHARACTERISTIC = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");//读UUID
    private UUID WRITE_CHARACTERISTIC = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");//写UUID

    private String BleName = "MS360";

    private static BleDevice bleDevice;

    public static BleDevice getInstance() {
        if (bleDevice == null) {
            synchronized (BleDevice.class){
                if (bleDevice == null) {
                    bleDevice = new BleDevice();
                }
            }
        }
        return bleDevice;
    }

    private BleDevice() {
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        bluetoothManager = (BluetoothManager) BaseApplication.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    private ScanConnectDeviceCallback mCallBack = null;

    private LocationManager locationManager = null;
    String mProviderName = "";
    private Context mContext = null;

    public void initBleNFC(Application context, Activity activity, ScanConnectDeviceCallback listener) {
        // 手机是否支持蓝牙
        if (!isSupportBle()){
            mCallBack.initFailed((byte) 0x0011); // 不支持蓝牙
            return;
        }
        // 蓝牙插件的监听事件
        mCallBack = listener;
        // 上下文事件
        mContext = context;
        // 判断GPS权限问题
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mCallBack.initFailed((byte) 0x0001); // 位置权限未打开
            return;
        }
        // 蓝牙是否打开
        if (!BleIsOpen()) { // 蓝牙未打开
            mCallBack.initFailed((byte) 0x0010);
            return;
        }
        // 启动 GPS 定位信息相关信息
        setLocationInfo(context);
        // 插件初始化成功
        mCallBack.initSuccess();
    }

    /**
     * is support ble?
     *
     * @return
     */
    public boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    // 扫描最长时长
    private final int maxTimeOut = 10 * 1000;

    /**
     * 扫描蓝牙设备
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scanDevice() {
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice bluetoothDevice = result.getDevice();
                //获取搜索到的蓝牙设备的mac地址
                String mac = bluetoothDevice.getAddress();
                //获取搜索到的蓝牙设备的蓝牙名称
                String name = bluetoothDevice.getName();

                if (name != null && name.contains(BleName)) { // 名称不为空 并且名称中带有”MS360“的文字 搜索到设备  开始进行连接设备
                    // TODO 需要进行判断当前设备是否已经连接
                    if (bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTED) {
                        // TODO 找到目标设备 准备开始连接
                        mCallBack.scanDevice();
                        // 表示当前设备没有进行连接 TODO 开始连接蓝牙设备
                        connectDevice(mac);
                        // TODO 停止扫描的动作
                        bluetoothLeScanner.stopScan(scanCallback);//停止扫描
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                // TODO 没有找到目标设备
                mCallBack.scanNotDevice();
            }
        };

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        bluetoothLeScanner.startScan(scanCallback);//开始扫描蓝牙设备

        //3秒后停止扫描，并返回扫描结果list
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(scanCallback);//停止扫描
                mCallBack.scanNotDevice();
            }
        }, maxTimeOut);
    }


    /**
     * 连接蓝牙设备,通过设备的mac连接
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connectDevice(String mac) {
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
        //连接蓝牙设备 TODO 开始连接当前扫描到的蓝牙设备
        bluetoothGatt = bluetoothDevice.connectGatt(BaseApplication.getInstance(), false, bluetoothGattCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disConnectDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    /**
     * 连接蓝牙设备结果回调
     */
    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 连接设备
         * @param gatt
         * @param status
         * @param newState
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            // 开始连接
            mCallBack.startConnDevice();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT_SUCCESS");
                connectionStateChange(gatt, newState);
            } else if (status == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
                Log.e(TAG, "不支持");
                // TODO 不支持
                mCallBack.startConnNoSupport();
            }

        }

        /**
         * 发现设备服务
         * @param gatt
         * @param status
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean flag = enableNotification(gatt);
                if (flag) {
                    Log.e(TAG, "蓝牙设备已连接");
                    // TODO 设备连接成功
                    mCallBack.connSuccesDevice();
                } else {
                    Log.e(TAG, "蓝牙设备已断开连接");
                    // TODO 设备断开连接
                    mCallBack.disConnDevice();
                }
            }
        }

        /**
         * 写数据
         * @param gatt
         * @param characteristic
         * @param status
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //写数据成功
            if (BluetoothGatt.GATT_SUCCESS == status && WRITE_CHARACTERISTIC.equals(characteristic.getUuid())) {
                Log.e(TAG, "write onCharacteristicWrite GATT_SUCCESS---" + status);
            } else if (BluetoothGatt.GATT_FAILURE == status && WRITE_CHARACTERISTIC.equals(characteristic.getUuid())) {
                Log.e(TAG, "write onCharacteristicWrite GATT_FAILURE" + status);
            }
        }

        /**
         * 通知数据，往设备写入数据之后，接收设备返回的数据
         * @param gatt
         * @param characteristic
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG, " onCharacteristicChanged ");
            // TODO 获取到设备发送过来的数据
            //设备传输的数据包
            byte[] packData = characteristic.getValue();
            // TODO 获取到notify信息
            mCallBack.getNotifyConnDeviceData(HexUtil.encodeHexStr(packData));
        }
    };

    /**
     * 处理蓝牙设备连接 操作
     *
     * @param gatt
     * @param newState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectionStateChange(final BluetoothGatt gatt, int newState) {
        /**
         * 当前蓝牙设备已经连接
         */
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG, "蓝牙设备连接成功");
            // TODO 设备连接成功
            mCallBack.connSuccesDevice();
            //获取ble设备上面的服务
            gatt.discoverServices();
        }
        /**
         * 当前设备无法连接
         */
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // TODO 设备连接失败
            mCallBack.connFailedDevice();
            Log.e(TAG, "蓝牙设备连接失败");
        }
    }

    /**
     * 订阅蓝牙通知消息，在onCharacteristicChanged()回调中接收蓝牙返回的消息
     *
     * @param gatt
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean enableNotification(BluetoothGatt gatt) {
        boolean success = false;
        BluetoothGattService service = gatt.getService(SERVICE_UUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service);
            if (characteristic != null) {
                success = gatt.setCharacteristicNotification(characteristic, true);
                gatt.readCharacteristic(characteristic);
                if (success) {
                    for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
                        if (dp != null) {
                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            int writeType = characteristic.getWriteType();
                            Log.e(TAG, "enableNotification: " + writeType);
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                            gatt.writeDescriptor(dp);
                            characteristic.setWriteType(writeType);
                        }
                    }
                }
            }
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service) {
        BluetoothGattCharacteristic characteristic = null;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 && READ_CHARACTERISTIC.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
            //用于通讯的UUID character
            if (c.getUuid().equals(WRITE_CHARACTERISTIC)) {
                writeCharacteristic = c;
            }
        }
        if (characteristic != null) {
            return characteristic;
        }
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 && READ_CHARACTERISTIC.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        return characteristic;
    }


    /**
     * 判断当前蓝牙硬件是否已经打开
     * @return
     */
    public boolean BleIsOpen(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
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


    /**
     * 定位信息的相关代码
     */
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


    /**
     * TODO 由于需要后台运行 需要考虑放到Stop的生命周期 还是Destroy的生命周期中
     */
    public void onStopBlueToothPlugin(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

}
