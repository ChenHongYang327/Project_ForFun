package idv.tfp10105.project_forfun.publish;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import idv.tfp10105.project_forfun.MainActivity;
import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.CityAreaUtil;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.Gender;
import idv.tfp10105.project_forfun.common.HouseType;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Area;
import idv.tfp10105.project_forfun.common.bean.City;
import idv.tfp10105.project_forfun.common.bean.Favorite;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.Order;
import idv.tfp10105.project_forfun.common.bean.Publish;

public class PublishDetailFragment extends Fragment {
    private MainActivity activity;
    private Gson gson;
    private FirebaseStorage storage;
    private RatingAdapter ratingAdapter;
    private SharedPreferences sharedPreferences;

    // UI
    // ????????????
    private Banner<Bitmap, BannerImageAdapter<Bitmap>> banner;
    private TextView publishDetailTitle, publishDetailRent, publishDetailArea, publishDetailSquare, publishDetailGender, publishDetailType, publishDetailDeposit, publishDetailInfo;
    private TextView publishDetailFurnished0, publishDetailFurnished1, publishDetailFurnished2, publishDetailFurnished3, publishDetailFurnished4, publishDetailFurnished5, publishDetailFurnished6, publishDetailFurnished7, publishDetailFurnished8;
    private Button publishDetailBtnAppoint;
    private MapView publishDetailMap;
    private ImageView publishDetailLike;

    // ????????????
    private CircularImageView publishDetailHead;
    private TextView publishDetailOwnerName;
    private Button publishDetailBtnCall;

    // ????????????
    private TextView publishDetailRating;
    private RatingBar publishDetailRatingBar;
    private RecyclerView publishDetailRatingView;

    private int userId;
    private Publish publish;
    private List<Bitmap> publishImg;
    private List<City> cityList;
    private List<Area> areaList;
    private TextView[] furnishedArray;
    private Member owner;
    private List<Order> orderList;
    private Favorite favorite;
    private int appointmentId;

    // ????????????
    private GoogleMap googleMap;
    private LatLng userLatLng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        gson = new Gson();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);

        ratingAdapter = new RatingAdapter(new DiffUtil.ItemCallback<Order>() {
            @Override
            public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                return oldItem.getOrderId().intValue() == newItem.getOrderId().intValue();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                return oldItem.equals(newItem);
            }
        });

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
        userId = sharedPreferences.getInt("memberId",-1);

//        Log.d("home", "publishId = " + publishId);
        // ????????????
        publish = getPublishDataById(publishId);
//        Log.d("home", "publish = " + gson.toJson(publish));
        // ????????????
        owner = getMemberByOwnerId(publish.getOwnerId());
//        Log.d("home", "owner = " + gson.toJson(owner));
        // ????????????
        orderList = getOrdersByPublishId(publish.getPublishId());
        // ????????????
        favorite = getMyFavoriteByPublishId(userId, publish.getPublishId());
        // ????????????
        appointmentId = getMyAppointmentByPublishId(userId, publish.getPublishId());
//        Log.d("publish", "appointmentId = " + appointmentId);

        // ????????????
        banner = view.findViewById(R.id.bannerPublishDetail);
        publishDetailLike = view.findViewById(R.id.publishDetailLike);

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

        publishDetailBtnAppoint = view.findViewById(R.id.publishDetailBtnAppoint);

        publishDetailMap = view.findViewById(R.id.publishDetailMap);

        // ????????????
        publishDetailHead = view.findViewById(R.id.publishDetailHead);
        publishDetailOwnerName = view.findViewById(R.id.publishDetailOwnerName);
        publishDetailBtnCall = view.findViewById(R.id.publishDetailBtnCall);

        // ????????????
        publishDetailRating = view.findViewById(R.id.publishDetailRating);
        publishDetailRatingBar = view.findViewById(R.id.publishDetailRatingBar);
        publishDetailRatingView = view.findViewById(R.id.publishDetailRatingView);

        // ?????????????????????????????????????????????
        checkPositioning();

        // ???????????????
        publishDetailMap.onCreate(savedInstanceState);
        publishDetailMap.onStart();
        publishDetailMap.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            // ??????????????????????????????????????????
            this.googleMap.getUiSettings().setAllGesturesEnabled(false);

            // ???????????????marker???????????????
            this.googleMap.setOnMapClickListener(latLng -> goNavigation());
            this.googleMap.setOnMarkerClickListener(marker -> {
                goNavigation();
                return true;
            });

            LatLng latLng = new LatLng(publish.getLatitude(), publish.getLongitude());
            addMarker(latLng);
            moveCamera(latLng);
        });

        // ?????????????????????????????????????????????
        getUserLocation();

        // ??????????????????
        setPublishData(publish);
        // ??????????????????
        setOwnerData(owner);
        // ??????????????????
        setRatingData(orderList);
        // ??????????????????
        setFavoriteData(favorite);

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

    private Member getMemberByOwnerId (int ownerId) {
        Member member = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/memberCenterPersonalInformation";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getMember");
            request.addProperty("member_id", ownerId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));

            member = gson.fromJson(jsonResule, Member.class);
        }

        return  member;
    }

    private List<Order> getOrdersByPublishId(int publishId) {
        List<Order> orderList = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/getPublishData";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getOrderList");
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            String orderJson = response.get("orderList").getAsString();

            Type listOrder = new TypeToken<List<Order>>() {}.getType();
            orderList = gson.fromJson(orderJson, listOrder);

