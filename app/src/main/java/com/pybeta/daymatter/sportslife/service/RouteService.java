package com.pybeta.daymatter.sportslife.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.pybeta.daymatter.sportslife.MainActivity;
import com.pybeta.daymatter.sportslife.R;
import com.pybeta.daymatter.sportslife.activity.RouteDetailActivity;
import com.pybeta.daymatter.sportslife.bean.RoutePoint;
import com.pybeta.daymatter.sportslife.database.RouteDBHelper;
import com.pybeta.daymatter.sportslife.interfaces.AllInterface;
import com.pybeta.daymatter.sportslife.map.MyOrientationListener;
import com.pybeta.daymatter.sportslife.utils.Utils;

import java.util.ArrayList;

import static com.pybeta.daymatter.sportslife.utils.Constant.span;

/**
 *
 *   记录轨迹思路
 *   用Service获取经纬度，onCreate中开始采集经纬度点，保存到ArrayList
 *   每隔5秒取样一次，若经纬度未发生变化，丢弃该次取样
 *   在onDestroy中，将ArrayList转成JSON格式，然后存储到SDCard中
 *   显示轨迹思路
 *   读取目录下所有轨迹文件，并生成ListView
 *   在OnItemClick中将文件名称通过intent.putExtra传递给显示轨迹的Activity
 *   根据文件名将对应的JSON内容转成ArrayList
 *   然后将以上ArrayList的点集依次连线，并绘制到百度地图上
 *   设置起始点Marker，Zoom级别,中心点为起始点
 *
 * Created by luogj on 2018/4/2.
 */

public class RouteService extends Service {

    private static final String TAG = "RouteService";
    private double currentLatitude, currentLongitude;
    private LocationClient mLocationClient = null;
    private MyLocationListener mListener;
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private String rt_time, rt_distance, rt_price;
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;
    AllInterface.IUpdateLocation iUpdateLocation;
    public ArrayList<RoutePoint> routPointList = new ArrayList<>();
    public  int totalDistance = 0;
    public  float totalPrice = 0;
    public  long beginTime = 0, totalTime = 0;
    Notification notification;
    RemoteViews contentView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setiUpdateLocation(AllInterface.IUpdateLocation iUpdateLocation) {
        this.iUpdateLocation = iUpdateLocation;
    }

