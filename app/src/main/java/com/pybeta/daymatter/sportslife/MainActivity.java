package com.pybeta.daymatter.sportslife;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.overlayutil.OverlayManager;
import com.overlayutil.WalkingRouteOverlay;
import com.pybeta.daymatter.sportslife.activity.CodeUnlockActivity;
import com.pybeta.daymatter.sportslife.activity.InvitingFriendsActivity;
import com.pybeta.daymatter.sportslife.activity.MyRouteActivity;
import com.pybeta.daymatter.sportslife.activity.NavigationActivity;
import com.pybeta.daymatter.sportslife.activity.ProblemFeedbackActivity;
import com.pybeta.daymatter.sportslife.activity.RouteDetailActivity;
import com.pybeta.daymatter.sportslife.activity.SettingActivity;
import com.pybeta.daymatter.sportslife.activity.UseGuideActivity;
import com.pybeta.daymatter.sportslife.activity.WalletActivity;
import com.pybeta.daymatter.sportslife.base.BaseActivity;
import com.pybeta.daymatter.sportslife.bean.BikeInfo;
import com.pybeta.daymatter.sportslife.custom.LeftDrawerLayout;
import com.pybeta.daymatter.sportslife.fragment.LeftMenuFragment;
import com.pybeta.daymatter.sportslife.interfaces.AllInterface;
import com.pybeta.daymatter.sportslife.map.MyOrientationListener;
import com.pybeta.daymatter.sportslife.map.RouteLineAdapter;
import com.pybeta.daymatter.sportslife.service.RouteService;
import com.pybeta.daymatter.sportslife.utils.LocationManager;
import com.pybeta.daymatter.sportslife.utils.Utils;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pybeta.daymatter.sportslife.bean.BikeInfo.infos;
import static com.pybeta.daymatter.sportslife.utils.Constant.span;

