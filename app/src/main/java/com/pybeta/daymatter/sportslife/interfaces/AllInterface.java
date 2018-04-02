package com.pybeta.daymatter.sportslife.interfaces;

/**
 * Created by luogj on 2018/4/2.
 */

public class AllInterface {

    public  interface OnMenuSlideListener{
        void onMenuSlide(float offset);
    }

    public  interface IUnlock{
        void onUnlock();
    }

    public  interface IUpdateLocation{
        void updateLocation(String totalTime,String totalDistance);
        void endLocation();
    }
}
