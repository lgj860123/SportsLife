package com.pybeta.daymatter.sportslife.bean;

/**
 * Created by luogj on 2018/4/10.
 */

public class PoiObject {
    public String address;
    public String lattitude;
    public String longitude;
    public String district;

    public PoiObject(String address, String lattitude, String longitude,String district) {
        this.address = address;
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.district=district;
    }
}
