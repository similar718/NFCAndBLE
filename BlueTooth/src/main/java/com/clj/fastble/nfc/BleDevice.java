//package com.clj.fastble.nfc;
//
//import android.annotation.TargetApi;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanResult;
//import android.content.Context;
//import android.os.Build;
//import android.os.Handler;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import androidx.annotation.RequiresApi;
//
//public class BleDevice {
//    private final String TAG = BleDevice.class.getSimpleName();
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothLeScanner bluetoothLeScanner;
//    private BluetoothManager bluetoothManager;
//    private android.bluetooth.le.ScanCallback scanCallback;
//    private BluetoothGatt bluetoothGatt;
//    private BluetoothGattCharacteristic writeCharacteristic;
//    private UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");//服务UUID
//    private UUID WRITE_CHARACTERISTIC = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");//写UUID
//    private UUID READ_CHARACTERISTIC = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");//读UUID
//
//
//    private static BleDevice bleDevice;
//    private SendData sendCmd;
//    private ConnectState connectState;//设备连接状态回调
//
//    /**
//     * 蓝牙设备连接状态   0-未连接 1 已连接   2 连接中
//     */
//    public boolean isConnect;
//
//
//    public static BleDevice getInstance() {
//        if (bleDevice == null) {
//            bleDevice = new BleDevice();
//        }
//        return bleDevice;
//    }
//
//    public BleDevice() {
//        init();
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void init() {
//        bluetoothManager = (BluetoothManager) BaseApplication.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
//        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//    }
//
//    /**
//     * 扫描蓝牙设备
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void scanDevice(final ScanDeviceCallback callback) {
//
//        final List<DeviceInfo> devices = new ArrayList<>();
//        final List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
//
//        scanCallback = new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                super.onScanResult(callbackType, result);
//                BluetoothDevice bluetoothDevice = result.getDevice();
//                //获取搜索到的蓝牙设备的mac地址
//                String mac = bluetoothDevice.getAddress();
//                //获取搜索到的蓝牙设备的蓝牙名称
//                String name = bluetoothDevice.getName();
//
//                DeviceInfo deviceInfo = new DeviceInfo();
//                deviceInfo.mac = mac;
//                deviceInfo.deviceName = name;
//                deviceInfo.rssi = result.getRssi();
//
//                if (!bluetoothDevices.contains(bluetoothDevice) && name != null && name.contains("Fit")) {
//                    bluetoothDevices.add(bluetoothDevice);
//                    devices.add(deviceInfo);
//                }
//            }
//
//            @Override
//            public void onBatchScanResults(List<ScanResult> results) {
//                super.onBatchScanResults(results);
//            }
//
//            @Override
//            public void onScanFailed(int errorCode) {
//
//                super.onScanFailed(errorCode);
//            }
//        };
//
//        if (bluetoothLeScanner == null) {
//            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//        }
//        bluetoothLeScanner.startScan(scanCallback);//开始扫描蓝牙设备
//
//        //3秒后停止扫描，并返回扫描结果list
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                bluetoothLeScanner.stopScan(scanCallback);//停止扫描
//                //将连接过的设备添加到devices
//                String deviceMac = (String) PreferenceManagerUtil.getInstance().get(PreferenceManagerUtil.DEVICE_MAC, "");
//                String deviceName = (String) PreferenceManagerUtil.getInstance().get(PreferenceManagerUtil.DEVICE_NAME, "");
//                if (!"".equals(deviceMac)) {
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.mac = deviceMac;
//                    deviceInfo.deviceName = deviceName;
//                    deviceInfo.rssi = 0;
//                    devices.add(deviceInfo);
//                }
//                callback.onScanResult(devices);
//            }
//        }, 3000);
//    }
//
//
//    /**
//     * 连接蓝牙设备,通过设备的mac连接
//     */
//    public void connectDevice(String mac, ConnectState connectState) {
//        this.connectState = connectState;
//        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
//        //连接蓝牙设备
//        bluetoothGatt = bluetoothDevice.connectGatt(BaseApplication.getInstance(), false, bluetoothGattCallback);
//    }
//
//    public void disConnectDevice() {
//        if (bluetoothGatt != null) {
//            bluetoothGatt.disconnect();
//            bluetoothGatt.close();
//            bluetoothGatt = null;
//            isConnect = false;
//        }
//
//    }
//
//    /**
//     * 连接蓝牙设备结果回调
//     */
//    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//
//
//        /**
//         * 连接设备
//         * @param gatt
//         * @param status
//         * @param newState
//         */
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.e(TAG, "GATT_SUCCESS");
//                connectionStateChange(gatt, newState);
//            } else if (status == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
//                Log.e(TAG, "不支持");
//            }
//
//        }
//
//        /**
//         * 发现设备服务
//         * @param gatt
//         * @param status
//         */
//        @Override
//        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                boolean flag = enableNotification(gatt);
//                if (flag) {
//                    Log.e(TAG, "蓝牙设备已连接");
//
//                    isConnect = true;
//                    connectState.success();
//                } else {
//                    Log.e(TAG, "蓝牙设备已断开连接");
//
//                    isConnect = false;
//                    connectState.fail(2);
//                }
//            }
//        }
//
//        /**
//         * 写数据
//         * @param gatt
//         * @param characteristic
//         * @param status
//         */
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            //写数据成功
//            if (BluetoothGatt.GATT_SUCCESS == status && WRITE_CHARACTERISTIC.equals(characteristic.getUuid())) {
//                Log.e(TAG, "write onCharacteristicWrite GATT_SUCCESS---" + status);
//            } else if (BluetoothGatt.GATT_FAILURE == status && WRITE_CHARACTERISTIC.equals(characteristic.getUuid())) {
//                Log.e(TAG, "write onCharacteristicWrite GATT_FAILURE" + status);
//            }
//        }
//
//        int dataNum = 0;
//        byte[] ecgData = new byte[80];//拼接ecgData
//        boolean isEcgData;
//
//        /**
//         * 通知数据，往设备写入数据之后，接收设备返回的数据
//         * @param gatt
//         * @param characteristic
//         */
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//
//            Log.e(TAG, " onCharacteristicChanged ");
//
//            //设备传输的数据包
//            byte[] packData = characteristic.getValue();
//
//            //心电数据
//            if (((packData[1] & 0xFF) == 0x48)) {
//                dataNum = 0;
//                isEcgData = true;
//            }
//            if (isEcgData) {
//                System.arraycopy(packData, 0, ecgData, packData.length * dataNum, packData.length);
//                dataNum++;
//                if (dataNum == 4) {
//                    isEcgData = false;
//                    packData = ecgData;
//                }
//            }
//
//            if (!isEcgData) {
//                printByteArray(packData);////////
//                ReceiveData receiveData = new ReceiveData(packData);
//                Log.e(TAG, "--isPass===" + receiveData.isValidPack());
//
//
//                //普通指令
//                if (receiveData.isValidPack()) {
//                    if (((packData[1] & 0xFF) == 0x48)) {//心电原始数据
//                        List<Integer> intData = new ArrayList<>();
//                        byte[] ecgData = new byte[72];
//                        System.arraycopy(packData, 7, ecgData, 0, ecgData.length);//截取数据部分
//                        long ecgId = (long) PreferenceManagerUtil.getInstance().get(PreferenceManagerUtil.ECG_ID, 0L);
//                        try {
//                            FileUtil.writeFileToSDCard(BaseApplication.getInstance(), ecgId, ecgData);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        ecgDataOpera(ecgData);
//
//                    } else {
//                        sendCmd.callback.success(receiveData);
//                    }
//
//                } else {
//                    sendCmd.callback.fail(receiveData);
//                }
//            }
//
//
//        }
//    };
//
//    /**
//     * 监听心电原始数据
//     */
//   /* public interface EcgDataListener {
//        void register(EcgDataListener ecgDataListener);
//
//        void remove(EcgDataListener ecgDataListener);
//
//        void send(byte[] ecgData);
//    }*/
//
//    /**
//     * 监听心电原始数据,画图，上传云端，给算法
//     */
//    public interface EcgDataListener {
//
//        void operaEcgData(byte[] ecgData);
//    }
//
//
//    List<EcgDataListener> ecgDataListeners = new ArrayList<>();
//
//    public void addEcgDataListener(EcgDataListener ecgDataListener) {
//        if (ecgDataListener == null) {
//            return;
//        }
//        if (!ecgDataListeners.contains(ecgDataListener)) {
//            ecgDataListeners.add(ecgDataListener);
//        }
//    }
//
//    public void removeEcgDataListener(EcgDataListener ecgDataListener) {
//        ecgDataListeners.remove(ecgDataListener);
//    }
//
//    public void ecgDataOpera(byte[] ecgData) {
//
//        for (EcgDataListener dataListener : ecgDataListeners) {
//            dataListener.operaEcgData(ecgData);
//        }
//    }
//
//    /**
//     * 处理蓝牙设备连接 操作
//     *
//     * @param gatt
//     * @param newState
//     */
//    private void connectionStateChange(final BluetoothGatt gatt, int newState) {
//        /**
//         * 当前蓝牙设备已经连接
//         */
//        if (newState == BluetoothProfile.STATE_CONNECTED) {
//            Log.e(TAG, "蓝牙设备连接成功");
//
//            //获取ble设备上面的服务
//            gatt.discoverServices();
//        }
//
//        /**
//         * 当前设备无法连接
//         */
//        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//
//            isConnect = false;
//            Log.e(TAG, "蓝牙设备连接失败");
////            connectDevice(wifiMac);
//        }
////        broadcastConnectStateChange();//将连接状态广播出去
//    }
//
//    /**
//     * 订阅蓝牙通知消息，在onCharacteristicChanged()回调中接收蓝牙返回的消息
//     *
//     * @param gatt
//     * @return
//     */
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
//                            Log.e(TAG, "enableNotification: " + writeType);
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
//                writeCharacteristic = c;
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
//    /**
//     * 发送指令
//     *
//     * @param sendData
//     * @param callback
//     */
//    public void write(SendData sendData, SendDataCallback callback) {
//
//        sendCmd = sendData;
//        sendCmd.callback = callback;
//        byte[] initData = sendCmd.send();
//        printByteArray(initData);
//        if (writeCharacteristic != null) {
//            writeCharacteristic.setValue(initData);
//            bluetoothGatt.writeCharacteristic(writeCharacteristic);
//        }
//    }
//
//
//    /**
//     * byte[] 转十六进制的字符串
//     *
//     * @param data
//     */
//    public void printByteArray(byte[] data) {
//
//        final StringBuilder hexString = new StringBuilder();
//        for (int i = 0; i < data.length; i++) {
//            if ((data[i] & 0xff) < 0x10)//0~F前面不零
//                hexString.append("0");
//            hexString.append(Integer.toHexString(0xFF & data[i]));
//        }
//        Log.e(TAG, "hexString======" + hexString.toString().toLowerCase());
//    }
//
//    public interface ConnectState {
//        void success();//连接成功
//
//        /**
//         * 连接失败
//         *
//         * @param error 1未扫描到设备，2连接失败
//         */
//        void fail(int error);
//    }
//}
