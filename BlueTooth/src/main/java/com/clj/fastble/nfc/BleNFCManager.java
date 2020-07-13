package com.clj.fastble.nfc;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.libs.config.Constants;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import static com.clj.fastble.libs.config.Constants.UUID_SERVICE_ALL;
import static com.clj.fastble.libs.config.Constants.UUID_SERVICE_READ;
import static com.clj.fastble.libs.config.Constants.UUID_SERVICE_WRITE;

public class BleNFCManager {
    // 单例模式
    private BleNFCManager() {

    }

    public static synchronized BleNFCManager getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder { // 静态内部类进行初始化当前单例
        private static BleNFCManager instance = new BleNFCManager(); // 类加载机制会在对象初始化的时候加锁  使不会进行重排序的情况
    }

    private LocationManager locationManager = null;
    String mProviderName = "";
    private Context mContext = null;

    public void initBleNFC(Application context, Activity activity, BleNFCListener listener) {
        // 蓝牙插件的监听事件
        mBlueToothListener = listener;
        // 上下文事件
        mContext = context;
        // 判断GPS权限问题
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mBlueToothListener.initFailed((byte) 0x0001); // 位置权限未打开
            return;
        }
        // 蓝牙是否打开
        if (!BleIsOpen()) { // 蓝牙未打开
            mBlueToothListener.initFailed((byte) 0x0010);
            return;
        }
        // 初始化控件
        BleManager.getInstance().init(context);
        // 设置扫描设备配置
        BleManager.getInstance()
                .enableLog(true) // 是否需要log
                .setReConnectCount(1, 5000) //重连次数一次 每隔5秒重连一次
                .setConnectOverTime(20000) // 连接超时的时间设置
                .setOperateTimeout(5000); // 操作超时的时间设置
        // 设置默认RSSI值 TODO 目前没有说需要控制RSSI值
//        setMinRssi(Constants.mMinRssi);
        // 启动 GPS 定位信息相关信息
        setLocationInfo(context);
        // 插件初始化成功
        mBlueToothListener.initSuccess();
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

    // 插件监听的实例化
    private BleNFCListener mBlueToothListener;

    public void getBleNFCInfo(){
        setScanRule();
        startScan();
    }

    /**
     * TODO 由于需要后台运行 需要考虑放到Stop的生命周期 还是Destroy的生命周期中
     */
    public void onStopBlueToothPlugin(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * 设置扫描规则 TODO 需要先设置规则 才可以正式开始扫描连接设备
     */
    private void setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)      // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    /**
     * TODO 是否扫描到可用设备
     */
    private boolean mIsScanDes = false;

    /**
     * 开始扫描
     */
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onScanning(BleDevice bleDevice) {
                if (Constants.mBleName.equals(bleDevice.getName())){ // TODO 判断是否是我们需要的设备名称的设备
                    if (!BleManager.getInstance().isConnected(bleDevice)) { // 判断设备名称是正常的设备是否已经被连接
                        mIsScanDes = true;
                        mBlueToothListener.scanDevice(); // 已经扫描到一个可用设备
                        BleManager.getInstance().cancelScan(); // 已经找到可以连接的设备 停止扫描设备
                        connect(bleDevice); // 连接当前设备
                    }
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if (!mIsScanDes){
                    mBlueToothListener.scanNotDevice(); // 提示监听 没有找到设备
                } else {
                    mIsScanDes = false;
                }
            }
        });
    }


//    private UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");//服务UUID
//    private UUID WRITE_CHARACTERISTIC = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");//写UUID
//    private UUID READ_CHARACTERISTIC = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");//读UUID

    private BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic writeCharacteristic;

    private String TAG = BleNFCManager.class.getSimpleName();

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
            //获取ble设备上面的服务
            gatt.discoverServices();
            mBlueToothListener.connSuccesDevice(null);
        }

        /**
         * 当前设备无法连接
         */
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.e(TAG, "蓝牙设备连接失败");
            mBlueToothListener.connFailedDevice(null);
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
            mBlueToothListener.startConnDevice(null); // 开始连接设备
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT_SUCCESS");
                connectionStateChange(gatt, newState);
            } else if (status == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
                Log.e(TAG, "不支持");
                mBlueToothListener.startConnNoSupport();
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
                    mBlueToothListener.connSuccesDevice(null);
                } else {
                    Log.e(TAG, "蓝牙设备已断开连接");
                    mBlueToothListener.disConnDevice(null);
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
            if (BluetoothGatt.GATT_SUCCESS == status && UUID_SERVICE_WRITE.equals(characteristic.getUuid())) {
                Log.e(TAG, "write onCharacteristicWrite GATT_SUCCESS---" + status);
            } else if (BluetoothGatt.GATT_FAILURE == status && UUID_SERVICE_WRITE.equals(characteristic.getUuid())) {
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
            //设备传输的数据包
            byte[] packData = characteristic.getValue();
            mBlueToothListener.getNotifyConnDeviceSuccess(HexUtil.encodeHexStr(packData));
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connect(final BleDevice bleDevice) {
//        BluetoothDevice bluetoothDevice = bleDevice.getDevice();
//        //连接蓝牙设备
//        bluetoothGatt = bluetoothDevice.connectGatt(mContext, false, bluetoothGattCallback);

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                mBlueToothListener.startConnDevice((BleDevice) bleDevice); // 开始连接设备
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                mBlueToothListener.connFailedDevice((BleDevice) bleDevice); // 设备连接失败
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onConnectSuccess(final BleDevice bleDevice, final BluetoothGatt gatt, int status) {
                mBlueToothListener.connSuccesDevice((BleDevice) bleDevice); // 成功连接设备  准备验证数据
                // 需要打开notify 准备接收数据

                BleManager.getInstance().indicate(bleDevice, UUID_SERVICE_READ,UUID_SERVICE_ALL, new BleIndicateCallback() {
                    @Override
                    public void onIndicateSuccess() {
                        mBlueToothListener.getNotifyConnDeviceSuccess("打开Indicate成功");
                    }

                    @Override
                    public void onIndicateFailure(BleException exception) {
                        mBlueToothListener.getNotifyConnDeviceFail("打开Indicate失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        mBlueToothListener.getNotifyConnDeviceData(HexUtil.encodeHexStr(data));
                    }
                });
            }
            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mBlueToothListener.disConnDevice((BleDevice) bleDevice); // 断开连接
            }
        });
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
        BluetoothGattService service = gatt.getService(UUID.fromString(UUID_SERVICE_ALL));
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
                            Log.e(BleNFCManager.class.getSimpleName(), "enableNotification: " + writeType);
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
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 && UUID_SERVICE_READ.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
            //用于通讯的UUID character
            if (c.getUuid().equals(UUID_SERVICE_WRITE)) {
                writeCharacteristic = c;
            }
        }
        if (characteristic != null) {
            return characteristic;
        }
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 && UUID_SERVICE_READ.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        return characteristic;
    }

    public void setCloseConn(BleDevice bleDevice){
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

    private void setWriteData(BleDevice bleDevice,byte datas){
        final byte[] data = {datas};
        BleManager.getInstance().write(
                bleDevice,
                UUID_SERVICE_WRITE,
                UUID_SERVICE_ALL,
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        mBlueToothListener.replyDataToDeviceSuccess(new String(data));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                        mBlueToothListener.replyDataToDeviceFailed(new String(data));
                    }
                });
    }

    // 清除掉我们打开的蓝牙设备
    public void destroyBlueToothPlugin(){
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
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
}
