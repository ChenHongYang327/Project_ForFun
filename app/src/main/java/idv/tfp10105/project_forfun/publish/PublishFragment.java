package idv.tfp10105.project_forfun.publish;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

import static android.app.Activity.RESULT_OK;

public class PublishFragment extends Fragment {
    private final double PICK_HEIGHT = 200.0;

    private MainActivity activity;
    private Gson gson;
    private Geocoder geocoder;
    private Uri uriPicture;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;

    // UI??????
    private TextInputEditText editPublishTitle, editPublishInfo, editPublishAddress, editPublishRent, editPublishDeposit, editPublishSquare;
    private ImageView imgPublishTitle, imgPublishInfo1, imgPublishInfo2, imgPublishInfo3;
    private TextView txtPublishTitleImage, txtPublishInfo1, txtPublishInfo2, txtPublishInfo3;
    private Spinner spPublishCity, spPublishArea;
    private RadioGroup radioPublishGender, radioPublishType;
    private RadioButton radioPublishGender0, radioPublishGender1, radioPublishGender2;
    private RadioButton radioPublishType0, radioPublishType1;
    private CheckBox cbPublishFurnishedAll, cbPublishFurnished0, cbPublishFurnished1, cbPublishFurnished2, cbPublishFurnished3, cbPublishFurnished4, cbPublishFurnished5, cbPublishFurnished6, cbPublishFurnished7, cbPublishFurnished8;
    private Button btnPublishSubmit;
    private BottomSheetDialog bottomSheetDialog;
    private Button btnSheetPickPic, btnSheetTakePic, btnSheetCancel;


    // ????????????
    private int userId = 0;
    private int publishId = 0;
    private int uploadImgViewId;
    private String[] furnished = new String[9];
    private List<City> cityList;
    private List<Area> areaList;
    private Map<Integer, List<Area>> areaMap;
    private boolean isEditMode = false;

