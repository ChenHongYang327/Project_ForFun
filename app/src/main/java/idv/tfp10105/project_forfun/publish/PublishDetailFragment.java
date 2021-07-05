package idv.tfp10105.project_forfun.publish;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import idv.tfp10105.project_forfun.MainActivity;
import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.CityAreaUtil;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.Gender;
import idv.tfp10105.project_forfun.common.HouseType;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Area;
import idv.tfp10105.project_forfun.common.bean.City;
import idv.tfp10105.project_forfun.common.bean.Publish;

public class PublishDetailFragment extends Fragment {
    private MainActivity activity;
    private Gson gson;
    private Geocoder geocoder;
    private FirebaseStorage storage;

    // UI
    // 刊登相關
    private Banner<Bitmap, BannerImageAdapter<Bitmap>> banner;
    private TextView publishDetailTitle, publishDetailRent, publishDetailArea, publishDetailSquare, publishDetailGender, publishDetailType, publishDetailDeposit, publishDetailInfo;
    private TextView publishDetailFurnished0, publishDetailFurnished1, publishDetailFurnished2, publishDetailFurnished3, publishDetailFurnished4, publishDetailFurnished5, publishDetailFurnished6, publishDetailFurnished7, publishDetailFurnished8;
    private Button publishDetailBtnCall, publishDetailBtnAppoint;
    private MapView publishDetailMap;

    // 房東相關

    // 評價相關

    private Publish publish;
    private List<Bitmap> publishImg;
    private List<City> cityList;
    private List<Area> areaList;
    private TextView[] furnishedArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        gson = new Gson();
        geocoder = new Geocoder(activity);
        storage = FirebaseStorage.getInstance();

        publishImg = new ArrayList<>();
        cityList = CityAreaUtil.getInstance().getCityList();
        areaList = CityAreaUtil.getInstance().getAreaList();
        furnishedArray = new TextView[9];

        return inflater.inflate(R.layout.fragment_publish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int publishId = getArguments() != null ? getArguments().getInt("publishId") : 0;
//        Log.d("home", "publishId = " + publishId);
        publish = getPublishDataById(publishId);
//        Log.d("home", "publish = " + gson.toJson(publish));

        banner = view.findViewById(R.id.bannerPublishDetail);

        publishDetailTitle = view.findViewById(R.id.publishDetailTitle);
        publishDetailRent = view.findViewById(R.id.publishDetailRent);
        publishDetailArea = view.findViewById(R.id.publishDetailArea);
        publishDetailSquare = view.findViewById(R.id.publishDetailSquare);
        publishDetailGender = view.findViewById(R.id.publishDetailGender);
        publishDetailType = view.findViewById(R.id.publishDetailType);
        publishDetailDeposit = view.findViewById(R.id.publishDetailDeposit);
        publishDetailInfo = view.findViewById(R.id.publishDetailInfo);

        furnishedArray[0] = publishDetailFurnished0 = view.findViewById(R.id.publishDetailFurnished0);
        furnishedArray[1] = publishDetailFurnished1 = view.findViewById(R.id.publishDetailFurnished1);
        furnishedArray[2] = publishDetailFurnished2 = view.findViewById(R.id.publishDetailFurnished2);
        furnishedArray[3] = publishDetailFurnished3 = view.findViewById(R.id.publishDetailFurnished3);
        furnishedArray[4] = publishDetailFurnished4 = view.findViewById(R.id.publishDetailFurnished4);
        furnishedArray[5] = publishDetailFurnished5 = view.findViewById(R.id.publishDetailFurnished5);
        furnishedArray[6] = publishDetailFurnished6 = view.findViewById(R.id.publishDetailFurnished6);
        furnishedArray[7] = publishDetailFurnished7 = view.findViewById(R.id.publishDetailFurnished7);
        furnishedArray[8] = publishDetailFurnished8 = view.findViewById(R.id.publishDetailFurnished8);

        publishDetailBtnCall = view.findViewById(R.id.publishDetailBtnCall);
        publishDetailBtnAppoint = view.findViewById(R.id.publishDetailBtnAppoint);


        publishDetailMap = view.findViewById(R.id.publishDetailMap);

        setPublishData(publish);
        handleButton();
    }



    private Publish getPublishDataById(int publishId) {
        Publish publish = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/getPublishData";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getByPublishId");
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            String publishJson = response.get("publish").getAsString();
            publish = gson.fromJson(publishJson, Publish.class);
        }

        return publish;
    }

    private void setPublishData(Publish publish) {
        // 圖片輪播
        getImage(publish.getPublishImg1());
        getImage(publish.getPublishImg2());
        getImage(publish.getPublishImg3());

        // 基本資料
        publishDetailTitle.setText(publish.getTitle());
        publishDetailRent.setText(publish.getRent() + "/月");
        publishDetailSquare.setText(publish.getSquare() + "坪");
        publishDetailDeposit.setText(publish.getDeposit() + "個月");
        publishDetailInfo.setText(publish.getPublishInfo());

        String cityName = "";
        for (City city : cityList) {
            if (city.getCityId() == publish.getCityId()) {
                cityName = city.getCityName();
                break;
            }
        }
        String areaName = "";
        for (Area area : areaList) {
            if (area.getAreaId() == publish.getAreaId()) {
                areaName = area.getAreaName();
                break;
            }
        }
        publishDetailArea.setText(cityName + areaName);

        String gender = "";
        switch (Gender.toEnum(publish.getGender())){
            case BOTH:
                gender = "無限制";
                break;
            case MALE:
                gender = "限男性";
                break;
            case FEMALE:
                gender = "限女性";
                break;
        }
        publishDetailGender.setText(gender);

        String type = "";
        switch (HouseType.toEnum(publish.getType())) {
            case WITH_BATH:
                type = "套房";
                break;
            case NO_BATH:
                type = "雅房";
                break;
        }
        publishDetailType.setText(type);

        String[] purnished = publish.getFurnished().split("\\|");
        for (int i = 0; i < purnished.length; i++) {
            furnishedArray[i].setEnabled("1".equals(purnished[i]));
        }
    }

    private void handleButton() {
        publishDetailBtnAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 把ID帶到預約頁面
                Bundle bundle = new Bundle();
                bundle.putInt("publishId", publish.getPublishId());

                Navigation.findNavController(v).navigate(R.id.action_publishDetailFragment_to_appointmentFragment, bundle);
            }
        });
    }

    //下載Firebase storage的照片
    public void getImage(final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //設定上限
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        publishImg.add(bitmap);

                        // 三張圖片都下載好後才進行初始化
                        if (publishImg.size() == 3) {
                            banner.addBannerLifecycleObserver(activity)
                                    .setIndicator(new CircleIndicator(activity))
                                    .setAdapter(new BannerImageAdapter<Bitmap>(publishImg) {
                                        @Override
                                        public void onBindView(BannerImageHolder holder, Bitmap data, int position, int size) {
                                            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                            holder.imageView.setImageBitmap(data);
                                        }
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Toast.makeText(activity, "圖片取得錯誤", Toast.LENGTH_SHORT).show();
                        Log.d("顯示Firebase取得圖片的錯誤", errorMessage);

                    }
                });

    }
}