package com.nfc.cn.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nfc.cn.listener.GPSServiceListener;

import androidx.core.app.ActivityCompat;

public class GPSService extends Service {

    //2000ms
    private static final long minTime = 2000;
    //最小变更距离 10m
    private static final float minDistance = 10;

    String tag = this.toString();

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final IBinder mBinder = new GPSServiceBinder();

    public void startService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSServiceListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
    }

    public void endService() {
        if(locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        startService();
        Log.v(tag, "GPSService Started.");
    }

    @Override
    public void onDestroy() {
        endService();
        Log.v(tag, "GPSService Ended.");
    }

    public class GPSServiceBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
    }
}