    ActivityResultLauncher<Intent> takePicLauncher =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    corp(uriPicture);
                }
            }
    );

    ActivityResultLauncher<Intent> pickPicLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    corp(result.getData().getData());
                }
            }
    );

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = scaleImage(UCrop.getOutput(result.getData()));

                    switch (uploadImgViewId) {
                        case R.id.imgPublishTitle:
                            imgPublishTitle.setImageBitmap(bitmap);
                            break;
                        case R.id.imgPublishInfo1:
                            imgPublishInfo1.setImageBitmap(bitmap);
                            break;
                        case R.id.imgPublishInfo2:
                            imgPublishInfo2.setImageBitmap(bitmap);
                            break;
                        case R.id.imgPublishInfo3:
                            imgPublishInfo3.setImageBitmap(bitmap);
                            break;
                        default:
                            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                            break;
                    }

                    bottomSheetDialog.dismiss();
                }
            }
    );

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
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);

        cityList = CityAreaUtil.getInstance().getCityList();
        areaList = CityAreaUtil.getInstance().getAreaList();
        areaMap = CityAreaUtil.getInstance().getAreaMap();
        userId = sharedPreferences.getInt("memberId",-1);
        publishId = getArguments() != null ? getArguments().getInt("publishId") : 0;

        return inflater.inflate(R.layout.fragment_publish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int role = sharedPreferences.getInt("role",-1);
        if (role != 2) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage("????????????????????????");
            dialog.setCancelable(false);
            dialog.setPositiveButton("??????", (dialog1, which) -> {
                // ??????????????????
                Navigation.findNavController(view).navigate(R.id.memberCenterFragment);
            });
            Window window = dialog.show().getWindow();
            // ??????????????????
            Button btnOK = window.findViewById(android.R.id.button1);
            btnOK.setTextColor(getResources().getColor(R.color.black));
        }

        // UI??????
        // ????????????
        editPublishTitle = view.findViewById(R.id.editPublishTitle);
        editPublishInfo = view.findViewById(R.id.editPublishInfo);
        editPublishAddress = view.findViewById(R.id.editPublishAddress);
        editPublishRent = view.findViewById(R.id.editPublishRent);
        editPublishDeposit = view.findViewById(R.id.editPublishDeposit);
        editPublishSquare = view.findViewById(R.id.editPublishSquare);

        // ??????
        imgPublishTitle = view.findViewById(R.id.imgPublishTitle);
        imgPublishInfo1 = view.findViewById(R.id.imgPublishInfo1);
        imgPublishInfo2 = view.findViewById(R.id.imgPublishInfo2);
        imgPublishInfo3 = view.findViewById(R.id.imgPublishInfo3);
        txtPublishTitleImage = view.findViewById(R.id.txtPublishTitleImage);
        txtPublishInfo1 = view.findViewById(R.id.txtPublishInfo1);
        txtPublishInfo2 = view.findViewById(R.id.txtPublishInfo2);
        txtPublishInfo3 = view.findViewById(R.id.txtPublishInfo3);

        // bottom sheet
        View bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet,null);
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(bottomSheetView);

        btnSheetPickPic = bottomSheetView.findViewById(R.id. btPickpic);
        btnSheetTakePic = bottomSheetView.findViewById(R.id. btTakepic);
        btnSheetCancel = bottomSheetView.findViewById(R.id. btCancel);

        handlePicture();

        // ????????????
        spPublishCity = view.findViewById(R.id.spPublishCity);
        spPublishArea = view.findViewById(R.id.spPublishArea);
        handleSpinner();

        // ????????????
        radioPublishGender = view.findViewById(R.id.radioPublishGender);
        radioPublishGender0 = view.findViewById(R.id.radioPublishGender0);
        radioPublishGender1 = view.findViewById(R.id.radioPublishGender1);
        radioPublishGender2 = view.findViewById(R.id.radioPublishGender2);

        // ????????????
        radioPublishType = view.findViewById(R.id.radioPublishType);
        radioPublishType0 = view.findViewById(R.id.radioPublishType0);
        radioPublishType1 = view.findViewById(R.id.radioPublishType1);

        // ????????????
        cbPublishFurnishedAll = view.findViewById(R.id.cbPublishFurnishedAll);
        cbPublishFurnished0 = view.findViewById(R.id.cbPublishFurnished0);
        cbPublishFurnished1 = view.findViewById(R.id.cbPublishFurnished1);
        cbPublishFurnished2 = view.findViewById(R.id.cbPublishFurnished2);
        cbPublishFurnished3 = view.findViewById(R.id.cbPublishFurnished3);
        cbPublishFurnished4 = view.findViewById(R.id.cbPublishFurnished4);
        cbPublishFurnished5 = view.findViewById(R.id.cbPublishFurnished5);
        cbPublishFurnished6 = view.findViewById(R.id.cbPublishFurnished6);
        cbPublishFurnished7 = view.findViewById(R.id.cbPublishFurnished7);
        cbPublishFurnished8 = view.findViewById(R.id.cbPublishFurnished8);
        handleCheckBox();

        // ??????
        btnPublishSubmit = view.findViewById(R.id.btnPublishSubmit);
        handleButton();

        TextView btnDebug = view.findViewById(R.id.btnPublishDebug);
        btnDebug.setOnClickListener(v -> {
            editPublishTitle.setText("?????????????????????");
            editPublishInfo.setText("????????????????????????\n??????????????????????????????");
            spPublishCity.setSelection(0, true);
            spPublishArea.setSelection(2, true);
            editPublishAddress.setText("??????????????????219???4-5F");
            editPublishRent.setText("1000");
            editPublishDeposit.setText("1");
            editPublishSquare.setText("100");
            cbPublishFurnishedAll.setChecked(true);
        });

        // ????????????
        if (publishId != 0) {
            isEditMode = true;

            // ??????action bar
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // ??????bottom navigation
            activity.findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
            // ??????Debug??????
            btnDebug.setVisibility(View.GONE);
            editMode(publishId);
            btnPublishSubmit.setText("??????");
        }
    }

    /*
     * ????????????
     */
    private void  handlePicture() {
        View.OnClickListener listener = v -> {
            uploadImgViewId = v.getId();
            bottomSheetDialog.show();
        };

        imgPublishTitle.setOnClickListener(listener);
        imgPublishInfo1.setOnClickListener(listener);
        imgPublishInfo2.setOnClickListener(listener);
        imgPublishInfo3.setOnClickListener(listener);

        btnSheetPickPic.setOnClickListener(v -> {
            // ??????
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPicLauncher.launch(intent);
        });

        btnSheetTakePic.setOnClickListener(v -> {
            // ??????
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (isIntentAvailable(intent)) {
                // ????????????????????????????????????
                File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = new File(file, "Pic.jpg");
                // ??????Uri
                uriPicture = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileProvider",
                        file
                );

                // ??????????????????????????????
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPicture);

                // ????????????
                takePicLauncher.launch(intent);
            }
        });

        btnSheetCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    // ????????????????????????App
    private boolean isIntentAvailable (Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        return intent.resolveActivity(packageManager) != null;
    }

    // ????????????
    private void corp(Uri sourceUri) {
        // ??????????????????
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "Pic_corp.jpg");

        Uri destinationUri = Uri.fromFile(file);
        Intent intent = UCrop.of(sourceUri, destinationUri).getIntent(activity);
        cropPictureLauncher.launch(intent);
    }

    // ????????????
    private Bitmap scaleImage(Uri srcUri) {
        Bitmap newBitmap = null;

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), srcUri);

                // ??????ImageDecoder.OnHeaderDecodedListener ??????????????????????????????
                ImageDecoder.OnHeaderDecodedListener listener = (decoder, info, source1) -> {
                    // ??????????????????
                    int srcWidth = info.getSize().getWidth();
                    int srcHeight = info.getSize().getHeight();
                    // ????????????????????????
                    int longer = Math.max(srcWidth, srcHeight);
                    double scale = PICK_HEIGHT / longer;
                    int newWidth = (int) (srcWidth * scale);
                    int newHeight = (int) (srcHeight * scale);
                    // ??????????????????
                    decoder.setTargetSize(newWidth, newHeight);
                };

                newBitmap = ImageDecoder.decodeBitmap(source, listener);
            } else {
                InputStream is = activity.getContentResolver().openInputStream(srcUri);
                Bitmap srcBitmap = BitmapFactory.decodeStream(is);

                // ??????????????????
                int srcWidth = srcBitmap.getWidth();
                int srcHeight = srcBitmap.getHeight();
                // ????????????????????????
                int longer = Math.max(srcWidth, srcHeight);
                double scale = PICK_HEIGHT / longer;
                int newWidth = (int) (srcWidth * scale);
                int newHeight = (int) (srcHeight * scale);

                newBitmap = Bitmap.createScaledBitmap(srcBitmap, newWidth, newHeight, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newBitmap;
    }

    // ????????????
    private boolean uploadImage(String fullFilePath, ImageView imageView) {
        /*
        ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ???????????????????????????????????????????????????????????????

        ?????????????????????final???????????????????????????
        ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ????????????????????????????????????"???????????????"??????????????????????????????????????????"???????????????"???
        ??????????????????????????????????????????List???????????????????????????~

        ???????????????
        https://nnosnhoj.pixnet.net/blog/post/43603513
         */
        final boolean[] uploadComplete = {true};

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ((BitmapDrawable)imageView.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream);

        // ????????????????????????????????????(storage.getReference() ??????????????????)
        StorageReference imageRef = storage.getReference().child(fullFilePath);
        imageRef.putBytes(stream.toByteArray())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        uploadComplete[0] = true;
                        Log.d("??????Firebase?????????????????????","????????????");
                    } else {
                        uploadComplete[0] = false;
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("??????Firebase?????????????????????", errorMessage);
                    }

                });

        return uploadComplete[0];
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

        spPublishCity.setAdapter(cityAdapter);
        spPublishCity.setSelection(0, true);
        spPublishCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cityName = ((TextView)view).getText().toString();

                // ??????????????????????????????
                List<String> areaNames = new ArrayList<>();
                for (Area area : areaMap.get(cityList.get(position).getCityId())) {
                    areaNames.add(area.getAreaName());
                }

                ArrayAdapter<String> areaAdapter = (ArrayAdapter<String>) spPublishArea.getAdapter();
                areaAdapter.clear();
                areaAdapter.addAll(areaNames);
                areaAdapter.notifyDataSetChanged();
                spPublishArea.setSelection(0, true);