/**
 * 首页
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, OnGetRoutePlanResultListener, AllInterface.OnMenuSlideListener{

    private static final String TAG = "MainActivity";
    private double currentLatitude, currentLongitude, changeLatitude, changeLongitude;
    private ImageView splash_img, btn_locale, btn_refresh, menu_icon;
    public static TextView current_address;
    private TextView title, book_bt, cancel_book, end_route;
    private LinearLayout bike_layout, bike_distance_layout, bike_info_layout, confirm_cancel_layout;
    private TextView bike_code, bike_sound, book_countdown, prompt,tv_time, tv_distance, tv_price, unlock;
    public static TextView bike_distance, bike_time, bike_price;
    private long exitTime = 0;
    private View divider;
    private boolean isFirstIn;
    //自定义图标
    private BitmapDescriptor mIconLocation, dragLocationIcon, bikeIcon, nearestIcon;
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;
    private BikeInfo bInfo;

    PlanNode startNodeStr, endNodeStr;
    int nodeIndex = -1, distance;
    WalkingRouteResult nowResultWalk = null;
    boolean useDefaultIcon = true, hasPlanRoute = false, isServiceLive = false;
    RouteLine routeLine = null;
    OverlayManager routeOverlay = null;
    LatLng currentLL;
    LeftDrawerLayout mLeftDrawerLayout;
    LeftMenuFragment mMenuFragment;
    View shadowView;
    // 定位相关
    LocationClient mLocationClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private MyOrientationListener myOrientationListener;
    private MapView mMapView;
    private BaiduMap mBaiDuMap;
    private float mCurrentX;
    private boolean isFirstLoc = true; // 是否首次定位
    private final int DISMISS_SPLASH = 0;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_SPLASH:
                    Animator animator = AnimatorInflater.loadAnimator(MainActivity.this, R.animator.splash);
                    animator.setTarget(splash_img);
                    animator.start();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//在Application的onCreate()不行，必须在activity的onCreate()中
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity---------onCreate---------------");
        setStatusBar();
        initMap();
        initView();
        isServiceLive = Utils.isServiceWork(this, "com.biubike.service.RouteService");
        if (isServiceLive)
            beginService();

        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.fl_main_menu);
        mLeftDrawerLayout.setOnMenuSlideListener(this);

        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.fl_main_menu, mMenuFragment = new LeftMenuFragment()).commit();
        }
    }

    private void initMap() {
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        mBaiDuMap = mMapView.getMap();
        // 开启定位图层
        mBaiDuMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(span);//设置onReceiveLocation()获取位置的频率
        option.setIsNeedAddress(true);//如想获得具体位置就需要设置为true
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiDuMap.setMyLocationConfigeration(new MyLocationConfiguration( mCurrentMode, true, null));
        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        myOrientationListener.start();
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        initMarkerClickEvent();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(mCurrentX)//设定图标方向     // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiDuMap.setMyLocationData(locData);
            currentLatitude = bdLocation.getLatitude();
            currentLongitude = bdLocation.getLongitude();
            current_address.setText(bdLocation.getAddrStr());
            currentLL = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            LocationManager.getInstance().setCurrentLL(currentLL);
            LocationManager.getInstance().setAddress(bdLocation.getAddrStr());
            startNodeStr = PlanNode.withLocation(currentLL);
            //option.setScanSpan(2000)，每隔2000ms这个方法就会调用一次，而有些我们只想调用一次，所以要判断一下isFirstLoc
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                //地图缩放比设置为18
                builder.target(ll).zoom(18.0f);
                mBaiDuMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                changeLatitude = bdLocation.getLatitude();
                changeLongitude = bdLocation.getLongitude();
                if (!isServiceLive) {
                    addOverLayout(currentLatitude, currentLongitude);
                }
            }
        }
    }


    public void openMenu() {
        mLeftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        mLeftDrawerLayout.closeDrawer();
        shadowView.setVisibility(View.GONE);
    }

    private void initView() {
//        new SpeechUtil(this).startSpeech("欢迎光临");
        splash_img = (ImageView) findViewById(R.id.splash_img);
//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.guide_1);
        current_address = (TextView) findViewById(R.id.current_addr);
        bike_layout = (LinearLayout) findViewById(R.id.bike_layout);
        bike_distance_layout = (LinearLayout) findViewById(R.id.bike_distance_layout);
        bike_info_layout = (LinearLayout) findViewById(R.id.bike_info_layout);
        confirm_cancel_layout = (LinearLayout) findViewById(R.id.confirm_cancel_layout);
        bike_time = (TextView) findViewById(R.id.bike_time);
        bike_distance = (TextView) findViewById(R.id.bike_distance);
        bike_price = (TextView) findViewById(R.id.bike_price);
        bike_price.setText(R.string.price);
        tv_time = (TextView) findViewById(R.id.textview_time);
        tv_distance = (TextView) findViewById(R.id.textview_distance);
        tv_price = (TextView) findViewById(R.id.textview_price);
        unlock = (TextView) findViewById(R.id.unlock);
        divider = (View) findViewById(R.id.divider);


        bike_code = (TextView) findViewById(R.id.bike_code);
        bike_sound = (TextView) findViewById(R.id.bike_sound);
        book_countdown = (TextView) findViewById(R.id.book_countdown);
        prompt = (TextView) findViewById(R.id.prompt);
        cancel_book = (TextView) findViewById(R.id.cancel_book);
        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.custom_leftDrawerLayout);
        shadowView = (View) findViewById(R.id.shadow);
        menu_icon = (ImageView) findViewById(R.id.menu_icon);
        bike_sound.setOnClickListener(this);
        menu_icon.setOnClickListener(this);
        shadowView.setOnClickListener(this);
//        mLeftDrawerLayout.setListener(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 50));
        layoutParams.setMargins(0, statusBarHeight, 0, 0);//4个参数按顺序分别是左上右下
//        title_layout.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.i(TAG, "statusBarHeight---------------" + statusBarHeight);
        layoutParams2.setMargins(40, statusBarHeight + Utils.dp2px(MainActivity.this, 50), 0, 0);//4个参数按顺序分别是左上右下
//      person_layout.setLayoutParams(layoutParams2);

//        String price = "1元";
//        setSpannableStr(bike_price, price, 0, price.length() - 1);

        mBaiDuMap = mMapView.getMap();

        mBaiDuMap.setOnMapStatusChangeListener(changeListener);
        btn_locale = (ImageView) findViewById(R.id.btn_locale);
        btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        end_route = (TextView) findViewById(R.id.end_route);
        title = (TextView) findViewById(R.id.title);
        book_bt = (TextView) findViewById(R.id.book_bt);
        book_bt.setOnClickListener(this);
        cancel_book.setOnClickListener(this);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        end_route.setOnClickListener(this);
        mMapView.setOnClickListener(this);
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
        handler.sendEmptyMessageDelayed(DISMISS_SPLASH, 3000);
    }


    public void getMyLocation() {
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiDuMap.setMapStatus(msu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.book_bt:
                bike_info_layout.setVisibility(View.VISIBLE);
                confirm_cancel_layout.setVisibility(View.VISIBLE);
                prompt.setVisibility(View.VISIBLE);
                bike_distance_layout.setVisibility(View.GONE);
                book_bt.setVisibility(View.GONE);
                bike_code.setText(bInfo.getName());
                countDownTimer.start();
                break;
            case R.id.cancel_book:
                cancelBook();
                break;
            case R.id.btn_locale:
                getMyLocation();
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                Log.i(TAG, "currentLatitude-----btn_locale--------" + currentLatitude);
                Log.i(TAG, "currentLongitude-----btn_locale--------" + currentLongitude);
//                startNodeStr = PlanNode.withLocation(currentLL);
                addOverLayout(currentLatitude, currentLongitude);
                break;
            case R.id.btn_refresh:
//              Intent intent = new Intent(MainActivity.this, LocationDemo.class);
//              startActivity(intent);
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                Log.i(TAG, "changeLatitude-----btn_refresh--------" + changeLatitude);
                Log.i(TAG, "changeLongitude-----btn_refresh--------" + changeLongitude);
                addOverLayout(changeLatitude, changeLongitude);
//                drawPlanRoute(endNodeStr);
                break;
            case R.id.end_route:
                toastDialog();
                break;
            case R.id.menu_icon:
                Log.i(TAG, "menu_icon-----click--------openMenu()");
                openMenu();
                break;
            case R.id.bike_sound:
                beginService();
                break;
            case R.id.shadow:
                closeMenu();
                Log.d(TAG, "shadow-----click--------closeMenu()");
                break;
        }
    }

    private void cancelBook() {
        countDownTimer.cancel();
        bike_layout.setVisibility(View.GONE);
        bike_info_layout.setVisibility(View.GONE);
        confirm_cancel_layout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
        bike_distance_layout.setVisibility(View.VISIBLE);
        bike_distance_layout.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.VISIBLE);
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        //地图缩放比设置为18
        builder.target(currentLL).zoom(18.0f);
        mBaiDuMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onGetWalkingRouteResult(final WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;

            if (result.getRouteLines().size() > 1) {
                nowResultWalk = result;

                MyTransitDlg myTransitDlg = new MyTransitDlg(MainActivity.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.WALKING_ROUTE);
                myTransitDlg.setOnItemInDlgClickListener(new OnItemInDlgClickListener() {
                    public void onItemClick(int position) {
                        routeLine = nowResultWalk.getRouteLines().get(position);
                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiDuMap);

                        routeOverlay = overlay;
                        //路线查询成功
                        try {
                            overlay.setData(nowResultWalk.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "路径规划异常", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
                myTransitDlg.show();

            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                routeLine = result.getRouteLines().get(0);
                int totalDistance = routeLine.getDistance();
                int totalTime = routeLine.getDuration() / 60;
                bike_distance.setText(Utils.distanceFormatter(totalDistance));
                bike_time.setText(Utils.timeFormatter(totalTime));
                String distanceStr = Utils.distanceFormatter(totalDistance);
                String timeStr = Utils.timeFormatter(totalTime);
//                setSpannableStr(bike_time, timeStr, 0, timeStr.length() - 2);
//                setSpannableStr(bike_distance, distanceStr, 0, distanceStr.length() - 1);
                Log.i(TAG, "totalDistance------------------" + totalDistance);

                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiDuMap);
//                    mBaiDuMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            } else {
                Log.i(TAG, "结果数<0");
                return;
            }
        }
    }

    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        System.out.print("");
    }

    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
        System.out.print("");
    }

    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        System.out.print("");
    }

    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
        System.out.print("");
    }

    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        System.out.print("");
    }

    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        String hex = Integer.toHexString(alpha).toUpperCase();
        Log.i(TAG, "color------------" + "#" + hex + "000000");
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }


    private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            String _str = mapStatus.toString();
            String _regex = "target lat: (.*)\ntarget lng";
            String _regex2 = "target lng: (.*)\ntarget screen x";
            changeLatitude = Double.parseDouble(latLng(_regex, _str));
            changeLongitude = Double.parseDouble(latLng(_regex2, _str));
            LatLng changeLL = new LatLng(changeLatitude, changeLongitude);
            startNodeStr = PlanNode.withLocation(changeLL);
            Log.i(TAG, "changeLatitude-----change--------" + changeLatitude);
            Log.i(TAG, "changeLongitude-----change--------" + changeLongitude);
        }

        public void onMapStatusChange(MapStatus mapStatus) {
        }
    };

    private String latLng(String regexStr, String str) {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            str = matcher.group(1);
        }
        return str;
    }

    public void addInfoListOverlay(List<BikeInfo> bikeInfoList) {
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
        Marker marker = null;
        for (BikeInfo info : bikeInfoList) {
            // 位置
            latLng = new LatLng(info.getLatitude(), info.getLongitude());
            // 图标
            overlayOptions = new MarkerOptions().position(latLng)
                    .icon(bikeIcon).zIndex(5);
            marker = (Marker) (mBaiDuMap.addOverlay(overlayOptions));
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);
        }
        // 将地图移到到最后一个经纬度位置
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiDuMap.setMapStatus(u);
    }

    private void addOverLayout(double _latitude, double _longitude) {
        //先清除图层
        mBaiDuMap.clear();
        mLocationClient.requestLocation();
        // 定义Maker坐标点
        LatLng point = new LatLng(_latitude, _longitude);
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(point)
                .icon(dragLocationIcon);
        // 在地图上添加Marker，并显示
        mBaiDuMap.addOverlay(options);
        infos.clear();
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_mobai, "001",
                "100米", "1分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_youbai, "002",
                "200米", "2分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_ofo, "003",
                "300米", "3分钟"));
        infos.add(new BikeInfo(_latitude - new Random().nextInt(5) * 0.0005, _longitude - new Random().nextInt(5) * 0.0005, R.mipmap.bike_xiaolan, "004",
                "400米", "4分钟"));
        BikeInfo bikeInfo = new BikeInfo(_latitude - 0.0005, _longitude - 0.0005, R.mipmap.bike_xiaolan, "005",
                "50米", "0.5分钟");
        infos.add(bikeInfo);
        addInfoListOverlay(infos);
        initNearestBike(bikeInfo, new LatLng(_latitude - 0.0005, _longitude - 0.0005));
    }

    private void initMarkerClickEvent() {
        // 对Marker的点击
        mBaiDuMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // 获得marker中的数据
                if (marker != null && marker.getExtraInfo() != null) {
                    BikeInfo bikeInfo = (BikeInfo) marker.getExtraInfo().get("info");
                    if (bikeInfo != null)
                        updateBikeInfo(bikeInfo);
                }
                return true;
            }
        });
    }

    private void initNearestBike(final BikeInfo bikeInfo, LatLng ll) {
        ImageView nearestIcon = new ImageView(getApplicationContext());
        nearestIcon.setImageResource(R.mipmap.nearest_icon);
        InfoWindow.OnInfoWindowClickListener listener = null;
        listener = new InfoWindow.OnInfoWindowClickListener() {
            public void onInfoWindowClick() {
                updateBikeInfo(bikeInfo);
                mBaiDuMap.hideInfoWindow();
            }
        };
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(nearestIcon), ll, -108, listener);
        mBaiDuMap.showInfoWindow(mInfoWindow);
    }

    private void updateBikeInfo(BikeInfo bikeInfo) {

        if (!hasPlanRoute) {
            bike_layout.setVisibility(View.VISIBLE);
            bike_time.setText(bikeInfo.getTime());
            bike_distance.setText(bikeInfo.getDistance());
            bInfo = bikeInfo;
            endNodeStr = PlanNode.withLocation(new LatLng(bikeInfo.getLatitude(), bikeInfo.getLongitude()));
            drawPlanRoute(endNodeStr);
        }
    }

    private void drawPlanRoute(PlanNode endNodeStr) {
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        if (endNodeStr != null) {

            Log.i(TAG, "changeLatitude-----startNode--------" + startNodeStr.getLocation().latitude);
            Log.i(TAG, "changeLongitude-----startNode--------" + startNodeStr.getLocation().longitude);
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(startNodeStr).to(endNodeStr));

        }
    }

    private CountDownTimer countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            book_countdown.setText(millisUntilFinished / 60000 + "分" + ((millisUntilFinished / 1000) % 60) + "秒");
        }

        @Override
        public void onFinish() {
            book_countdown.setText("预约结束");
            Toast.makeText(MainActivity.this, getString(R.string.cancel_book_toast), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {
        private List<? extends RouteLine> mTransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mTransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mTransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    onItemInDlgClickListener.onItemClick( position);
//                    mBtnPre.setVisibility(View.VISIBLE);
//                    mBtnNext.setVisibility(View.VISIBLE);
//                    dismiss();

                }
            });
        }

        public void setOnItemInDlgClickListener(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }
    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//            }
//            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
//            if (useDefaultIcon) {
            return BitmapDescriptorFactory.fromResource(R.mipmap.transparent_icon);
//            }
//            return null;
        }
    }

    /**
     * 进入扫码解锁
     * @param view
     */
    public void gotoCodeUnlock(View view) {
        startActivity(new Intent(this, CodeUnlockActivity.class));
    }

    /**
     * 进入选择目的地
     * @param view
     */
    public void gotoNavigation(View view) {
        startActivity(new Intent(this, NavigationActivity.class));
    }

    /**
     * 进入我的钱包
     * @param view
     */
    public void gotoWallet(View view) {
        startActivity(new Intent(this, WalletActivity.class));
    }


    /**
     * 进入我的行程
     * @param view
     */
    public void gotoMyRoute(View view) {
        startActivity(new Intent(this, MyRouteActivity.class));
    }

    /**
     * 进入邀请好友
     * @param view
     */
    public void gotoInvitingFriends(View view){
        startActivity(new Intent(this, InvitingFriendsActivity.class));
    }

    /**
     * 进入问题反馈
     * @param view
     */
    public void goToProblemFeedback(View view){
        startActivity(new Intent(this, ProblemFeedbackActivity.class));
    }

    /**
     * 进入使用指南
     * @param view
     */
    public void goToUseGuide(View view){
        startActivity(new Intent(this, UseGuideActivity.class));
    }

    /**
     * 进入设置
     * @param view
     */
    public void goToSetting(View view){
        startActivity(new Intent(this, SettingActivity.class));
    }



    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "MainActivity------------onStart------------------");
    }

    protected void onRestart() {
        super.onRestart();
        mBaiDuMap.setMyLocationEnabled(true);
        mLocationClient.start();
        myOrientationListener.start();
        mLocationClient.requestLocation();
        isServiceLive = Utils.isServiceWork(this, "com.biubike.service.RouteService");
        Log.i(TAG, "MainActivity------------onRestart------------------");
        if (CodeUnlockActivity.unlockSuccess || isServiceLive) {
            beginService();
        }
        if (RouteDetailActivity.completeRoute)
            backFromRouteDetail();
    }

    private void backFromRouteDetail() {
        isFirstIn = true;
        title.setText(getString(R.string.bybike));
        tv_time.setText(getString(R.string.foot));
        tv_distance.setText(getString(R.string.distance));
        tv_price.setText(getString(R.string.price));

        tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bike_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bike_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bike_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        bike_layout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
        current_address.setVisibility(View.VISIBLE);
        menu_icon.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.VISIBLE);
        unlock.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        btn_refresh.setVisibility(View.VISIBLE);
        btn_locale.setVisibility(View.VISIBLE);
        end_route.setVisibility(View.GONE);
        mMapView.showZoomControls(true);

        getMyLocation();
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        addOverLayout(currentLatitude, currentLongitude);
    }

    private void beginService() {
        if (!Utils.isGpsOPen(this)) {
            Utils.showDialog(this);
            return;
        }
        title.setText(getString(R.string.routing));
        tv_time.setText(getString(R.string.bike_time));
        tv_distance.setText(getString(R.string.bike_distance));
        tv_price.setText(getString(R.string.bike_price));
        prompt.setText(getString(R.string.routing_prompt));

        tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_distance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bike_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        prompt.setVisibility(View.VISIBLE);
        bike_layout.setVisibility(View.VISIBLE);
        current_address.setVisibility(View.GONE);
        menu_icon.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        btn_refresh.setVisibility(View.GONE);

        countDownTimer.cancel();
        bike_info_layout.setVisibility(View.GONE);
        confirm_cancel_layout.setVisibility(View.GONE);
        bike_distance_layout.setVisibility(View.VISIBLE);
        book_bt.setVisibility(View.GONE);
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        btn_locale.setVisibility(View.GONE);
        bike_info_layout.setVisibility(View.GONE);
        end_route.setVisibility(View.VISIBLE);
        mMapView.showZoomControls(false);
        mBaiDuMap.clear();
        if (isServiceLive)
            mLocationClient.requestLocation();
        Intent intent = new Intent(this, RouteService.class);
        startService(intent);
        MyLocationConfiguration configuration = new MyLocationConfiguration(locationMode, true, mIconLocation);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiDuMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        countDownTimer.cancel();
        isFirstIn = true;
        Log.i(TAG, "MainActivity------------onDestroy------------------");
    }

    /**
     * 连续点击两次返回按钮退出应用
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (bike_layout.getVisibility() == View.VISIBLE) {
                if (!Utils.isServiceWork(this, "com.biubike.service.RouteService"))
                    cancelBook();
                return true;
            }

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
//                finish();
//                System.exit(0);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public static class LocationReceiver extends BroadcastReceiver {
        public LocationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isTopActivity(context)) {
                String time = intent.getStringExtra("totalTime");
                String distance = intent.getStringExtra("totalDistance");
                String price = intent.getStringExtra("totalPrice");
                bike_time.setText(time);
                bike_distance.setText(distance);
                bike_price.setText(price);
            } else {
                Log.i(TAG, "MainActivity-------TopActivity---------false");
            }
        }
    }

    protected void toastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认要结束进程吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, RouteService.class);
                stopService(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
