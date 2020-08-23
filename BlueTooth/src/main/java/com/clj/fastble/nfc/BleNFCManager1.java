//package com.clj.fastble.nfc;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.Application;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.clj.fastble.BleManager;
//import com.clj.fastble.callback.BleGattCallback;
//import com.clj.fastble.callback.BleReadCallback;
//import com.clj.fastble.callback.BleScanCallback;
//import com.clj.fastble.callback.BleWriteCallback;
//import com.clj.fastble.data.BleDevice;
//import com.clj.fastble.exception.BleException;
//import com.clj.fastble.libs.config.Constants;
//import com.clj.fastble.scan.BleScanRuleConfig;
//import com.clj.fastble.utils.HexUtil;
//
//import java.util.List;
//import java.util.UUID;
//
//import androidx.annotation.RequiresApi;
//import androidx.core.app.ActivityCompat;
//
//import static com.clj.fastble.libs.config.Constants.UUID_SERVICE_READ;
//import static com.clj.fastble.libs.config.Constants.UUID_SERVICE_WRITE;
//import static com.clj.fastble.libs.config.Constants.characteristic_uuid;
//
//public class BleNFCManager1 {
//    static final String mDataStart = "020106"; // 开始的数据是固定的
//    static final String mDataEnd = "9c"; // 开始的数据是固定的
//    static final String mDataEnd_ = "9C"; // 开始的数据是固定的
//    private static final int DATA_LENGTH_35 = 70; // 定位终端数据包格式 三种 TODO 停止 启动 带有坐标
//    private static final int DATA_LENGTH_25 = 50; // 终端回复数据包格式
//    private static final int DATA_LENGTH_33 = 66; // APP回复终端数据包格式
//    private static final int DATA_LENGTH_7 = 14; // NFC数据包格式（NFC数据格式）
//    private static final int APP_DATA_LENGTH_21 = 42; // APP上报停止/启动事件数据包格式（APP上报停止/启动事件数据格式）
//    private static final int APP_DATA_LENGTH_18 = 36; // APP上报任务期间手持坐标数据包格式
//    private static final int APP_DATA_LENGTH_10 = 20; // APP查询数据包格式（APP上报查询事件数据格式）
//    private static final int APP_DATA_LENGTH_22 = 44; // APP上报车辆人员ID信息数据包格式（APP上报车辆人员ID信息数据格式）
//    private static final int APP_DATA_LENGTH_16 = 32; // APP配对数据包格式（APP上报RFID与设备MAC配对数据格式）
//    private static final int APP_DATA_LENGTH_11 = 22; // APP维修数据包格式（ APP上报维修数据格式）
//
//
//    // 单例模式
//    private BleNFCManager1() {
//
//    }
//
//    public static synchronized BleNFCManager1 getInstance() {
//        return SingletonHolder.instance;
//    }
//
//    private static final class SingletonHolder { // 静态内部类进行初始化当前单例
//        private static BleNFCManager1 instance = new BleNFCManager1(); // 类加载机制会在对象初始化的时候加锁  使不会进行重排序的情况
//    }
//
//    private LocationManager locationManager = null;
//    String mProviderName = "";
//    private Context mContext = null;
//
//    public void initBleNFC(Application context, Activity activity, BleNFCListener listener) {
//        // 蓝牙插件的监听事件
//        mBlueToothListener = listener;
//        // 上下文事件
//        mContext = context;
//        // 判断GPS权限问题
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            mBlueToothListener.initFailed((byte) 0x0001); // 位置权限未打开
//            return;
//        }
//        // 蓝牙是否打开
//        if (!BleIsOpen()) { // 蓝牙未打开
//            mBlueToothListener.initFailed((byte) 0x0010);
//            return;
//        }
//        // 初始化控件
//        BleManager.getInstance().init(context);
//        // 设置扫描设备配置
//        BleManager.getInstance()
//                .enableLog(true) // 是否需要log
//                .setReConnectCount(1, 5000) //重连次数一次 每隔5秒重连一次
//                .setConnectOverTime(20000) // 连接超时的时间设置
//                .setOperateTimeout(5000); // 操作超时的时间设置
//        // 设置默认RSSI值 TODO 目前没有说需要控制RSSI值
////        setMinRssi(Constants.mMinRssi);
//        // 启动 GPS 定位信息相关信息
//        setLocationInfo(context);
//        // 插件初始化成功
//        mBlueToothListener.initSuccess();
//    }
//
//    /**
//     * 判断当前蓝牙硬件是否已经打开
//     * @return
//     */
//    public boolean BleIsOpen(){
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        return bluetoothAdapter.isEnabled();
//    }
//
//    private void setLocationInfo(Application activity) {
//        String serviceName = Context.LOCATION_SERVICE;
//        locationManager = (LocationManager) activity.getSystemService(serviceName);
//        // 查找到服务信息
//        Criteria criteria = new Criteria();
//        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        // 设置是否要求速度
//        criteria.setSpeedRequired(false);
//        // 设置是否需要海拔信息
//        criteria.setAltitudeRequired(false);
//        // 设置是否需要方位信息connNotDesDevice
//        criteria.setBearingRequired(false);
//        // 设置是否允许运营商收费
//        criteria.setCostAllowed(true);
//        // 设置对电源的需求
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//
//        // 为获取地理位置信息时设置查询条件
//        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
//
//        Location lastKnownLocation = null;
//        if (ActivityCompat.checkSelfPermission(activity,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(activity,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        mProviderName = LocationManager.GPS_PROVIDER;
//        if (lastKnownLocation == null) {
//            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            mProviderName = LocationManager.NETWORK_PROVIDER;
//        }
//        if (mProviderName != null && !"".equals(mProviderName)) {
//            locationManager.requestLocationUpdates(mProviderName, 1000, 1, locationListener);
//        }
//    }
//
//    /**
//     * 设置最小的RRSI TODO
//     * @param minRssi
//     */
//    private void setMinRssi(long minRssi){
//        Constants.mMinRssi = minRssi;
//    }
//
//    /**
//     * 获取最小的RSSI TODO
//     * @return
//     */
//    private long getMinRssi(){
//        return Constants.mMinRssi;
//    }
//
//    // 插件监听的实例化
//    private BleNFCListener mBlueToothListener;
//
//    // 获取设备信息 TODO
//    public void getBleNFCInfo(){
//        setScanRule();
//        startScan();
//    }
//
//    /**
//     * TODO 由于需要后台运行 需要考虑放到Stop的生命周期 还是Destroy的生命周期中
//     */
//    public void onStopBlueToothPlugin(){
//        if (locationManager != null) {
//            locationManager.removeUpdates(locationListener);
//        }
//    }
//
//    /**
//     * 设置扫描规则 TODO 需要先设置规则 才可以正式开始扫描连接设备
//     */
//    private void setScanRule() {
//        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
//                .setScanTimeOut(10000)      // 扫描超时时间，可选，默认10秒
//                .build();
//        BleManager.getInstance().initScanRule(scanRuleConfig);
//    }
//
//    /**
//     * TODO 是否扫描到可用设备 用BLE_KEY蓝牙名称判断
//     */
//    private boolean mIsScanDes = false;
//
//    /**
//     * 开始扫描
//     */
//    private void startScan() {
//        BleManager.getInstance().scan(new BleScanCallback() {
//            @Override
//            public void onScanStarted(boolean success) {
//            }
//
//            @Override
//            public void onLeScan(BleDevice bleDevice) {
//                super.onLeScan(bleDevice);
//            }
//
//            @Override
//            public void onScanning(BleDevice bleDevice) {
//                if (Constants.mBleName.equals(bleDevice.getName())){ // TODO 判断是否是我们需要的设备名称的设备
//                    if (!BleManager.getInstance().isConnected(bleDevice)) { // 判断设备名称是正常的设备是否已经被连接
////                        if (bleDevice.getRssi() >= getMinRssi()){ // 在规定的rssi范围内进行连接 TODO 目前没有要求
//                            mIsScanDes = true;
//                            mBlueToothListener.scanDevice(); // 已经扫描到一个可用设备
//                            BleManager.getInstance().cancelScan(); // 已经找到可以连接的设备 停止扫描设备
//                            connect(bleDevice); // 连接当前设备
////                        } else { // 未在rssi的范围内不进行操作
////                            mBlueToothListener.scanDeviceMinRSSI();
////                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onScanFinished(List<BleDevice> scanResultList) {
//                if (!mIsScanDes){
//                    mBlueToothListener.scanNotDevice(); // 提示监听 没有找到设备
//                } else {
//                    mIsScanDes = false;
//                }
//            }
//        });
//    }
//
//
//    private UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");//服务UUID
//    private UUID WRITE_CHARACTERISTIC = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");//写UUID
//    private UUID READ_CHARACTERISTIC = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");//读UUID
//
//    private void connect(final BleDevice bleDevice) {
//        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
//            @Override
//            public void onStartConnect() {
//                mBlueToothListener.startConnDevice((BleDevice) bleDevice); // 开始连接设备
//            }
//
//            @Override
//            public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                mBlueToothListener.connFailedDevice((BleDevice) bleDevice); // 设备连接失败
//            }
//
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//            @Override
//            public void onConnectSuccess(final BleDevice bleDevice, final BluetoothGatt gatt, int status) {
//                mBlueToothListener.connSuccesDevice((BleDevice) bleDevice); // 成功连接设备  准备验证数据
//
//
////                BluetoothGattService linkLossService = gatt.getService(UUID.fromString(UUID_SERVICE_READ));
////                final BluetoothGattCharacteristic characteristic2 = linkLossService.getCharacteristic(UUID.fromString(characteristic_uuid));
////
////                if(gatt.setCharacteristicNotification(characteristic2, true)){
////                    //获取到Notify当中的Descriptor通道 然后再进行注册
////                    BluetoothGattDescriptor clientConfig = characteristic2 .getDescriptor(UUID.fromString(characteristic_uuid));
////                    clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
////                    gatt.writeDescriptor(clientConfig);
////                    mBlueToothListener.getNotifyConnDeviceSuccess("打开通知操作成功");
////                } else {
////                    // 有问题
////                    mBlueToothListener.getNotifyConnDeviceFail("打开通知操作失败");
////                    return;
////                }
////
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        try{
////                            Thread.sleep(100);
////                            boolean isGetData = gatt.readCharacteristic(characteristic2);
////                            if (isGetData) {
////                                String scancord =  HexUtil.formatHexString(characteristic2.getValue());
////                                mBlueToothListener.getNotifyConnDeviceData(scancord);
////                            }
////                        }catch (Exception e){
////                            e.printStackTrace();
////                        }
////                    }
////                }).start();
//
//
////                BleManager.getInstance().notify(bleDevice,
////                        UUID_SERVICE_READ,
////                        characteristic_uuid,
////                        new BleNotifyCallback() {
////                            @Override
////                            public void onNotifySuccess() {
////                                // 打开通知操作成功
////                                mBlueToothListener.getNotifyConnDeviceSuccess("打开通知操作成功");
////                            }
////
////                            @Override
////                            public void onNotifyFailure(BleException exception) {
////                                // 打开通知操作失败
////                                mBlueToothListener.getNotifyConnDeviceFail("打开通知操作失败");
////                            }
////
////                            @Override
////                            public void onCharacteristicChanged(byte[] data) {
////                                // 打开通知后，设备发过来的数据将在这里出现
////                                String scancord =  HexUtil.formatHexString(bleDevice.getScanRecord());
////                                mBlueToothListener.getNotifyConnDeviceData(scancord);
////                            }
////                        });
//
//                // 成功获取到数据进行判断当前数据的是否是我要的格式
//                // 广播指示 02 01 06  数据固定 2 bytes
//
////                if (bleDevice.getRssi() < getMinRssi()){ // TODO 只要获取的RSSI的值大于当前设置的RSSI的值就可以正常的操作蓝牙设备
////                    // 当前所获取的信息不符合 断开连接
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
////                        setCloseConn(bleDevice);
////                        gatt.disconnect();
////                        gatt.close();
////                        mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
////                    }
////                    return;
////                }
//
//                /**
//                 * 02 01 06 05 02 c0 ff e0
//                 * ff 12 ff 0d 00 61 03 36
//                 * 00 02 00 00 00 15 12 29
//                 * f8 e6 a0 c5 08 09 42 4c
//                 * 45 5f 4b 45 59 05 12 0a
//                 * 00 14 00 02 0a 00 00 00
//                 * 00 00 00 00 00 00 00 00
//                 * 00 00 00 00 00 00 ----------------------31个字节数据 需要防止没有数据的情况使用的是00填充的数据
//                 */
//                // 开始解析数据 TODO ——————————————————————————————————————————————————————
////                String scancord = HexUtil.formatHexString(bleDevice.getScanRecord()); // 设备传递过来的数据
////                // TODO ******************将拿取到的数据开放给研发者
////                mBlueToothListener.getDeviceDataOriginal(scancord);
//                // TODO ******************
//
////                // TODO 获取当前字段中的最后一个9c结尾符号的位置  然后做截取
////                String splitDataStr = scancord.substring(0, scancord.lastIndexOf(mDataEnd) + 2);
////
////                // 将截取后的代码复制给需要进行解析的主字段的参数
////                scancord = splitDataStr;
////                // TODO 可能上面判断错误
////                String datas = scancord.substring(0, mDataStart.length());
////                String datae = scancord.substring(scancord.length() - 2);
////                // TODO 查看数据的开始数据是否是我们需要的固定元素
////                if (scancord.startsWith(mDataStart) && scancord.endsWith(mDataEnd)) { // 检查开始元素是不是和固定数据一样 是不是以9c结尾 一样的情况
////                    // TODO  进一步的解析
////                    parseData(scancord, bleDevice, gatt);
////                } else if (datas.equals(mDataStart) && datae.endsWith(mDataEnd)) { // 固定数据固定的情况 前面和后面固定
////                    // TODO  进一步的解析
////                    parseData(scancord, bleDevice, gatt);
////                } else if (datae.endsWith(mDataEnd)) { // 最后两位是固定数据
////                    parseData(scancord, bleDevice, gatt);
////                } else { // TODO 发现开始和结尾都不是我要的数据
////                    // 当前所获取的信息不符合 断开连接
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
////                        setCloseConn(bleDevice);
////                        gatt.disconnect();
////                        gatt.close();
////                        mBlueToothListener.connNotDesDevice((BleDevice) bleDevice); // 监听状态取消连接
////                    }
////                }
//
//                // 结束解析数据 TODO ——————————————————————————————————————————————————————
//            }
//            @Override
//            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                mBlueToothListener.disConnDevice((BleDevice) bleDevice); // 断开连接
//            }
//        });
//    }
//
//
//    /**
//     * 订阅蓝牙通知消息，在onCharacteristicChanged()回调中接收蓝牙返回的消息
//     *
//     * @param gatt
//     * @return
//     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public boolean enableNotification(BluetoothGatt gatt) {
//        boolean success = false;
//        BluetoothGattService service = gatt.getService(SERVICE_UUID);
//        if (service != null) {
//            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service);
//            if (characteristic != null) {
//                success = gatt.setCharacteristicNotification(characteristic, true);
//                gatt.readCharacteristic(characteristic);
//                if (success) {
//                    for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
//                        if (dp != null) {
//                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
//                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
//                                dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//                            }
//                            int writeType = characteristic.getWriteType();
//                            Log.e(BleNFCManager1.class.getSimpleName(), "enableNotification: " + writeType);
//                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                            gatt.writeDescriptor(dp);
//                            characteristic.setWriteType(writeType);
//                        }
//                    }
//                }
//            }
//        }
//        return success;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service) {
//        BluetoothGattCharacteristic characteristic = null;
//        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//        for (BluetoothGattCharacteristic c : characteristics) {
//            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 && READ_CHARACTERISTIC.equals(c.getUuid())) {
//                characteristic = c;
//                break;
//            }
//            //用于通讯的UUID character
//            if (c.getUuid().equals(WRITE_CHARACTERISTIC)) {
////                writeCharacteristic = c;
//            }
//        }
//        if (characteristic != null) {
//            return characteristic;
//        }
//        for (BluetoothGattCharacteristic c : characteristics) {
//            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 && READ_CHARACTERISTIC.equals(c.getUuid())) {
//                characteristic = c;
//                break;
//            }
//        }
//        return characteristic;
//    }
//
//
//
//    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
//
////    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
////    private void setAutoReceiveData(BluetoothGatt gatt) {
////        try {
////            BluetoothGattService linkLossService = gatt.getService(UUID.fromString(UUID_SERVICE_READ));
////            BluetoothGattCharacteristic data = linkLossService.getCharacteristic(UUID.fromString(characteristic_uuid));
////            BluetoothGattDescriptor defaultDescriptor = data.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
////            if (null != defaultDescriptor) {
////                defaultDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
////                bluetoothGatt.writeDescriptor(defaultDescriptor);
////            }
////            bluetoothGatt.setCharacteristicNotification(data, true);
////        } catch (Exception e) {
////            BleLogUtils.appendLog("setAutoReceiveData:" + e.getMessage());
////        }
////    }
//
//    private void parseData(String data,BleDevice bleDevice,BluetoothGatt gatt){
//        // 获取拿到数据的长度
//        int length = data.length();
////        switch (length){
////            case DATA_LENGTH_35: // 35 字段的时候的解析情况
//                /**
//                 * （注：三种类型的数据包长度一样，34字节；
//                 * 停止广播有效字段：IP/端口、Dev ID1、启动时间、电池电压、MAC地址；
//                 *       启动广播有效字段：IP/端口、Dev ID1、启动时间、电池电压、MAC地址；
//                 *       带坐标广播有效字段：IP/端口、Dev ID1、启动时间、电池电压、经纬度坐标、
//                 * 经纬度标识、MAC地址。）
//                 * 0x02 0x01 0x06 0x06 0x04 0x04 0x02 0x03 0x01  0x01  0xC0 0xA8 0x01 0x01 0x44 0x23   0x2F 0x0A  0x1E 0x1A 0x16  0x67 0x1A 0x16 0x12 0xFF 0xDD 0x00 0x44 0x33 0x22  0x9C
//                 *
//                 * 数据段解释示意：
//                 * 0x02 0x01 0x06 -> 包头
//                 * 0x06 0x04 0x04 0x02 0x03 0x01 -> UUID
//                 * 0x01 -> 代表停放事件
//                 * 0xC0 0xA8 0x01 0x01 -> 192 168 1 1 -> IP：192.168.1.1
//                 * 0x41 0x41 -> 01000001 01000001 -> 0100：B4(十六进制) 0001：B3(十六进制) 0100：B2(十六进制) 0001：B1(十六进制) -> Dev ID: B4 B3 B2 B1
//                 * 0x44 0x23 -> 0x2344 -> 端口：9028
//                 * 0x41 ->  01000001(二进制) -> 0100：4（十进制）0100:1（十进制）->电池电压：4.1
//                 * 0x0A ->停车前10分钟启动
//                 * 0x1E 0x1A 0x16  0x67 0x1A 0x16 -> 经纬度数据：30°26' 22" ,103°26' 22"
//                 * 0x12 ->卫星数：12
//                 * 0x01 ->代表NE北纬、东经
//                 * 0xFF 0xDD 0x00 0x44 0x33 0x22 -> 序列号：FFDD00443322
//                 * 0x9C ->结束符
//                 */
//                // TODO 前面符合信息 开始校验数据的格式是否正确 不做解析不做解析不做解析 只是验证
//        setReadData(bleDevice); // 读取数据
////        if (length > 36) { // 广播不做操作
////            // 拿到mac所有的数据
////            String mac = data.substring(6, 18);
////            // 分析出Mac的B1~B4
////            String mac_B1 = mac.substring(5, 6); // 0x04
////            String mac_B2 = mac.substring(10, 11); // 0x03
////            String mac_B3 = mac.substring(1, 2); // 0x02
////            String mac_B4 = mac.substring(7, 8); // 0x02
////            // 获取DevID数据
////            String devId = data.substring(32, 36); // B4 B3 B2 B1 TODO 有问题 怎么进行转换的
////            StringBuilder macb = new StringBuilder();
////            macb.append(mac_B4).append(mac_B3).append(mac_B2).append(mac_B1);
////            String mac_dev = macb.toString();
////            if (devId.equals(mac_dev)) { // 表示验证成功  开始回复数据
////                // TODO 替换拿到的数据 18~20 之间
////                String databefore = data.substring(0, 18);
////                String dataafter = data.substring(19);
////
////                StringBuilder result = new StringBuilder();
////                result.append(databefore).append("1").append(dataafter); // 将数据更改并且合并 回传给客户
////
////                mBlueToothListener.getDeviceData(result.toString());
////                // 进行验证 验证通过将数据更改并上传服务器
//////                setWriteData(bleDevice); // TODO  已做更改
////            } else {
////                // TODO  已做更改
//////                mBlueToothListener.checkDataIsFailure(mac, devId, mac_dev, data);
//////                //  获取到数据之后开始断开连接
//////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
//////                    setCloseConn(bleDevice);
//////                    gatt.disconnect();
//////                    gatt.close();
//////                    mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
//////                }
////            }
////        }
//
//
////                String devId_b4 = data.substring(22, 24);
////                String devId_b1 = data.substring(24, 26);
////                String scanMac = data.substring(42, 54);
////                String mac_b1 = scanMac.substring(4, 6);
////                String mac_b4 = scanMac.substring(6, 8);
////
////                if (scanMac.contains("000000000000")) {
////                    setOptionsData(scancord, bleDevice.getRssi());
////                    // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
////                    setWriteData(bleDevice);
////                } else if (devId_b1.equals(mac_b1) && devId_b4.equals(mac_b4)) {
////                    setOptionsData(scancord, bleDevice.getRssi());
////                    // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
////                    setWriteData(bleDevice);
////                } else {
////                    // 当前所获取的信息不符合 断开连接
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
////                        setCloseConn(bleDevice);
////                        gatt.disconnect();
////                        gatt.close();
////                        mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
////                    }
////                }
//
////                break;
////
////            default:
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // 18
////                    setCloseConn(bleDevice);
////                    gatt.disconnect();
////                    gatt.close();
////                    mBlueToothListener.connNotDesDevice((BleDevice) bleDevice);
////                }
////                break;
////        }
//    }
//
//    private void setReadData(final BleDevice bleDevice){
////        BleManager.getInstance().notify(bleDevice,
////                uuid_service,
////                characteristic_uuid,
////                new BleNotifyCallback() {
////                    @Override
////                    public void onNotifySuccess() {
////
////                    }
////
////                    @Override
////                    public void onNotifyFailure(BleException exception) {
////
////                    }
////
////                    @Override
////                    public void onCharacteristicChanged(byte[] data) {
////
////                    }
////                });
////
//
//        BleManager.getInstance().read(
//            bleDevice,
//            UUID_SERVICE_READ,
//            characteristic_uuid,
//                new BleReadCallback() {
//                    @Override
//                    public void onReadSuccess(byte[] data) {
//                        // 读特征值数据成功
////                        Log.e("ooooooooo","data = " + HexUtil.encodeHexStr(data));
//                        String scancord =  HexUtil.formatHexString(bleDevice.getScanRecord());
//                        // 成功获取到数据进行判断当前数据的是否是我要的格式
//                        // 广播指示 02 01 06  数据固定 2 bytes
//                        // UUID 05 02 c0 ff e0 ff 数据固定 6 bytes
//                        /**
//                         *  自定义数据
//                         *      长度 12 数据固定 1 bytes
//                         *      类型 FF 数据固定 1 bytes
//                         *      DevID 0D 00 数据固定 2 bytes
//                         *      电量 2 bytes
//                         *      芯片温度 2 bytes
//                         *      按键次数 4 bytes
//                         *      mac地址 6bytes
//                         *      发射功率 c5 数据固定 1bytes
//                         */
//                        // 02 01 06 05 02 c0 ff e0 ff 12 ff 0d 00 61 03 36 00 02 00 00 00 15 12 29 f8 e6 a0 c5 08 09 42 4c 45 5f 4b 45 59 05 12 0a 00 14 00 02 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
//
//                        mBlueToothListener.getConnDeviceData(scancord);
//
////                        String datas = scancord.substring(0, mData.length());
////                        if (mData.equals(datas)){
////                            setOptionsData(scancord,bleDevice.getRssi());
////                            // 获取成功之后再进行Indicate的执行和读取和发送消息  将在这里开始运行
////                            setWriteData(bleDevice);
////                        }
//                    }
//
//                    @Override
//                    public void onReadFailure(BleException exception) {
//                        // 读特征值数据失败
//                    }
//                });
//    }
//
//    public void setCloseConn(BleDevice bleDevice){
//        if (BleManager.getInstance().isConnected(bleDevice)) {
//            BleManager.getInstance().disconnect(bleDevice);
//        }
//    }
//
//    private void setWriteData(BleDevice bleDevice,byte datas){
//        final byte[] data = {datas};
//        BleManager.getInstance().write(
//                bleDevice,
//                UUID_SERVICE_WRITE,
//                characteristic_uuid,
//                data,
//                new BleWriteCallback() {
//                    @Override
//                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
//                        mBlueToothListener.replyDataToDeviceSuccess(new String(data));
//                    }
//
//                    @Override
//                    public void onWriteFailure(BleException exception) {
//                        // 发送数据到设备失败
//                        mBlueToothListener.replyDataToDeviceFailed(new String(data));
//                    }
//                });
//    }
//
//    // 清除掉我们打开的蓝牙设备
//    public void destroyBlueToothPlugin(){
//        BleManager.getInstance().disconnectAllDevice();
//        BleManager.getInstance().destroy();
//    }
//
//    /**
//     * 定位信息的相关代码
//     */
//    private LocationListener locationListener = new LocationListener() {
//        /**
//         * 位置信息变化时触发
//         */
//        public void onLocationChanged(Location location) {
//            Constants.mLatitude = location.getLatitude();
//            Constants.mLongitude = location.getLongitude();
//        }
//
//        /**
//         * GPS状态变化时触发
//         */
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            switch (status) {
//                // GPS状态为可见时
//                case LocationProvider.AVAILABLE:
//                    // 当前GPS状态为可见状态
//                    break;
//                // GPS状态为服务区外时
//                case LocationProvider.OUT_OF_SERVICE:
//                    // 当前GPS状态为服务区外状态
//                    break;
//                // GPS状态为暂停服务时
//                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    // 当前GPS状态为暂停服务状态
//                    break;
//            }
//        }
//
//        /**
//         * GPS开启时触发
//         */
//        public void onProviderEnabled(String provider) {
//            if (
//                    ActivityCompat.checkSelfPermission(mContext,
//                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                            ActivityCompat.checkSelfPermission(mContext,
//                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            Location location = locationManager.getLastKnownLocation(provider);
//            Constants.mLatitude = location.getLatitude();
//            Constants.mLongitude = location.getLongitude();
//        }
//        /**
//         * GPS禁用时触发
//         */
//        public void onProviderDisabled(String provider) {
//        }
//    };
//}
