package com.example.getuserlocation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

public class CustomLocationManager {

    Context context;
    private LocationManager mLocationManager;
    private LocationValue locationValue;
    private Location networkLocation = null;
    private Location gpsLocation = null;

    private Timer mTimer;

    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;

    private static CustomLocationManager _instance;

    private CustomLocationManager() {
    }

    public static CustomLocationManager getCustomLocationManager() {
        if (_instance == null) {
            _instance = new CustomLocationManager();
        }
        return _instance;
    }

    public LocationManager getLocationManager(Context context) {
        context=context;
        if (mLocationManager == null)
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager;
    }

    public boolean getCurrentLocation(Context context, LocationValue result) {
        locationValue = result;
        if (mLocationManager == null)
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!isGpsEnabled && !isNetworkEnabled)
            return false;

        if (isGpsEnabled)
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return true;
            }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);

        if (isNetworkEnabled)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);

        mTimer = new Timer();
        mTimer.schedule(new GetLastKnownLocation(), 20000);

        return true;
    }

    LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            locationValue.getCurrentLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(networkLocationListener);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private LocationListener networkLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            locationValue.getCurrentLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(gpsLocationListener);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private class GetLastKnownLocation extends TimerTask {
        CurrentLocationHandler handler;

        GetLastKnownLocation() {
            handler = new CurrentLocationHandler();
        }

        @Override
        public void run() {
            mLocationManager.removeUpdates(gpsLocationListener);
            mLocationManager.removeUpdates(networkLocationListener);

            if (isGpsEnabled)
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (isNetworkEnabled)
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            handler.sendEmptyMessage(0);
        }
    }

    private class CurrentLocationHandler extends Handler {
        @Override
        public final void handleMessage(Message msg) {
            if (gpsLocation != null && networkLocation != null) {

                if (gpsLocation.getTime() > networkLocation.getTime())
                    locationValue.getCurrentLocation(gpsLocation);
                else
                    locationValue.getCurrentLocation(networkLocation);

                return;
            }

            if (gpsLocation != null) {
                locationValue.getCurrentLocation(gpsLocation);
                return;
            }

            if (networkLocation != null) {
                locationValue.getCurrentLocation(networkLocation);
                return;
            }

            locationValue.getCurrentLocation(null);
        }
    }
}
