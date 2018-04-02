package com.pybeta.daymatter.sportslife.utils;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by luogj on 2018/4/2.
 */

public class LocationManager {
    LatLng currentLL;
    String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getCurrentLL() {
        return currentLL;
    }

    public void setCurrentLL(LatLng currentLL) {
        this.currentLL = currentLL;
    }

    public static LocationManager getInstance() {
        return SingletonFactory.singletonInstance;
    }

    private static class SingletonFactory {
        private static LocationManager singletonInstance = new LocationManager();
    }
}