    public void onCreate() {
        super.onCreate();
        beginTime = System.currentTimeMillis();
//        RouteDBHelper dbHelper = new RouteDBHelper(this);
//        // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
//        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        totalTime = 0;
        totalDistance = 0;
        totalPrice = 0;
        routPointList.clear();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "RouteService--------onStartCommand---------------");
        initLocation();//初始化LocationgClient
        initNotification();
        Utils.acquireWakeLock(this);
        // 开启轨迹记录线程
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化定位连接
     */
    private void initLocation() {
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.location_marker);
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mLocationClient = new LocationClient(this);
        mListener = new MyLocationListener();
//         initMarkerClickEvent();
        //注册监听器
        mLocationClient.registerLocationListener(mListener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        mOption.setScanSpan(span);
        //设置 LocationClientOptionƒ20
        mLocationClient.setLocOption(mOption);
        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.location_marker);
        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
            }
        });
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        myOrientationListener.start();
    }

    /**
     * 初始化通知栏
     */
    private void initNotification() {
        int icon = R.mipmap.bike_icon2;
        contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        notification = new NotificationCompat.Builder(this).setContent(contentView).setSmallIcon(icon).build();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("flag", "notification");
        notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    }

    /**
     * 开启通知栏
     * @param time
     * @param distance
     * @param price
     */
    private void startNotification(String time, String distance, String price) {
        startForeground(1, notification);
        contentView.setTextViewText(R.id.bike_time, time);
        contentView.setTextViewText(R.id.bike_distance, distance);
        contentView.setTextViewText(R.id.bike_price, price);
        rt_time=time;
        rt_distance=distance;
        rt_price=price;
    }



    public boolean onUnBind(Intent intent) {
        Log.d(TAG, "onBind-------------");
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        myOrientationListener.stop();
        Log.d(TAG, "RouteService----0nDestroy---------------");
        Gson gson = new Gson();
        String routeListStr = gson.toJson(routPointList);
        Log.d(TAG, "RouteService----routeListStr-------------" + routeListStr);
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", totalTime + "");
        bundle.putString("totalDistance", totalDistance + "");
        bundle.putString("totalPrice", totalPrice + "");
        bundle.putString("routePoints", routeListStr);
        Intent intent = new Intent(this, RouteDetailActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (routPointList.size() > 2)
            insertData(routeListStr);
        Utils.releaseWakeLock();
        stopForeground(true);
    }

    //所有的定位信息都通过接口回调来实现
    public class MyLocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;
        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (null == bdLocation) return;
            //"4.9E-324"表示目前所处的环境（室内或者是网络状况不佳）造成无法获取到经纬度
            if ("4.9E-324".equals(String.valueOf(bdLocation.getLatitude())) || "4.9E-324".equals(String.valueOf(bdLocation.getLongitude()))) {
                return;
            }//过滤百度定位失败
            Log.d(TAG, "RouteService---------getAddrStr()-------------" + bdLocation.getAddrStr());
            double routeLat = bdLocation.getLatitude();
            double routeLng = bdLocation.getLongitude();
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteLat(routeLat);
            routePoint.setRouteLng(routeLng);
            if (routPointList.size() == 0)
                routPointList.add(routePoint);
            else {
                RoutePoint lastPoint = routPointList.get(routPointList.size() - 1);
                if (routeLat == lastPoint.getRouteLat() && routeLng == lastPoint.getRouteLng()) {

                } else {
                    LatLng lastLatLng = new LatLng(lastPoint.getRouteLat(),lastPoint.getRouteLng());
                    LatLng currentLatLng = new LatLng(routeLat, routeLng);
                    if (routeLat > 0 && routeLng > 0) {
                        double distance = DistanceUtil.getDistance(lastLatLng, currentLatLng);
                        //大于2米算作有效加入列表
                        if (distance > 2) {
                            //distance单位是米 转化为km/h
                            routePoint.speed = Double.parseDouble(String.format("%.1f", (distance/1000)*30*60));
                            routePoint.time=System.currentTimeMillis();
                            routPointList.add(routePoint);
                            totalDistance += distance;
                        }
                    }
                }
            }

            totalTime = (int) (System.currentTimeMillis() - beginTime) / 1000 / 60;
            totalPrice = (float) (Math.floor(totalTime / 30) * 0.5 + 0.5);
            startNotification(totalTime + "分钟", totalDistance + "米", totalPrice + "元");
            Intent intent = new Intent("com.locationreceiver");
            Bundle bundle = new Bundle();
            bundle.putString("totalTime", totalTime + "分钟");
            bundle.putString("totalDistance", totalDistance + "米");
            bundle.putString("totalPrice", totalPrice + "元");
            intent.putExtras(bundle);
            sendBroadcast(intent);
        }
    }

    public static class NetWorkReceiver extends BroadcastReceiver{
        public NetWorkReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo.State wifiState = null;
            NetworkInfo.State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED == mobileState) {
                Toast.makeText(context, context.getString(R.string.net_mobile), Toast.LENGTH_SHORT).show();
                // 手机网络连接成功
            } else if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED != mobileState) {
                Toast.makeText(context, context.getString(R.string.net_none), Toast.LENGTH_SHORT).show();
                // 手机没有任何的网络
            } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
                // 无线网络连接成功
                Toast.makeText(context, context.getString(R.string.net_wifi), Toast.LENGTH_SHORT).show();

            }
        }
    }

    /**
     * 将坐标点插入数据库
     * @param routeListStr
     */
    public void insertData(String routeListStr) {
        ContentValues values = new ContentValues();
        // 向该对象中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据当中的数据类型一致
        values.put("cycle_date", Utils.getDateFromMillisecond(beginTime));
        values.put("cycle_time", totalTime);
        values.put("cycle_distance", totalDistance);
        values.put("cycle_price", totalPrice);
        values.put("cycle_points", routeListStr);
        // 创建DatabaseHelper对象
        RouteDBHelper dbHelper = new RouteDBHelper(this);
        // 得到一个可写的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
        // 调用insert方法，就可以将数据插入到数据库当中
        // 第一个参数:表名称
        // 第二个参数：SQl不允许一个空列，如果ContentValues是空的，那么这一列被明确的指明为NULL值
        // 第三个参数：ContentValues对象
        sqliteDatabase.insert("cycle_route", null, values);
        sqliteDatabase.close();
    }
}