//            Log.d("publish", jsonResule);
        }

        return orderList;
    }

    private Favorite getMyFavoriteByPublishId (int userId, int publishId) {
        Favorite favorite = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/favoriteController";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getMyFavoriteByPublishId");
            request.addProperty("userId", userId);
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));
//            Log.d("publish", jsonResule);

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            String favoriteJson = response.get("favorite").getAsString();

            favorite = gson.fromJson(favoriteJson, Favorite.class);
        }

        return favorite;
    }

    private Favorite addMyFavorite (int userId, int publishId) {
        Favorite favorite = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/favoriteController";
            JsonObject request = new JsonObject();
            request.addProperty("action", "addMyFavorite");
            request.addProperty("userId", userId);
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));
//            Log.d("publish", jsonResule);

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            String favoriteJson = response.get("favorite").getAsString();

            favorite = gson.fromJson(favoriteJson, Favorite.class);
        }

        return favorite;
    }

    private boolean deleteMyFavorite (int favoriteId) {
        boolean result = false;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/favoriteController";
            JsonObject request = new JsonObject();
            request.addProperty("action", "remove");
            request.addProperty("removeId", favoriteId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));
//            Log.d("publish", jsonResule);

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            result = response.get("pass").getAsBoolean();
        }

        return result;
    }

    private int getMyAppointmentByPublishId(int userId, int publishId) {
        int appointmentId = 0;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/appointment";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getMyAppointmentByPublishId");
            request.addProperty("userId", userId);
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));
//            Log.d("publish", jsonResule);

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            appointmentId = Math.max(response.get("appointmentId").getAsInt(), 0);
        }

        return  appointmentId;
    }

    private void setPublishData(Publish publish) {
        // ????????????
        getBannerImage(publish.getPublishImg1());
        getBannerImage(publish.getPublishImg2());
        getBannerImage(publish.getPublishImg3());

        // ????????????
        publishDetailTitle.setText(publish.getTitle());
        publishDetailRent.setText(publish.getRent() + "/???");
        publishDetailSquare.setText(publish.getSquare() + "???");
        publishDetailDeposit.setText(publish.getDeposit() + "??????");
        publishDetailInfo.setText(publish.getPublishInfo());

        // ??????
        publishDetailLike.setOnClickListener(v -> {
            // ??????????????????
            int role = sharedPreferences.getInt("role", -1);
            if (role == 3) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("????????????");
                dialog.setMessage("?????????????????????");
                dialog.setPositiveButton("??????", null);

                Window window = dialog.show().getWindow();
                // ??????????????????
                Button btnOK = window.findViewById(android.R.id.button1);
                btnOK.setTextColor(getResources().getColor(R.color.black));

                return;
            }

            if (favorite == null) {
                favorite = addMyFavorite(userId, publish.getPublishId());
                publishDetailLike.setImageResource(R.drawable.icon_favorite);
                Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
            } else {
                if (deleteMyFavorite(favorite.getFavoriteId())) {
                    favorite = null;
                    publishDetailLike.setImageResource(R.drawable.icon_unfavorite);
                    Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                }

            }
        });

        String cityName = "";
        for (City city : cityList) {
            if (city.getCityId().equals(publish.getCityId())) {
                cityName = city.getCityName();
                break;
            }
        }
        String areaName = "";
        for (Area area : areaList) {
            if (area.getAreaId().equals(publish.getAreaId())) {
                areaName = area.getAreaName();
                break;
            }
        }
        publishDetailArea.setText(cityName + areaName);

        String gender = "";
        switch (Gender.toEnum(publish.getGender())){
            case BOTH:
                gender = "?????????";
                break;
            case MALE:
                gender = "?????????";
                break;
            case FEMALE:
                gender = "?????????";
                break;
        }
        publishDetailGender.setText(gender);

        String type = "";
        switch (HouseType.toEnum(publish.getType())) {
            case WITH_BATH:
                type = "??????";
                break;
            case NO_BATH:
                type = "??????";
                break;
        }
        publishDetailType.setText(type);

        String[] furnished = publish.getFurnished().split("\\|");
        for (int i = 0; i < furnished.length; i++) {
            furnishedArray[i].setEnabled("1".equals(furnished[i]));
        }
    }

    private void setOwnerData(Member owner) {
        String gender = "";
        switch (Gender.toEnum(owner.getGender())){
            case MALE:
                gender = "??????";
                break;
            case FEMALE:
                gender = "??????";
                break;
        }
        publishDetailOwnerName.setText(owner.getNameL() + gender);
        publishDetailBtnCall.setText("0" + owner.getPhone());

        // ????????????
        getImage(publishDetailHead, owner.getHeadshot());
        publishDetailHead.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("SelectUser", owner);
            Navigation.findNavController(v).navigate(R.id.personalSnapshotFragment, bundle);
        });
    }

    private void setRatingData(List<Order> orderList) {

        if (orderList.size() == 0) {
            // ?????????????????????
            publishDetailRating.setVisibility(View.GONE);
            publishDetailRatingBar.setVisibility(View.GONE);
            publishDetailRatingView.setVisibility(View.GONE);
            return;
        }
        publishDetailRating.setVisibility(View.VISIBLE);
        publishDetailRatingBar.setVisibility(View.VISIBLE);
        publishDetailRatingView.setVisibility(View.VISIBLE);

        // ??????????????????
        int sum = 0;
        for (Order order : orderList) {
            sum += order.getPublishStar();
        }
        int rating = sum / orderList.size();
        
        publishDetailRating.setText(String.format(Locale.TAIWAN, "????????????%d", rating));
        publishDetailRatingBar.setRating(rating);

        // ??????????????????
        publishDetailRatingView.setLayoutManager(new LinearLayoutManager(activity));
        publishDetailRatingView.setAdapter(ratingAdapter);

        ratingAdapter.submitList(new ArrayList<>(orderList));
    }

    private void setFavoriteData(Favorite favorite){
        publishDetailLike.setImageResource(favorite == null ? R.drawable.icon_unfavorite : R.drawable.icon_favorite);
    }

    private void handleButton() {
        publishDetailBtnCall.setOnClickListener(v -> {
            String phone = publishDetailBtnCall.getText().toString();
            if (phone.isEmpty()) {
                Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phone));
            if (isIntentAvailable(intent)) {
                startActivity(intent);
            }
        });

        publishDetailBtnAppoint.setText(appointmentId == 0 ? "????????????" : "????????????");
        publishDetailBtnAppoint.setOnClickListener(v -> {
            // ??????????????????
            int role = sharedPreferences.getInt("role", -1);
            if (role == 3) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("????????????");
                dialog.setMessage("?????????????????????");
                dialog.setPositiveButton("??????", (dialog1, which) -> {
                    // ??????????????????
                    Navigation.findNavController(v).navigate(R.id.registIntroductionFragment);
                });

                Window window = dialog.show().getWindow();
                // ??????????????????
                Button btnOK = window.findViewById(android.R.id.button1);
                btnOK.setTextColor(getResources().getColor(R.color.black));

                return;
            }

            if (userId == publish.getOwnerId()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("????????????");
                dialog.setMessage("???????????????????????????");
                dialog.setPositiveButton("??????", null);

                Window window = dialog.show().getWindow();
                // ??????????????????
                Button btnOK = window.findViewById(android.R.id.button1);
                btnOK.setTextColor(getResources().getColor(R.color.black));

                return;
            }

            // ???ID??????????????????
            Bundle bundle = new Bundle();
            bundle.putInt("publishId", publish.getPublishId());
            bundle.putInt("ApmtID", appointmentId);

            Navigation.findNavController(v).navigate(R.id.action_publishDetailFragment_to_appointmentFragment, bundle);
        });
    }

    //??????Firebase storage?????????
    public void getBannerImage(final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //????????????
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        publishImg.add(bitmap);

                        // ?????????????????????????????????????????????
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
                        Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                        Log.d("??????Firebase?????????????????????", errorMessage);

                    }
                });

    }

    //??????Firebase storage?????????
    public void getImage(final ImageView imageView, final String path) {
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //????????????
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                        Log.d("??????Firebase?????????????????????", errorMessage);

                    }
                });

    }

    // ?????????????????????????????????????????????
    private void checkPositioning() {
        // ????????????????????????
        LocationRequest locationRequest = LocationRequest.create();

        // ????????????????????????
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        // ?????????????????????
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);

        // ??????????????????????????????
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);

        // ??????????????????????????????????????????????????????????????????????????????
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity, 0);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
    }

    // ??????????????????
    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("marker")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));

        googleMap.addMarker(markerOptions);
    }

    // ???????????????
    private void moveCamera(LatLng latLng) {
        double lat = latLng.latitude;
        double lnt = latLng.longitude;

        // ??????CameraPosition???CameraUpdate
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lnt))
                .zoom(17.5f)
                .build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        googleMap.moveCamera(cameraUpdate);
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // ??????????????????
        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity);

        // ??????????????????
        Task<Location> task = fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken());

        task.addOnSuccessListener(location -> {
            if (location != null) {
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });
    }

    // ????????????????????????App
    private boolean isIntentAvailable (Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        return intent.resolveActivity(packageManager) != null;
    }

    // ??????google map APP ????????????
    private void goNavigation() {
        // intent?????????google map app
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.google.android.apps.maps");

        String uriString = String.format(Locale.TAIWAN,
                "https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f",
                userLatLng.latitude, userLatLng.longitude,
                publish.getLatitude(), publish.getLongitude());
        Uri uri = Uri.parse(uriString);
        intent.setData(uri);
        if (isIntentAvailable(intent)) {
            startActivity(intent);
        }
    }

    // ??????list
    private class RatingAdapter extends ListAdapter<Order, RatingAdapter.RatingViewHolder> {

        protected RatingAdapter(@NonNull DiffUtil.ItemCallback<Order> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.publish_detail_rating_itemview,parent,false);

            return new RatingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
            holder.onBind(getItem(position));
        }

        private class RatingViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPdCommentByPS;
            TextView tvPdCommentByPS;
            TextView tvPdCommentPS;
            TextView tvPdCommentTimePS;
            RatingBar ratingPdPS;

            public RatingViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPdCommentByPS = itemView.findViewById(R.id.ivPdCommentByPS);
                tvPdCommentByPS = itemView.findViewById(R.id.tvPdCommentByPS);

                tvPdCommentPS = itemView.findViewById(R.id.tvPdCommentPS);
                ratingPdPS = itemView.findViewById(R.id.ratingPdPS);
                tvPdCommentTimePS = itemView.findViewById(R.id.tvPdCommentTimePS);
            }

            public void onBind(Order order) {
                // ?????????????????????
                Member member = getMemberByOwnerId(order.getTenantId());
                getImage(ivPdCommentByPS, member.getHeadshot());

                String gender = "";
                switch (Gender.toEnum(member.getGender())){
                    case MALE:
                        gender = "??????";
                        break;
                    case FEMALE:
                        gender = "??????";
                        break;
                }
                tvPdCommentByPS.setText(member.getNameL() + gender);

                // ????????????
                tvPdCommentPS.setText(order.getPublishComment());
                ratingPdPS.setRating(order.getPublishStar());

                // ????????????
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.TAIWAN);
                tvPdCommentTimePS.setText(sdf.format(order.getCreateTime()));

                itemView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("SelectUser", member);
                    Navigation.findNavController(v).navigate(R.id.personalSnapshotFragment,bundle);
                });
            }
        }
    }
}