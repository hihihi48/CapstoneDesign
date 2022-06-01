package com.example.weatherinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherinfo.weatherAPI.ForeCastManager;
import com.example.weatherinfo.weatherAPI.WeatherInfo;
import com.example.weatherinfo.weatherAPI.WeatherToKorean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private TextView txtResult;
    private ImageView mainWeather;
    SwipeRefreshLayout swipeRefreshLayout;

    LocationManager lm;

    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    TextView mainWeatherText;
    double longitude; // 좌표 설정
    double latitude;  // 좌표 설정
    double altitude;
    Double Temp;
    Intent intent;

    ForeCastManager mForeCast;

    MainActivity mThis;
    ArrayList<ContentValues> mWeatherData;
    ArrayList<WeatherInfo> mWeatherInfomation;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("MM월 dd일 hh:mm:ss");
    ImageView cloth1, cloth2, cloth3;

    RecyclerView mRecyclerView;
    List<WeatherItem> list = new ArrayList<>();

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = (TextView) findViewById(R.id.text1);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        Initialize();

        cloth1 = findViewById(R.id.Cloth1);
        cloth2 = findViewById(R.id.Cloth2);
        cloth3 = findViewById(R.id.Cloth3);
        cloth1.setOnClickListener(new ImageClickListener());
        cloth2.setOnClickListener(new ImageClickListener());
        cloth3.setOnClickListener(new ImageClickListener());

    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            toast.cancel();
        }
    }

    class ImageClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            intent = new Intent(getApplicationContext(), WebViewActivity.class);
            String cloth = String.valueOf(view.getTag());
            Log.d("ClickListener", cloth);
            if (!cloth.equals("0")) {
                intent.putExtra("옷", cloth);
                startActivity(intent);
            }
        }
    }

    //날씨 API 받아오기
    public void Initialize()
    {
        mainWeatherText = (TextView)findViewById(R.id.mainWeatherText);
        mainWeather = (ImageView) findViewById(R.id.MainWeather);

        mWeatherInfomation = new ArrayList<>();
        mThis = this;
        mForeCast = new ForeCastManager(String.valueOf(longitude), String.valueOf(latitude), mThis);
        mForeCast.run();
    }

    //API 값 출력 함수
    public  String PrintValue(){
        String mData = getDate(mWeatherInfomation.get(0).getWeather_Day()) + "\r\n";
        mData= mData
                + mWeatherInfomation.get(0).getWeather_Name() + "\r\n"
                + "구름량: " + mWeatherInfomation.get(0).getClouds_Value()
                + "(" +mWeatherInfomation.get(0).getClouds_Per() + ")\r\n"
                + "풍속: " + mWeatherInfomation.get(0).getWind_Speed() + " mps"
                + "(" +mWeatherInfomation.get(0).getWind_Name()+ ")\r\n"
                + "최고/최저기온: " + mWeatherInfomation.get(0).getTemp_Max()
                +"/"+ mWeatherInfomation.get(0).getTemp_Min() +"℃" + "\r\n"
                + "습도: " + mWeatherInfomation.get(0).getHumidity() + "%"+"\r\n";
        Temp = Double.parseDouble(mWeatherInfomation.get(0).getTemp_Max());
        return mData;
    }

    //subWeather 출력
    public void init(){

        list.clear();

        //recyclerView
        mRecyclerView = findViewById(R.id.subWeather_view);

        for (int i = 8; i < 40; i+=8){
            list.add(new WeatherItem( getDate(mWeatherInfomation.get(i).getWeather_Day()).substring(0,10), SubIcon(i),
                    mWeatherInfomation.get(i).getTemp_Max()+"/"+mWeatherInfomation.get(i).getTemp_Min()+"℃",
                    mWeatherInfomation.get(i).getHumidity()+"%",
                    mWeatherInfomation.get(i).getWeather_Name()));
        }

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(new WeatherAdapter(list));

    }

    //조건에 따라 알맞은 사진 아이콘의 번호를 반환 (수정필요함)
    public int SubIcon(int i) {
        //mRecyclerView.setAdapter(new WeatherAdapter(list));
        int icon=0;
        //mainWeather = (ImageView) findViewById(R.id.MainWeather);
        if (mWeatherInfomation.get(i).getWeather_Name() == "맑은 하늘") {

            icon=getResources().getIdentifier("sunday_icon","drawable","com.example.weatherinfo");
        } else if (mWeatherInfomation.get(i).getWeather_Name() == "눈") {

            icon=getResources().getIdentifier("snowflake_icon","drawable","com.example.weatherinfo");
        } else if (mWeatherInfomation.get(i).getWeather_Name() == "비" || mWeatherInfomation.get(i).getWeather_Name() == "강한 비" || mWeatherInfomation.get(i).getWeather_Name() == "가벼운 비") {
            // return R.drawable.rain_cloud_icon;
            icon=getResources().getIdentifier("rain_cloud_icon","drawable","com.example.weatherinfo");
        } else if (mWeatherInfomation.get(i).getWeather_Name() == "흐림") {
            //return R.drawable.sun_cloudy_icon;
            icon=getResources().getIdentifier("clouds_weather_cloud_icon","drawable","com.example.weatherinfo");
        } else {
            //return R.drawable.clouds_weather_cloud_icon;
            icon=getResources().getIdentifier("sun_cloudy_icon","drawable","com.example.weatherinfo");
        }
        return icon;
    }

    //날짜+i string 변환. 수정 필요함
    private String getDate(String day) {
        String Time = day.replace("T", " ");
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            mDate = simpleDate.parse(Time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        cal.add(Calendar.HOUR, +9);
        String getTime = simpleDate.format(cal.getTime());
        Log.d("getDate",getTime);
        return getTime;
    }


    public void DataChangedToKorean()
    {
        for(int i = 0 ; i <mWeatherInfomation.size(); i ++)
        {
            WeatherToKorean mHangeul = new WeatherToKorean(mWeatherInfomation.get(i));
            mWeatherInfomation.set(i,mHangeul.getKoreanWeather());
        }
    }

    public void DataToInformation()
    {
        for(int i = 0; i < mWeatherData.size(); i++)
        {
            mWeatherInfomation.add(new WeatherInfo(
                    String.valueOf(mWeatherData.get(i).get("weather_Name")),  String.valueOf(mWeatherData.get(i).get("weather_Number")), String.valueOf(mWeatherData.get(i).get("weather_Much")),
                    String.valueOf(mWeatherData.get(i).get("weather_Type")),  String.valueOf(mWeatherData.get(i).get("wind_Direction")),  String.valueOf(mWeatherData.get(i).get("wind_SortNumber")),
                    String.valueOf(mWeatherData.get(i).get("wind_SortCode")),  String.valueOf(mWeatherData.get(i).get("wind_Speed")),  String.valueOf(mWeatherData.get(i).get("wind_Name")),
                    String.valueOf(mWeatherData.get(i).get("temp_Min")),  String.valueOf(mWeatherData.get(i).get("temp_Max")),  String.valueOf(mWeatherData.get(i).get("humidity")),
                    String.valueOf(mWeatherData.get(i).get("Clouds_Value")),  String.valueOf(mWeatherData.get(i).get("Clouds_Sort")), String.valueOf(mWeatherData.get(i).get("Clouds_Per")),String.valueOf(mWeatherData.get(i).get("day"))
            ));

        }

    }

    //이미지 아이콘 변경
    public void MainImageIcon()
    {
        //mainWeather = (ImageView) findViewById(R.id.MainWeather);
        if(mWeatherInfomation.get(0).getWeather_Name()=="맑은 하늘"){
            mainWeather.setImageResource(R.drawable.sunday_icon);
        }else if(mWeatherInfomation.get(0).getWeather_Name()=="눈"){
            mainWeather.setImageResource(R.drawable.snowflake_icon);
        }else if(mWeatherInfomation.get(0).getWeather_Name()=="비" || mWeatherInfomation.get(0).getWeather_Name()=="강한 비"|| mWeatherInfomation.get(0).getWeather_Name() == "가벼운 비"){
            mainWeather.setImageResource(R.drawable.rain_cloud_icon);
        }else  if(mWeatherInfomation.get(0).getWeather_Name()=="구름 조금"||mWeatherInfomation.get(0).getWeather_Name()=="조각 구름"){
            mainWeather.setImageResource(R.drawable.sun_cloudy_icon);
        }else{
            mainWeather.setImageResource(R.drawable.clouds_weather_cloud_icon);
            //mainWeather.setImageResource(R.drawable.snowflake_icon);
        }
    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case THREAD_HANDLER_SUCCESS_INFO :
                    mForeCast.getmWeather();
                    mWeatherData = mForeCast.getmWeather();

                    if(mWeatherData.size() ==0)
                        mainWeatherText.setText("데이터가 없습니다");

                    DataToInformation(); // 자료 클래스로 저장,
                    String data = "";

                    DataChangedToKorean();
                    data = PrintValue();

                    //data = data + PrintValue();
                    MainImageIcon();
                    mainWeatherText.setText(data);
                    init();

                    clothes();
                    break;
                default:
                    break;
            }
        }


    };


    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitude = location.getAltitude();

            txtResult.setText("위치정보 : " + provider + "\n" +
                    "위도 : " + longitude + "\n" +
                    "경도 : " + latitude + "\n" +
                    "고도  : " + altitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    @Override
    public void onRefresh() {
        //새로 고침 코드
        updateLayoutView();
        //새로 고침 완
        swipeRefreshLayout.setRefreshing(false);

    }

    public void updateLayoutView(){
        Initialize();
        //clothes();
    }

    // 가중치 랜덤
    public static <E> E getWeightedRandom(Map<E, Double> weights, Random random) {
        E result = null;
        double bestValue = Double.MAX_VALUE;

        for (E element : weights.keySet()) {
            double value = -Math.log(random.nextDouble()) / weights.get(element);
            if (value < bestValue) {
                bestValue = value;
                result = element;
            }
        }
        return result;
    }

    public void clothes() {
        //String Temp = mWeatherInfomation.get(0).getTemp_Max();

        Double temp = Temp;
        //System.out.println(temp);

        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        int gender = sharedPreferences.getInt("gender",1);

        // 28도 일때
        if (temp >= 28) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("가디건", 5D);
                o.put("없음", 95D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "가디건":
                        outer.setImageResource(R.drawable.cardigan1_icon);
                        outer.setTag("가디건");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("폴로", 20D);
                t.put("반팔", 80D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "긴팔":
                        top.setImageResource(R.drawable.polo_icon);
                        top.setTag("긴팔티");
                        break;
                    default:
                        top.setImageResource(R.drawable.shortsleeve2_icon);
                        top.setTag("반팔티");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 19D);
                p.put("슬랙스", 23D);
                p.put("면바지", 23D);
                p.put("조거팬츠", 8D);
                p.put("반바지", 27D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    case "조거팬츠":
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                    default:
                        pants.setImageResource(R.drawable.shortpants1_icon);
                        pants.setTag("반바지");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("볼레로", 5D);
                o.put("없음", 95D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "볼레로":
                        outer.setImageResource(R.drawable.bolero_icon);
                        outer.setTag("볼레로");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 10D);
                w.put("투피스", 90D);
                Random rand9 = new Random();
                if ("원피스".equals(getWeightedRandom(w, rand9))) {
                    // 원피스
                    Map<String, Double> n = new HashMap<String, Double>();
                    n.put("콕", 30D);
                    n.put("쉬폰", 50D);
                    n.put("쉬폰2", 20D);
                    Random rand10 = new Random();
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(n, rand10)) {
                        case "콕":
                            dress.setImageResource(R.drawable.cocktaildress_icon);
                            dress.setTag("콕테일드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        case "쉬폰":
                            dress.setImageResource(R.drawable.chiffondress_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        default:
                            dress.setImageResource(R.drawable.dress3_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                    }

                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("민소매", 5D);
                    t.put("반팔", 95D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "민소매":
                            top.setImageResource(R.drawable.sleeveless_icon);
                            top.setTag("민소매");
                            break;
                        default:
                            top.setImageResource(R.drawable.shortsleeve2_icon);
                            top.setTag("반팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 20D);
                    p.put("슬랙스", 20D);
                    p.put("면바지", 20D);
                    p.put("조거팬츠", 5D);
                    p.put("반바지", 35D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        case "조거팬츠":
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                        default:
                            pants.setImageResource(R.drawable.shortpants3_icon);
                            pants.setTag("반바지");
                            break;
                    }
                }
            }
        }
        // 23 ~ 27도
        else if (temp >= 23) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("바람막이", 10D);
                o.put("가디건", 10D);
                o.put("없음", 80D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "바람막이":
                        outer.setImageResource(R.drawable.windbreak_icon);
                        outer.setTag("바람막이");
                        break;
                    case "가디건":
                        outer.setImageResource(R.drawable.cardigan3_icon);
                        outer.setTag("가디건");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("폴로", 20D);
                t.put("맨투맨", 5D);
                t.put("긴팔", 15D);
                t.put("반팔", 60D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "폴로":
                        top.setImageResource(R.drawable.polo_icon);
                        top.setTag("폴로");
                        break;
                    case "맨투맨":
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                    case "긴팔":
                        top.setImageResource(R.drawable.longsleeve_icon);
                        top.setTag("긴팔티");
                        break;
                    default:
                        top.setImageResource(R.drawable.shortsleeve2_icon);
                        top.setTag("반팔티");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 20D);
                p.put("슬랙스", 20D);
                p.put("면바지", 32D);
                p.put("조거팬츠", 10D);
                p.put("반바지", 18D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    case "조거팬츠":
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                    default:
                        pants.setImageResource(R.drawable.shortpants1_icon);
                        pants.setTag("반바지");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("볼레로", 20D);
                o.put("없음", 80D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "볼레로":
                        outer.setImageResource(R.drawable.bolero_icon);
                        outer.setTag("볼레로");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 10D);
                w.put("투피스", 90D);
                Random rand9 = new Random();
                if (getWeightedRandom(w, rand9).equals("원피스")) {
                    // 원피스
                    Map<String, Double> n = new HashMap<String, Double>();
                    n.put("콕", 30D);
                    n.put("쉬폰", 50D);
                    n.put("쉬폰2", 20D);
                    Random rand10 = new Random();
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(n, rand10)) {
                        case "콕":
                            dress.setImageResource(R.drawable.cocktaildress_icon);
                            dress.setTag("콕테일원피스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        case "쉬폰":
                            dress.setImageResource(R.drawable.chiffondress_icon);
                            dress.setTag("쉬폰원피스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        default:
                            dress.setImageResource(R.drawable.dress3_icon);
                            dress.setTag("쉬폰원피스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                    }
                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("민소매", 40D);
                    t.put("반팔", 60D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "민소매":
                            top.setImageResource(R.drawable.sleeveless_icon);
                            top.setTag("민소매");
                            break;
                        default:
                            top.setImageResource(R.drawable.shortsleeve2_icon);
                            top.setTag("반팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 20D);
                    p.put("슬랙스", 20D);
                    p.put("면바지", 15D);
                    p.put("조거팬츠", 5D);
                    p.put("반바지", 40D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        case "조거팬츠":
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                        default:
                            pants.setImageResource(R.drawable.shortpants1_icon);
                            pants.setTag("반바지");
                            break;
                    }
                }
            }
        }

        // 20 ~ 22도
        else if (temp >= 20) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("자켓", 30D);
                o.put("바람막이", 15D);
                o.put("가디건", 45D);
                o.put("없음", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket4_icon);
                        outer.setTag("자켓");
                        break;
                    case "바람막이":
                        outer.setImageResource(R.drawable.windbreak_icon);
                        outer.setTag("바람막이");
                        break;
                    case "가디건":
                        outer.setImageResource(R.drawable.cardigan3_icon);
                        outer.setTag("가디건");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("폴로", 20D);
                t.put("맨투맨", 20D);
                t.put("긴팔", 40D);
                t.put("반팔", 20D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "폴로":
                        top.setImageResource(R.drawable.polo_icon);
                        top.setTag("폴로");
                    case "맨투맨":
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                    case "긴팔":
                        top.setImageResource(R.drawable.longsleeve_icon);
                        top.setTag("긴팔티");
                        break;
                    default:
                        top.setImageResource(R.drawable.shortsleeve2_icon);
                        top.setTag("반팔티");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 29D);
                p.put("슬랙스", 26D);
                p.put("면바지", 24D);
                p.put("조거팬츠", 9D);
                p.put("반바지", 12D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    case "조거팬츠":
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                    default:
                        pants.setImageResource(R.drawable.shortpants1_icon);
                        pants.setTag("반바지");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("가디건", 90D);
                o.put("없음", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "가디건":
                        outer.setImageResource(R.drawable.cardigan4_icon);
                        outer.setTag("가디건");
                        break;
                    default:
                        outer.setImageResource(0);
                        outer.setTag(0);
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 10D);
                w.put("투피스", 90D);
                Random rand9 = new Random();
                if (getWeightedRandom(w, rand9).equals("원피스")) {
                    // 원피스
                    Map<String, Double> n = new HashMap<String, Double>();
                    n.put("쉬폰", 40D);
                    n.put("쉬폰1", 30D);
                    n.put("쉬폰2", 30D);
                    Random rand10 = new Random();
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(n, rand10)) {
                        case "쉬폰":
                            dress.setImageResource(R.drawable.dress2_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        case "쉬폰1":
                            dress.setImageResource(R.drawable.chiffondress_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        default:
                            dress.setImageResource(R.drawable.dress3_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                    }
                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("민소매", 20D);
                    t.put("긴팔", 40D);
                    t.put("반팔", 40D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "민소매":
                            top.setImageResource(R.drawable.sleeveless_icon);
                            top.setTag("민소매");
                        case "긴팔":
                            top.setImageResource(R.drawable.longsleeve_icon);
                            top.setTag("긴팔티");
                            break;
                        default:
                            top.setImageResource(R.drawable.shortsleeve2_icon);
                            top.setTag("반팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 25D);
                    p.put("슬랙스", 25D);
                    p.put("면바지", 15D);
                    p.put("조거팬츠", 5D);
                    p.put("반바지", 30D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        case "조거팬츠":
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                        default:
                            pants.setImageResource(R.drawable.shortpants3_icon);
                            pants.setTag("반바지");
                            break;
                    }
                }
            }
        }
        // 17 ~ 19도
        else if (temp >= 17) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("자켓", 50D);
                o.put("바람막이", 15D);
                o.put("가디건", 35D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket4_icon);
                        outer.setTag("자켓");
                        break;
                    case "바람막이":
                        outer.setImageResource(R.drawable.windbreak_icon);
                        outer.setTag("바람막이");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan3_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 20D);
                t.put("후드티", 20D);
                t.put("맨투맨", 20D);
                t.put("긴팔", 30D);
                t.put("폴로", 5D);
                t.put("반팔", 5D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                    case "맨투맨":
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                    case "긴팔":
                        top.setImageResource(R.drawable.longsleeve_icon);
                        top.setTag("긴팔티");
                        break;
                    case "폴로":
                        top.setImageResource(R.drawable.polo_icon);
                        top.setTag("폴로");
                        break;
                    default:
                        top.setImageResource(R.drawable.shortsleeve2_icon);
                        top.setTag("반팔티");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 31D);
                p.put("슬랙스", 30D);
                p.put("면바지", 24D);
                p.put("조거팬츠", 11D);
                p.put("반바지", 4D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    case "조거팬츠":
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                    default:
                        pants.setImageResource(R.drawable.shortpants1_icon);
                        pants.setTag("반바지");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("자켓", 60D);
                o.put("가디건", 40D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket2_icon);
                        outer.setTag("자켓");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan4_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 10D);
                w.put("투피스", 90D);
                Random rand9 = new Random();
                if (getWeightedRandom(w, rand9).equals("원피스")) {
                    // 원피스
                    Map<String, Double> n = new HashMap<String, Double>();
                    n.put("쉬폰", 40D);
                    n.put("쉬폰1", 30D);
                    n.put("울", 40D);
                    Random rand10 = new Random();
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(n, rand10)) {
                        case "쉬폰":
                            dress.setImageResource(R.drawable.dress2_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        case "쉬폰1":
                            dress.setImageResource(R.drawable.chiffondress_icon);
                            dress.setTag("쉬폰드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                        default:
                            dress.setImageResource(R.drawable.wooldress_icon);
                            dress.setTag("울드레스");
                            pants.setImageResource(0);
                            pants.setTag(0);
                            break;
                    }
                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("니트", 15D);
                    t.put("후드티", 20D);
                    t.put("맨투맨", 20D);
                    t.put("긴팔", 30D);
                    t.put("반팔", 5D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "니트":
                            top.setImageResource(R.drawable.sweater_icon);
                            top.setTag("니트");
                            break;
                        case "후드티":
                            top.setImageResource(R.drawable.hood1_icon);
                            top.setTag("후드티");
                        case "맨투맨":
                            top.setImageResource(R.drawable.mtm_icon);
                            top.setTag("맨투맨");
                            break;
                        case "긴팔":
                            top.setImageResource(R.drawable.longsleeve_icon);
                            top.setTag("긴팔티");
                            break;
                        default:
                            top.setImageResource(R.drawable.shortsleeve2_icon);
                            top.setTag("반팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 25D);
                    p.put("슬랙스", 25D);
                    p.put("면바지", 10D);
                    p.put("조거팬츠", 10D);
                    p.put("반바지", 20D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        case "조거팬츠":
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                        default:
                            pants.setImageResource(R.drawable.shortpants3_icon);
                            pants.setTag("반바지");
                            break;
                    }
                }
            }
        }
        // 12 ~ 16도
        else if (temp >= 12) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("야상", 10D);
                o.put("자켓", 50D);
                o.put("바람막이", 15D);
                o.put("가디건", 25D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket4_icon);
                        outer.setTag("자켓");
                        break;
                    case "바람막이":
                        outer.setImageResource(R.drawable.windbreak_icon);
                        outer.setTag("바람막이");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan3_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 35D);
                t.put("후드티", 20D);
                t.put("맨투맨", 25D);
                t.put("긴팔", 18D);
                t.put("폴로", 2D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater2_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                    case "맨투맨":
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                    case "긴팔":
                        top.setImageResource(R.drawable.longsleeve_icon);
                        top.setTag("긴팔티");
                        break;
                    default:
                        top.setImageResource(R.drawable.polo_icon);
                        top.setTag("폴로");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 25D);
                p.put("조거팬츠", 15D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("야상", 20D);
                o.put("자켓", 50D);
                o.put("가디건", 30D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket2_icon);
                        outer.setTag("자켓");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan4_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 5D);
                w.put("투피스", 95D);
                Random rand9 = new Random();
                if (getWeightedRandom(w, rand9).equals("원피스")) {
                    // 원피스
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    dress.setImageResource(R.drawable.wooldress_icon);
                    dress.setTag("울드레스");
                    pants.setImageResource(0);
                    pants.setTag(0);
                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("니트", 40D);
                    t.put("후드티", 25D);
                    t.put("맨투맨", 25D);
                    t.put("긴팔", 10D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "니트":
                            top.setImageResource(R.drawable.sweater2_icon);
                            top.setTag("니트");
                            break;
                        case "후드티":
                            top.setImageResource(R.drawable.hood1_icon);
                            top.setTag("후드티");
                        case "맨투맨":
                            top.setImageResource(R.drawable.mtm_icon);
                            top.setTag("맨투맨");
                            break;
                        default:
                            top.setImageResource(R.drawable.longsleeve_icon);
                            top.setTag("긴팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 30D);
                    p.put("슬랙스", 30D);
                    p.put("면바지", 30D);
                    p.put("조거팬츠", 10D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        default:
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                    }
                }
            }
        }
        // 9 ~ 11도
        else if (temp >= 9) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("코트", 30D);
                o.put("야상", 30D);
                o.put("자켓", 20D);
                o.put("바람막이", 10D);
                o.put("가디건", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("코트");
                        break;
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket4_icon);
                        outer.setTag("자켓");
                        break;
                    case "바람막이":
                        outer.setImageResource(R.drawable.windbreak_icon);
                        outer.setTag("바람막이");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan3_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 50D);
                t.put("후드티", 20D);
                t.put("맨투맨", 20D);
                t.put("긴팔", 10D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater2_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                        break;
                    case "맨투맨":
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                    default:
                        top.setImageResource(R.drawable.longsleeve_icon);
                        top.setTag("긴팔티");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 25D);
                p.put("조거팬츠", 15D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("코트", 20D);
                o.put("야상", 30D);
                o.put("자켓", 40D);
                o.put("가디건", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                    case "자켓":
                        outer.setImageResource(R.drawable.jacket2_icon);
                        outer.setTag("자켓");
                        break;
                    default:
                        outer.setImageResource(R.drawable.cardigan4_icon);
                        outer.setTag("가디건");
                        break;
                }

                // 원피스 vs 투피스
                Map<String, Double> w = new HashMap<String, Double>();
                w.put("원피스", 5D);
                w.put("투피스", 95D);
                Random rand9 = new Random();
                if (getWeightedRandom(w, rand9).equals("원피스")) {
                    // 원피스
                    ImageView dress = (ImageView) findViewById(R.id.Cloth2);
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    dress.setImageResource(R.drawable.wooldress_icon);
                    dress.setTag("울드레스");
                    pants.setImageResource(0);
                    pants.setTag(0);
                }
                // 투피스
                else {
                    // 상의
                    Map<String, Double> t = new HashMap<String, Double>();
                    t.put("니트", 35D);
                    t.put("후드티", 25D);
                    t.put("맨투맨", 30D);
                    t.put("긴팔", 10D);
                    Random rand1 = new Random();
                    //System.out.println(getWeightedRandom(t, rand1));
                    ImageView top = (ImageView) findViewById(R.id.Cloth2);
                    switch (getWeightedRandom(t, rand1)) {
                        case "니트":
                            top.setImageResource(R.drawable.sweater2_icon);
                            top.setTag("니트");
                            break;
                        case "후드티":
                            top.setImageResource(R.drawable.hood1_icon);
                            top.setTag("후드티");
                            break;
                        case "맨투맨":
                            top.setImageResource(R.drawable.mtm_icon);
                            top.setTag("맨투맨");
                            break;
                        default:
                            top.setImageResource(R.drawable.longsleeve_icon);
                            top.setTag("긴팔티");
                            break;
                    }

                    // 하의
                    Map<String, Double> p = new HashMap<String, Double>();
                    p.put("청바지", 40D);
                    p.put("슬랙스", 30D);
                    p.put("면바지", 20D);
                    p.put("조거팬츠", 10D);
                    Random rand2 = new Random();
                    //System.out.println(getWeightedRandom(t, rand2));
                    ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                    switch (getWeightedRandom(p, rand2)) {
                        case "청바지":
                            pants.setImageResource(R.drawable.bluejeans1_icon);
                            pants.setTag("청바지");
                            break;
                        case "슬랙스":
                            pants.setImageResource(R.drawable.slacks1_icon);
                            pants.setTag("슬랙스");
                            break;
                        case "면바지":
                            pants.setImageResource(R.drawable.cottonpants_icon);
                            pants.setTag("면바지");
                            break;
                        default:
                            pants.setImageResource(R.drawable.jogger1_icon);
                            pants.setTag("조거팬츠");
                            break;
                    }
                }
            }
        }
        // 5 ~ 8도
        else if (temp >= 5) {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("패딩", 35D);
                o.put("코트", 45D);
                o.put("야상", 15D);
                o.put("자켓", 5D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "패딩":
                        outer.setImageResource(R.drawable.padding3_icon);
                        outer.setTag("패딩");
                        break;
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                    default:
                        outer.setImageResource(R.drawable.jacket4_icon);
                        outer.setTag("자켓");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 50D);
                t.put("후드티", 30D);
                t.put("맨투맨", 20D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater2_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                    default:
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 20D);
                p.put("조거팬츠", 20D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("패딩", 30D);
                o.put("코트", 40D);
                o.put("야상", 15D);
                o.put("자켓", 15D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "패딩":
                        outer.setImageResource(R.drawable.padding1_icon);
                        outer.setTag("패딩");
                        break;
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    case "야상":
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                    default:
                        outer.setImageResource(R.drawable.jacket3_icon);
                        outer.setTag("자켓");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 40D);
                t.put("후드티", 30D);
                t.put("맨투맨", 30D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater2_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                        break;
                    default:
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 25D);
                p.put("조거팬츠", 15D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans1_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks1_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
        }
        // 4도 이하
        else {
            // 남자
            if (gender == 1) {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("패딩", 45D);
                o.put("코트", 45D);
                o.put("야상", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "패딩":
                        outer.setImageResource(R.drawable.padding4_icon);
                        outer.setTag("패딩");
                        break;
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    default:
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 50D);
                t.put("후드티", 25D);
                t.put("맨투맨", 25D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater2_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                    default:
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 20D);
                p.put("조거팬츠", 20D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans2_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks3_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
            // 여자
            else {
                // 아우터
                Map<String, Double> o = new HashMap<String, Double>();
                o.put("패딩", 45D);
                o.put("코트", 45D);
                o.put("야상", 10D);
                Random rand0 = new Random();
                //System.out.println(getWeightedRandom(o, rand0));
                ImageView outer = (ImageView) findViewById(R.id.Cloth1);
                switch (getWeightedRandom(o, rand0)) {
                    case "패딩":
                        outer.setImageResource(R.drawable.padding4_icon);
                        outer.setTag("패딩");
                        break;
                    case "코트":
                        outer.setImageResource(R.drawable.coat4_icon);
                        outer.setTag("코트");
                        break;
                    default:
                        outer.setImageResource(R.drawable.fieldjacket_icon);
                        outer.setTag("야상");
                        break;
                }

                // 상의
                Map<String, Double> t = new HashMap<String, Double>();
                t.put("니트", 50D);
                t.put("후드티", 25D);
                t.put("맨투맨", 25D);
                Random rand1 = new Random();
                //System.out.println(getWeightedRandom(t, rand1));
                ImageView top = (ImageView) findViewById(R.id.Cloth2);
                switch (getWeightedRandom(t, rand1)) {
                    case "니트":
                        top.setImageResource(R.drawable.sweater_icon);
                        top.setTag("니트");
                        break;
                    case "후드티":
                        top.setImageResource(R.drawable.hood1_icon);
                        top.setTag("후드티");
                    default:
                        top.setImageResource(R.drawable.mtm_icon);
                        top.setTag("맨투맨");
                        break;
                }

                // 하의
                Map<String, Double> p = new HashMap<String, Double>();
                p.put("청바지", 30D);
                p.put("슬랙스", 30D);
                p.put("면바지", 20D);
                p.put("조거팬츠", 20D);
                Random rand2 = new Random();
                //System.out.println(getWeightedRandom(t, rand2));
                ImageView pants = (ImageView) findViewById(R.id.Cloth3);
                switch (getWeightedRandom(p, rand2)) {
                    case "청바지":
                        pants.setImageResource(R.drawable.bluejeans1_icon);
                        pants.setTag("청바지");
                        break;
                    case "슬랙스":
                        pants.setImageResource(R.drawable.slacks1_icon);
                        pants.setTag("슬랙스");
                        break;
                    case "면바지":
                        pants.setImageResource(R.drawable.cottonpants_icon);
                        pants.setTag("면바지");
                        break;
                    default:
                        pants.setImageResource(R.drawable.jogger1_icon);
                        pants.setTag("조거팬츠");
                        break;
                }
            }
        }
    }
}