//                Log.d("spinner", "cityName = " + cityName + ", position = " + position + ", id = " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("spinner", "??????");
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

        spPublishArea.setAdapter(areaAdapter);
        spPublishArea.setSelection(0, true);
        spPublishArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String areaName = ((TextView)view).getText().toString();
                Log.d("spinner", "areaName = " + areaName + ", position = " + position + ", id = " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*
     * ??????check box
     */
    private void handleCheckBox() {
        Arrays.fill(furnished, "0");

        // ??????
        cbPublishFurnishedAll.setOnCheckedChangeListener((buttonView, isChecked) -> setAllFurnishedCheck(isChecked));

        // ??????
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            String strCheck = isChecked ? "1" : "0";

            switch (buttonView.getId()) {
                case R.id.cbPublishFurnished0:
                    furnished[0] = strCheck;
                    break;
                case R.id.cbPublishFurnished1:
                    furnished[1] = strCheck;
                    break;
                case R.id.cbPublishFurnished2:
                    furnished[2] = strCheck;
                    break;
                case R.id.cbPublishFurnished3:
                    furnished[3] = strCheck;
                    break;
                case R.id.cbPublishFurnished4:
                    furnished[4] = strCheck;
                    break;
                case R.id.cbPublishFurnished5:
                    furnished[5] = strCheck;
                    break;
                case R.id.cbPublishFurnished6:
                    furnished[6] = strCheck;
                    break;
                case R.id.cbPublishFurnished7:
                    furnished[7] = strCheck;
                    break;
                case R.id.cbPublishFurnished8:
                    furnished[8] = strCheck;
                    break;
            }
//                Log.d("publish", Arrays.toString(furnished));
        };

        cbPublishFurnished0.setOnCheckedChangeListener(listener);
        cbPublishFurnished1.setOnCheckedChangeListener(listener);
        cbPublishFurnished2.setOnCheckedChangeListener(listener);
        cbPublishFurnished3.setOnCheckedChangeListener(listener);
        cbPublishFurnished4.setOnCheckedChangeListener(listener);
        cbPublishFurnished5.setOnCheckedChangeListener(listener);
        cbPublishFurnished6.setOnCheckedChangeListener(listener);
        cbPublishFurnished7.setOnCheckedChangeListener(listener);
        cbPublishFurnished8.setOnCheckedChangeListener(listener);
    }

    /*
     * ????????????check box??????
     */
    private void setAllFurnishedCheck(boolean check) {
        cbPublishFurnished0.setChecked(check);
        cbPublishFurnished1.setChecked(check);
        cbPublishFurnished2.setChecked(check);
        cbPublishFurnished3.setChecked(check);
        cbPublishFurnished4.setChecked(check);
        cbPublishFurnished5.setChecked(check);
        cbPublishFurnished6.setChecked(check);
        cbPublishFurnished7.setChecked(check);
        cbPublishFurnished8.setChecked(check);
    }

    /*
     * ????????????
     */
    private void handleButton() {
        btnPublishSubmit.setOnClickListener(v -> {
            boolean canSubmit = true;

            // ??????????????????
            if (editPublishTitle.getText().toString().trim().isEmpty()) {
                editPublishTitle.setError("???????????????");
                canSubmit = false;
            }

            if (editPublishAddress.getText().toString().trim().isEmpty()) {
                editPublishAddress.setError("???????????????");
                canSubmit = false;
            }

            if (editPublishRent.getText().toString().trim().isEmpty()) {
                editPublishRent.setError("???????????????");
                canSubmit = false;
            }

            if (editPublishSquare.getText().toString().trim().isEmpty()) {
                editPublishSquare.setError("???????????????");
                canSubmit = false;
            }

            if (!canSubmit) {
                Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            // ????????????
            // ??????
            // ??????
            int cityId = cityList.get(spPublishCity.getSelectedItemPosition()).getCityId();
            String cityName = cityList.get(spPublishCity.getSelectedItemPosition()).getCityName();

            // ?????????
            int areaId = areaMap.get(cityId).get(spPublishArea.getSelectedItemPosition()).getAreaId();
            String areaName = areaMap.get(cityId).get(spPublishArea.getSelectedItemPosition()).getAreaName();

            // ??????
            String strAddress = cityName + areaName + editPublishAddress.getText().toString().trim();

            // ?????????
            Address address = nameToLatLng(strAddress);

            // ??????
            int deposit = 0;
            if (!editPublishDeposit.getText().toString().trim().isEmpty()) {
                deposit = Integer.parseInt(editPublishDeposit.getText().toString().trim());
            }

            // ??????
            Gender gender = Gender.BOTH;
            switch (radioPublishGender.getCheckedRadioButtonId()) {
                case R.id.radioPublishGender0:
                    gender = Gender.BOTH;
                    break;
                case R.id.radioPublishGender1:
                    gender = Gender.MALE;
                    break;
                case R.id.radioPublishGender2:
                    gender = Gender.FEMALE;
                    break;
            }

            // ??????
            HouseType type = HouseType.WITH_BATH;
            switch (radioPublishType.getCheckedRadioButtonId()) {
                case R.id.radioPublishType0:
                    type = HouseType.WITH_BATH;
                    break;
                case R.id.radioPublishType1:
                    type = HouseType.NO_BATH;
                    break;
            }

            // ??????
            String strFurnished = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                strFurnished = String.join("|", furnished);
            } else {
                StringBuilder str = new StringBuilder();
                str.append(furnished[0]);
                for (int i = 1; i < furnished.length; i++) {
                    str.append("|").append(furnished[i]);
                }
                strFurnished = str.toString();
            }

            if (publishId == 0) {
                if (RemoteAccess.networkCheck(activity)) {
                    String url = Common.URL + "/publishHouse";
                    JsonObject request = new JsonObject();
                    request.addProperty("action", "getNewId");

                    String jsonIn = RemoteAccess.getJsonData(url, gson.toJson(request));

                    JsonObject response = gson.fromJson(jsonIn, JsonObject.class);
                    publishId = response.get("newId").getAsInt();
                }
            }

            // ???????????????Publish??????
            Publish publish = new Publish();
            publish.setPublishId(publishId);
            publish.setOwnerId(userId);
            publish.setTitle(editPublishTitle.getText().toString().trim());
            publish.setTitleImg("");
            publish.setPublishInfo(editPublishInfo.getText().toString().trim());
            publish.setPublishImg1("");
            publish.setPublishImg2("");
            publish.setPublishImg3("");
            publish.setCityId(cityId);
            publish.setAreaId(areaId);
            publish.setAddress(strAddress);
            publish.setLatitude(address == null ? 0.0 : address.getLatitude());
            publish.setLongitude(address == null ? 0.0 : address.getLongitude());
            publish.setRent(Integer.parseInt(editPublishRent.getText().toString()));
            publish.setDeposit(deposit);
            publish.setSquare(Integer.parseInt(editPublishSquare.getText().toString().trim()));
            publish.setGender(gender.toInt());
            publish.setType(type.toInt());
            publish.setFurnished(strFurnished);

            // 2. ?????????????????????????????????????????????????????????????????? -> ???????????? ??????????????????????????????????????????????????????
            // ??????
            String imgRootPath = getString(R.string.app_name) + "/Publish/" + publishId + "/";
            String fullImagePath = imgRootPath + "Title.jpg";
            if (uploadImage(fullImagePath, imgPublishTitle)) {
                publish.setTitleImg(fullImagePath);
            }

            fullImagePath = imgRootPath + "Info1.jpg";
            if (uploadImage(fullImagePath, imgPublishInfo1)) {
                publish.setPublishImg1(fullImagePath);
            }

            fullImagePath = imgRootPath + "Info2.jpg";
            if (uploadImage(fullImagePath, imgPublishInfo2)) {
                publish.setPublishImg2(fullImagePath);
            }

            fullImagePath = imgRootPath + "Info3.jpg";
            if (uploadImage(fullImagePath, imgPublishInfo3)) {
                publish.setPublishImg3(fullImagePath);
            }

//            Log.d("publish", gson.toJson(publish));

            //???server?????????
            if (RemoteAccess.networkCheck(activity)) {
                String url = Common.URL + "/publishHouse";
                JsonObject request = new JsonObject();
                request.addProperty("action", "publishHouse");
                request.addProperty("publish", gson.toJson(publish));

                String jsonIn = RemoteAccess.getJsonData(url, gson.toJson(request));

                JsonObject response = gson.fromJson(jsonIn, JsonObject.class);
                if ("1".equals(response.get("result_code").getAsString())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                    dialog.setMessage("??????/????????????");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("??????", (dialog1, which) -> {
                        if (isEditMode) {
                            Navigation.findNavController(v).popBackStack();
                        } else {
                            // ????????????
                            Navigation.findNavController(v).navigate(R.id.homeFragment);
                        }
                    });
                    Window window = dialog.show().getWindow();
                    // ??????????????????
                    Button btnOK = window.findViewById(android.R.id.button1);
                    btnOK.setTextColor(getResources().getColor(R.color.black));
                } else {
                    Toast.makeText(activity, "????????????/????????????", Toast.LENGTH_SHORT).show();
                }
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

    private void editMode(int publishId) {
        Publish publish = getPublishDataById(publishId);
        
        if (publish == null) {
            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        if (publish.getOwnerId() != userId) {
            Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        // ??????????????????
        // ??????ID -> index
        List<Area> areaList = null;
        String address = publish.getAddress();
        for (int i = 0; i < cityList.size(); i++) {
            if (cityList.get(i).getCityId().equals(publish.getCityId())) {
                spPublishCity.setSelection(i, true);
                areaList = areaMap.get(cityList.get(i).getCityId());
                // ?????????????????????????????????
                address = address.replace(cityList.get(i).getCityName(), "");
                break;
            }
        }

        // ?????????ID -> index
        if (areaList != null) {
            for (int j = 0; j < areaList.size(); j++) {
                if (areaList.get(j).getAreaId().equals(publish.getAreaId())) {
                    spPublishArea.setSelection(j, true);
                    // ????????????????????????????????????
                    address = address.replace(areaList.get(j).getAreaName(), "");
                }
            }
        }

        // ??????
        editPublishTitle.setText(publish.getTitle());
        editPublishInfo.setText(publish.getPublishInfo());
        editPublishAddress.setText(address);
        editPublishRent.setText(String.valueOf(publish.getRent()));
        editPublishDeposit.setText(String.valueOf(publish.getDeposit()));
        editPublishSquare.setText(String.valueOf(publish.getSquare()));

        // ??????
        getImage(imgPublishTitle, publish.getTitleImg());
        getImage(imgPublishInfo1, publish.getPublishImg1());
        getImage(imgPublishInfo2, publish.getPublishImg2());
        getImage(imgPublishInfo3, publish.getPublishImg3());

        // ????????????
        int genderId = 0;
        switch (Gender.toEnum(publish.getGender())){
            case BOTH:
                genderId = R.id.radioPublishGender0;
                break;
            case MALE:
                genderId = R.id.radioPublishGender1;
                break;
            case FEMALE:
                genderId = R.id.radioPublishGender2;
                break;
        }
        radioPublishGender.check(genderId);

        // ????????????
        int typeId = 0;
        switch (HouseType.toEnum(publish.getType())) {
            case WITH_BATH:
                typeId = R.id.radioPublishType0;
                break;
            case NO_BATH:
                typeId = R.id.radioPublishType1;
                break;
        }
        radioPublishType.check(typeId);

        // ????????????
        String[] furnished = publish.getFurnished().split("\\|");
        cbPublishFurnished0.setChecked("1".equals(furnished[0]));
        cbPublishFurnished1.setChecked("1".equals(furnished[1]));
        cbPublishFurnished2.setChecked("1".equals(furnished[2]));
        cbPublishFurnished3.setChecked("1".equals(furnished[3]));
        cbPublishFurnished4.setChecked("1".equals(furnished[4]));
        cbPublishFurnished5.setChecked("1".equals(furnished[5]));
        cbPublishFurnished6.setChecked("1".equals(furnished[6]));
        cbPublishFurnished7.setChecked("1".equals(furnished[7]));
        cbPublishFurnished8.setChecked("1".equals(furnished[8]));
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
}