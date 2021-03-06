package idv.tfp10105.project_forfun.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tfp10105.project_forfun.MainActivity;
import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.CityAreaUtil;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Area;
import idv.tfp10105.project_forfun.common.bean.City;
import idv.tfp10105.project_forfun.common.bean.Favorite;
import idv.tfp10105.project_forfun.common.bean.Publish;
import idv.tfp10105.project_forfun.common.bean.PublishHome;

public class HomeFragment extends Fragment {
    private final double SEARCH_DISTANCE = 100.0;

    private MainActivity activity;
    private Gson gson;
    private Geocoder geocoder;
    private FirebaseStorage storage;
    private PublishAdapter publishAdapter;
    private SharedPreferences sharedPreferences;

    // UI??????
    private MapView mapView;
    private GoogleMap googleMap;
    private Button homeBtnDistanceSort, homeBtnRentSort, homeBtnSearch;
    private RecyclerView homePublishListView;
    private FrameLayout homeLoading;

    // ????????????
    private BottomSheetDialog bottomSheetDialog;
    private TextInputEditText editHomeSearchRentLower, editHomeSearchRentUpper;
    private Spinner spHomeSearchCity, spHomeSearchArea;
    private RadioGroup radioHomeSearchGender, radioHomeSearchType;
    private RadioButton radioHomeSearchGender0, radioHomeSearchGender1, radioHomeSearchGender2;
    private RadioButton radioHomeSearchType0, radioHomeSearchType1, radioHomeSearchType2;
    private Button btnHomeSearch;


    // ????????????
    private int userId;
    private List<PublishHome> publishHomeList;
    private List<City> cityList;
    private List<Area> areaList;
    private Map<Integer, List<Area>> areaMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ?????????????????????
        activity = (MainActivity) getActivity();
        gson = new Gson();
        geocoder = new Geocoder(activity);
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);

        cityList = CityAreaUtil.getInstance().getCityList();
        areaList = CityAreaUtil.getInstance().getAreaList();
        areaMap = CityAreaUtil.getInstance().getAreaMap();

        publishAdapter = new PublishAdapter(new DiffUtil.ItemCallback<PublishHome>() {
            @Override
            public boolean areItemsTheSame(@NonNull PublishHome oldItem, @NonNull PublishHome newItem) {
                return oldItem.getPublish().getPublishId().intValue() == newItem.getPublish().getPublishId().intValue();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PublishHome oldItem, @NonNull PublishHome newItem) {
                return oldItem.equals(newItem);
            }
        });

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = sharedPreferences.getInt("memberId",-1);

        // ??????
        homeBtnDistanceSort = view.findViewById(R.id.homeBtnDistanceSort);
        homeBtnRentSort = view.findViewById(R.id.homeBtnRentSort);
        homeBtnSearch = view.findViewById(R.id.homeBtnSearch);

        homeBtnDistanceSort.setOnClickListener(v -> sortByDistance(publishHomeList));

        homeBtnRentSort.setOnClickListener(v -> sortByRent(publishHomeList));

        homeBtnSearch.setOnClickListener(v -> bottomSheetDialog.show());

        // RecyclerView
        homePublishListView = view.findViewById(R.id.homePublishListView);
        homePublishListView.setLayoutManager(new LinearLayoutManager(activity));
        homePublishListView.setAdapter(publishAdapter);

        // ??????
        mapView = view.findViewById(R.id.homeMap);
        homeLoading = view.findViewById(R.id.homeLoading);
        // ??????loading??????
        homeLoading.setVisibility(View.VISIBLE);

        // ?????????????????????????????????????????????
        checkPositioning();

        // ???????????????
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            showMyLocation();
        });

        // 1. ?????????Loading....????????????????????????????????????????????????????????????thread
        // ?????????????????????activity???????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????hostdelay?
        // ProgressDialog ??? ProgressBar
