package com.pybeta.daymatter.sportslife.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 运动轨迹坐标点
 * Created by luogj on 2018/4/2.
 */

public class RoutePoint implements Parcelable {
    //
    public int id;
    //时间
    public long time;
    //横向坐标，纵向坐标，速度
    public double routeLat, routeLng, speed;

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRouteLat() {
        return routeLat;
    }

    public void setRouteLat(double routeLat) {
        this.routeLat = routeLat;
    }

    public double getRouteLng() {
        return routeLng;
    }

    public void setRouteLng(double routeLng) {
        this.routeLng = routeLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(routeLat);
        parcel.writeDouble(routeLng);
        parcel.writeDouble(speed);
        parcel.writeLong(time);
        parcel.writeInt(id);

    }

    public static final Parcelable.Creator<RoutePoint> CREATOR = new Creator<RoutePoint>() {
        @Override
        public RoutePoint createFromParcel(Parcel source) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.id = source.readInt();
            routePoint.routeLat = source.readDouble();
            routePoint.routeLng = source.readDouble();
            routePoint.speed = source.readDouble();
            routePoint.time = source.readLong();
            return routePoint;
        }

        @Override
        public RoutePoint[] newArray(int size) {
            return new RoutePoint[size];
        }
    };
}
