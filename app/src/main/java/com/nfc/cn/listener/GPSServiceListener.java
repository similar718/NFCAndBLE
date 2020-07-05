package com.nfc.cn.listener;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSServiceListener implements LocationListener {

    private static final float minAccuracyMeters = 35;
    public int GPSCurrentStatus;

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
//            if (location.hasAccuracy() && location.getAccuracy() <= minAccuracyMeters) {
//                Constants.mLatitude = location.getLatitude();
//                Constants.mLongitude = location.getLongitude();
//            }
//            Constants.mLatitude = location.getLatitude();
//            Constants.mLongitude = location.getLongitude();
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        GPSCurrentStatus = status;
    }
}