//        while (googleMap == null) {
//            Log.d("home", "Loading....");
//        }
//
//        Log.d("home", "OK!!!");

        // 2. ??????????????????????????????????????????????????????????????????????????????????????????????????????ID
        // ?????????????????????????????????(?????????)??????????????????????????????????????????????????????????????????ID
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        for (City city : cityList) {
//            if ("???????????????".contains(city.getCityName())) {
//                city.getCityId();
//                break;
//            }
//        }

        // ????????????
        View bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_search,null);
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(bottomSheetView);

        // ????????????
        spHomeSearchCity = bottomSheetView.findViewById(R.id.spHomeSearchCity);
        spHomeSearchArea = bottomSheetView.findViewById(R.id.spHomeSearchArea);
        handleSpinner();

        // ????????????
        editHomeSearchRentLower = bottomSheetView.findViewById(R.id.editHomeSearchRentLower);
        editHomeSearchRentUpper = bottomSheetView.findViewById(R.id.editHomeSearchRentUpper);

        // ????????????
        radioHomeSearchGender = bottomSheetView.findViewById(R.id.radioHomeSearchGender);
        radioHomeSearchGender0 = bottomSheetView.findViewById(R.id.radioHomeSearchGender0);
        radioHomeSearchGender1 = bottomSheetView.findViewById(R.id.radioHomeSearchGender1);
        radioHomeSearchGender2 = bottomSheetView.findViewById(R.id.radioHomeSearchGender2);

        // ????????????
        radioHomeSearchType = bottomSheetView.findViewById(R.id.radioHomeSearchType);
        radioHomeSearchType0 = bottomSheetView.findViewById(R.id.radioHomeSearchType0);
        radioHomeSearchType1 = bottomSheetView.findViewById(R.id.radioHomeSearchType1);
        radioHomeSearchType2 = bottomSheetView.findViewById(R.id.radioHomeSearchType2);

        // ??????
        btnHomeSearch = bottomSheetView.findViewById(R.id.btnHomeSearch);
        handleButton();
    }

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

    // ?????????????????????????????? + ??????????????????????????????
    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // ??????????????????????????????
//        googleMap.setMyLocationEnabled(true);

        // ??????????????????
        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity);

        // ??????????????????
        Task<Location> task = fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken());

        task.addOnSuccessListener(location -> {
            if (location != null) {
                // ??????????????????
                googleMap.clear();

                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                // ??????????????????
                publishHomeList = getPublishHomeListNearby(getPublishData(), userLatLng) ;
                addPublishMarker(publishHomeList);
                publishAdapter.submitList(new ArrayList<>(publishHomeList));

                // ?????????????????????
                drawCircle(userLatLng);
                addMapPeople(userLatLng);
                moveCamera(userLatLng);

//                latLngToName(userLatLng.latitude, userLatLng.longitude);

                // ??????loading??????
                homeLoading.setVisibility(View.GONE);
            }
        });
    }

    // ??????
    private void drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)             // 2. ????????????
                .radius(SEARCH_DISTANCE)    // 3. ????????????(??????)
                .strokeWidth(3)             // 4. ????????????
                .strokeColor(Color.rgb(239, 119, 220))  // 5. ????????????
                .fillColor(Color.argb(170, 236, 211, 208));  // 6. ???????????????

        googleMap.addCircle(circleOptions);
    }

    // ??????????????????
    private void addMapPeople(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_man));

        googleMap.addMarker(markerOptions);
    }

    // ??????????????????
    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("marker")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));

        googleMap.addMarker(markerOptions);
    }

    // ??????????????????????????????
    private void addPublishMarker(List<PublishHome> publishHomeList) {
        for (PublishHome publishHome : publishHomeList) {
            addMarker(new LatLng(publishHome.getPublish().getLatitude(), publishHome.getPublish().getLongitude()));
        }
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

    //??????Firebase storage?????????
    public void getImage(final ImageView imageView, final String path) {
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

    // ??????????????????
    private List<Publish> getPublishData() {
        List<Publish> publishList = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/getPublishData";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getAll");

            String jsonIn = RemoteAccess.getJsonData(url, gson.toJson(request));

            JsonObject response = gson.fromJson(jsonIn, JsonObject.class);
            String publishJson = response.get("publishList").getAsString();
            Type listPublish = new TypeToken<List<Publish>>() {}.getType();
            publishList = gson.fromJson(publishJson, listPublish);

//            Log.d("publish", publishJson);
        }

        return publishList;
    }

    // ???????????????????????????
    private List<PublishHome> getPublishHomeListNearby(List<Publish> publishes, LatLng latLng) {
        List<PublishHome> publishList = new ArrayList<>();

        for (Publish publish : publishes) {
            // ????????????
            float[] distance = new float[1];
            Location.distanceBetween(latLng.latitude, latLng.longitude, publish.getLatitude(), publish.getLongitude(), distance);

            // ????????????????????????list???
            if (distance[0] < SEARCH_DISTANCE) {
                PublishHome publishHome = new PublishHome();
                publishHome.setPublish(publish);
                publishHome.setDistance(distance[0]);

                publishList.add(publishHome);
            }
        }

        return publishList;
    }

    // ??????????????????
    private List<PublishHome> getPublishHomeList(List<Publish> publishes, LatLng latLng) {
        List<PublishHome> publishList = new ArrayList<>();

        for (Publish publish : publishes) {
            // ????????????
            float[] distance = new float[1];
            Location.distanceBetween(latLng.latitude, latLng.longitude, publish.getLatitude(), publish.getLongitude(), distance);

            PublishHome publishHome = new PublishHome();
            publishHome.setPublish(publish);
            publishHome.setDistance(distance[0]);

            publishList.add(publishHome);
        }

        return publishList;
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
            Log.d("publish", jsonResule);

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
            Log.d("publish", jsonResule);

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
            Log.d("publish", jsonResule);

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            result = response.get("pass").getAsBoolean();
        }

        return result;
    }

    private class PublishAdapter extends ListAdapter<PublishHome, PublishAdapter.PublishViewHolder> {

        protected PublishAdapter(@NonNull DiffUtil.ItemCallback<PublishHome> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public PublishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.publish_home_itemview, parent, false);
            return new PublishViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PublishViewHolder holder, int position) {
            holder.onBind(getItem(position));
        }

        private class PublishViewHolder extends RecyclerView.ViewHolder {
            final private ImageView homePublishImg, homePublishLike;
            final private TextView homePublishName, homePublishArea, homePublishSquare, homePublishRent;

            private Favorite favorite;

            public PublishViewHolder(@NonNull View itemView) {
                super(itemView);

                homePublishImg = itemView.findViewById(R.id.homePublishImg);
                homePublishLike = itemView.findViewById(R.id.homePublishLike);
                homePublishName = itemView.findViewById(R.id.homePublishName);
                homePublishArea = itemView.findViewById(R.id.homePublishArea);
                homePublishSquare = itemView.findViewById(R.id.homePublishSquare);
                homePublishRent = itemView.findViewById(R.id.homePublishRent);
            }

            void onBind(PublishHome publishHome) {
                String cityName = "";
                for (City city : cityList) {
                    if (city.getCityId().equals(publishHome.getPublish().getCityId())) {
                        cityName = city.getCityName();
                        break;
                    }
                }

                String areaName = "";
                for (Area area : areaList) {
                    if (area.getAreaId().equals(publishHome.getPublish().getAreaId())) {
                        areaName = area.getAreaName();
                        break;
                    }
                }

                homePublishName.setText(publishHome.getPublish().getTitle());
                homePublishArea.setText(cityName + areaName);
                homePublishSquare.setText(publishHome.getPublish().getSquare() + "???");
                homePublishRent.setText(publishHome.getPublish().getRent() + "/???");

                getImage(homePublishImg, publishHome.getPublish().getTitleImg());

                itemView.setOnClickListener(v -> {
                    // ???ID??????????????????
                    Bundle bundle = new Bundle();
                    bundle.putInt("publishId", publishHome.getPublish().getPublishId());

                    Navigation.findNavController(v).navigate(R.id.publishDetailFragment, bundle);
                });

                // ??????????????????
                favorite = getMyFavoriteByPublishId(userId, publishHome.getPublish().getPublishId());

                homePublishLike.setImageResource(favorite == null ? R.drawable.icon_unfavorite : R.drawable.icon_favorite);
                homePublishLike.setOnClickListener(v -> {
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
                        favorite = addMyFavorite(userId, publishHome.getPublish().getPublishId());
                        homePublishLike.setImageResource(R.drawable.icon_favorite);
                        Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        if (deleteMyFavorite(favorite.getFavoriteId())) {
                            favorite = null;
                            homePublishLike.setImageResource(R.drawable.icon_unfavorite);
                            Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }
    }

    private void sortByDistance(List<PublishHome> publishHomeList) {
        Collections.sort(publishHomeList, (o1, o2) -> (int)(o1.getDistance() - o2.getDistance()));

        publishAdapter.submitList(new ArrayList<>(publishHomeList), () -> homePublishListView.scrollToPosition(0));
    }

    private void sortByRent(List<PublishHome> publishHomeList) {
        Collections.sort(publishHomeList, (o1, o2) -> o1.getPublish().getRent() - o2.getPublish().getRent());

        publishAdapter.submitList(new ArrayList<>(publishHomeList), new Runnable() {
            // ?????????????????????????????????Runnable
            @Override
            public void run() {
                homePublishListView.scrollToPosition(0);
            }
        });
    }

    /*
     * ??????????????????
     */
    private void handleSpinner() {
        // ??????
        List<String> cityNames = new ArrayList<>();
        for (City city : cityList) {
            cityNames.add(city.getCityName());
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, cityNames);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spHomeSearchCity.setAdapter(cityAdapter);
        spHomeSearchCity.setSelection(0, true);
        spHomeSearchCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // ??????????????????????????????
                List<String> areaNames = new ArrayList<>();
                for (Area area : areaMap.get(cityList.get(position).getCityId())) {
                    areaNames.add(area.getAreaName());
                }

                ArrayAdapter<String> areaAdapter = (ArrayAdapter<String>) spHomeSearchArea.getAdapter();
                areaAdapter.clear();
                areaAdapter.addAll(areaNames);
                areaAdapter.notifyDataSetChanged();
                spHomeSearchArea.setSelection(0, true);

//                Log.d("spinner", "cityName = " + cityName + ", position = " + position + ", id = " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // ?????????
        List<String> areaNames = new ArrayList<>();
        for (Area area : areaMap.get(1)) {
            // ???????????????????????????1
            areaNames.add(area.getAreaName());
        }

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, areaNames);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spHomeSearchArea.setAdapter(areaAdapter);
        spHomeSearchArea.setSelection(0, true);
        spHomeSearchArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String areaName = ((TextView)view).getText().toString();
//                Log.d("spinner", "areaName = " + areaName + ", position = " + position + ", id = " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /*
     * ????????????
     */
    private void handleButton() {
        btnHomeSearch.setOnClickListener(v -> {
            // ????????????
            // ??????
            int cityId = cityList.get(spHomeSearchCity.getSelectedItemPosition()).getCityId();
            String cityName = cityList.get(spHomeSearchCity.getSelectedItemPosition()).getCityName();

            // ?????????
            int areaId = areaMap.get(cityId).get(spHomeSearchArea.getSelectedItemPosition()).getAreaId();
            String areaName = areaMap.get(cityId).get(spHomeSearchArea.getSelectedItemPosition()).getAreaName();

//            Log.d("home", "cityName = " + cityName + " areaName = " + areaName);

            // ??????????????????
            Address address = nameToLatLng(cityName + areaName);
            LatLng userLatLng = new LatLng(address.getLatitude(), address.getLongitude());

            // ????????????
            int rentLower = 0;
            if (!editHomeSearchRentLower.getText().toString().trim().isEmpty()) {
                rentLower = Integer.parseInt(editHomeSearchRentLower.getText().toString().trim());
            }

            int rentUpper = Integer.MAX_VALUE;
            if (!editHomeSearchRentUpper.getText().toString().trim().isEmpty()) {
                rentUpper = Integer.parseInt(editHomeSearchRentUpper.getText().toString().trim());
            }

            if (rentLower >= rentUpper) {
                editHomeSearchRentLower.setError("?????????????????????");
                editHomeSearchRentUpper.setError("?????????????????????");
                return;
            }

            // ????????????
            int gender = 0;
            switch (radioHomeSearchGender.getCheckedRadioButtonId()) {
                case R.id.radioHomeSearchGender0:
                    gender = 0;
                    break;
                case R.id.radioHomeSearchGender1:
                    gender = 1;
                    break;
                case R.id.radioHomeSearchGender2:
                    gender = 2;
                    break;
            }

            // ????????????
            int type = 0;
            switch (radioHomeSearchType.getCheckedRadioButtonId()) {
                case R.id.radioHomeSearchType0:
                    type = 0;
                    break;
                case R.id.radioHomeSearchType1:
                    type = 1;
                    break;
                case R.id.radioHomeSearchType2:
                    type = -1;
                    break;
            }

            //???server?????????
            if (RemoteAccess.networkCheck(activity)) {
                Map<String, String> searchMap = new HashMap<>();
                searchMap.put("cityId", String.valueOf(cityId));
                searchMap.put("areaId", String.valueOf(areaId));
                searchMap.put("rentLower", String.valueOf(rentLower));
                searchMap.put("rentUpper", String.valueOf(rentUpper));
                searchMap.put("gender", String.valueOf(gender));
                searchMap.put("type", String.valueOf(type));

                String url = Common.URL + "/getPublishData";
                JsonObject request = new JsonObject();
                request.addProperty("action", "getBySearch");
                request.addProperty("searchParam", gson.toJson(searchMap));

//                Log.d("home", gson.toJson(request));
                String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));

                JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
                String publishJson = response.get("publishList").getAsString();
                Type listCity = new TypeToken<List<Publish>>() {}.getType();
                List<Publish> publishes = gson.fromJson(publishJson, listCity);

                publishHomeList = getPublishHomeList(publishes, userLatLng);
//                Log.d("home", gson.toJson(publishHomeList));

                // ??????????????????
                googleMap.clear();

                // ??????????????????
                addPublishMarker(publishHomeList);

                // ????????????
                publishAdapter.submitList(new ArrayList<>(publishHomeList));

                // ???????????????
                moveCamera(userLatLng);

                // ??????????????????
                bottomSheetDialog.dismiss();
            }
        });
    }

    /*
     * ??????/?????? ??? ?????????
     */
    private Address nameToLatLng(String name) {
        try {
            List<Address> addressList = geocoder.getFromLocationName(name, 1);
            if (addressList != null && addressList.size() > 0) {
                return addressList.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????? ??? ??????/??????
     */
    private String latLngToName(final double lat, final double lng) {
        try {
            if (!Geocoder.isPresent()) {
                return null;
            }
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            StringBuilder name = new StringBuilder();
            Address address = addressList.get(0);
            if (address != null) {
//                Log.d("home", "getAdminArea = "+address.getAdminArea()+address.getSubAdminArea());

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    name.append(address.getAddressLine(i))
                            .append("\n");

                    Log.d("home", "user address = " + address.getAddressLine(i));
                }
            }
            return name.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}