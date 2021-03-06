package idv.tfp10105.project_forfun.signin;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.RemoteAccess;

import static android.app.Activity.RESULT_OK;


public class RegisterFragment extends Fragment {
    private Activity activity;
    private String sBundle = ""; //??????????????????
    private Member member;//?????????????????????
    private ImageButton btRgSubmit, btRgCancel;
    private EditText etRgNameL, etRgNameF, etRgId, etRgBirthday, etRgPhone, etRgMail, etRgAddress;
    private ImageView ivRgHeadshot, ivRgIdPicF, ivRgIdPicB, ivRgGoodPeople;
    private RadioButton rbRgMan, rbRgWoman;
    private TextView tvRgGoodPeople, rgTitle, rgGoodPeopleNote;
    //BottomSheet?????????
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;
    private Button btPickpic, btTakepic, btCancel;
    private FirebaseStorage storage;
    private String picUri; //???????????????
    private ByteArrayOutputStream baos; //?????????
    //??????
    private Uri contentUri;
    //????????????????????????
    private boolean uploadHeadshot = false;
    private boolean uploadIdPicF = false;
    private boolean uploadIdPicB = false;
    private boolean uploadGoodPeople = false;
    //????????????????????????
    private boolean clickHeadshot = false;
    private boolean clickIdPicF = false;
    private boolean clickIdPicB = false;
    private boolean clickGoodPeople = false;
    private final String url = Common.URL + "/register";
    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        sBundle = getArguments() != null ? getArguments().getString("Apply") : "";
        // ????????????????????????
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture.jpg");
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".fileProvider", file);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regist, container, false);
        //bottomeSheet
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        ViewGroup parent = (ViewGroup) bottomSheetView.getParent();
        parent.setBackgroundResource(android.R.color.transparent);
        //------
        findView(view);
        handleView();
        handleClick();
        return view;
    }




    private void findView(View view) {
        btRgSubmit = view.findViewById(R.id.btRgSubmit);
        btRgCancel = view.findViewById(R.id.btRgCancel);
        etRgNameL = view.findViewById(R.id.etRgNameL);
        etRgNameF = view.findViewById(R.id.etRgNameF);
        rbRgWoman = view.findViewById(R.id.rbRgWoman);
        rbRgMan = view.findViewById(R.id.rbRgMan);
        etRgPhone = view.findViewById(R.id.etRgPhone);
        etRgId = view.findViewById(R.id.etRgId);
        etRgBirthday = view.findViewById(R.id.etRgBirthday);
        etRgMail = view.findViewById(R.id.etRgMail);
        etRgAddress = view.findViewById(R.id.etRgAddress);
        ivRgHeadshot = view.findViewById(R.id.ivRgHeadshot);
        ivRgIdPicF = view.findViewById(R.id.ivRgIdPicF);
        ivRgIdPicB = view.findViewById(R.id.ivRgIdPicB);
        rgTitle = view.findViewById(R.id.rgTitle);
        tvRgGoodPeople = view.findViewById(R.id.tvRgGoodPeople);
        ivRgGoodPeople = view.findViewById(R.id.ivRgGoodPeople);
        rgGoodPeopleNote = view.findViewById(R.id.rgGoodPeopleNote);
        //bottomsheet
        btTakepic = bottomSheetView.findViewById(R.id.btTakepic);
        btPickpic = bottomSheetView.findViewById(R.id.btPickpic);
        btCancel = bottomSheetView.findViewById(R.id.btCancel);

    }

    private void handleView() {
        etRgBirthday.setInputType(InputType.TYPE_NULL);
        if (sBundle.equals("Landlord")) {
            rgTitle.setText("??????????????????");
            tvRgGoodPeople.setVisibility(View.VISIBLE);
            ivRgGoodPeople.setVisibility(View.VISIBLE);
            rgGoodPeopleNote.setVisibility(View.VISIBLE);
        } else if (sBundle.equals("Tenant")) {
            rgTitle.setText("??????????????????");
        }
    }

    private void handleClick() {
        // ?????????????????????
        rgTitle.setOnClickListener(v -> {
            etRgNameL.setError(null);
            etRgNameF.setError(null);
            etRgId.setError(null);
            etRgAddress.setError(null);
            etRgBirthday.setError(null);
            etRgPhone.setError(null);
            etRgMail.setError(null);
            etRgAddress.setError(null);

            if (sBundle.equals("Landlord")) {
                etRgNameL.setText("???");
                etRgNameF.setText("??????");
            } else {
                etRgNameL.setText("???");
                etRgNameF.setText("??????");
            }
            rbRgMan.setChecked(true);
            etRgId.setText("A123456789");
            etRgAddress.setText("????????????????????????????????????219???5???");
//            etRgBirthday.setText("1991/01/24");
//            etRgPhone.setText("0912345678");
//            etRgMail.setText("test@email.com");
            uploadHeadshot = true;
            uploadIdPicB = true;
            uploadIdPicF = true;
            if (sBundle.equals("Landlord")) {
                uploadGoodPeople = true;
            }
        });
        //????????????
        etRgBirthday.setOnClickListener(v -> {
            Calendar m_Calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            DatePickerDialog.OnDateSetListener datepicker = (view, year, month, dayOfMonth) -> {
                m_Calendar.set(Calendar.YEAR, year);
                m_Calendar.set(Calendar.MONTH, month);
                m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etRgBirthday.setText(sdf.format(m_Calendar.getTime()));
            };
            DatePickerDialog dialog = new DatePickerDialog(activity,
                    datepicker,
                    m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH),
                    m_Calendar.get(Calendar.DAY_OF_MONTH));
            Calendar maxCalendar = Calendar.getInstance();
            maxCalendar.set(Calendar.YEAR, maxCalendar.get(Calendar.YEAR) - 18);
            dialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        });

        btRgSubmit.setOnClickListener(v -> {
            //???????????????
            boolean status = true;
            if (etRgNameL.getText().toString().trim().isEmpty()) {
                etRgNameL.setError("???????????????");
                status = false;
            }
            if (etRgNameF.getText().toString().trim().isEmpty()) {
                etRgNameF.setError("??????????????????");
                status = false;

            }
            if (etRgPhone.getText().toString().trim().isEmpty()) {
                etRgPhone.setError("??????????????????");
                status = false;
            }
            if (etRgId.getText().toString().trim().isEmpty()) {
                etRgId.setError("?????????????????????");
                status = false;
            }
            if (!rbRgMan.isChecked() && !rbRgWoman.isChecked()) {
                Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
            }
            if (etRgAddress.getText().toString().trim().isEmpty()) {
                etRgAddress.setError("??????????????????");
                status = false;
            }
            if (etRgMail.getText().toString().trim().isEmpty()) {
                etRgMail.setError("??????????????????");
                status = false;
            }
            if (!etRgMail.getText().toString().trim().isEmpty()) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etRgMail.getText().toString().trim()).matches()) {
                    etRgMail.setError("???????????????????????????");
                    status = false;
                }
            }
            if (etRgPhone.getText().toString().trim().length() != 10) {
                etRgPhone.setError("???????????????????????????");
                status = false;
            }
            if (etRgBirthday.getText().toString().trim().length() != 10) {
                etRgBirthday.setError("??????????????????");
                status = false;
            }
            if (!uploadHeadshot) {
                Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                status = false;
            }
            if (!uploadIdPicF) {
                Toast.makeText(activity, "??????????????????(??????)", Toast.LENGTH_SHORT).show();
                status = false;
            }
            if (!uploadIdPicB) {
                Toast.makeText(activity, "??????????????????(??????)", Toast.LENGTH_SHORT).show();
                status = false;
            }
            //???????????????
            if (sBundle.equals("Landlord")) {
                if (!uploadGoodPeople) {
                    Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                    status = false;
                }
            }
            //?????????
            if (!status) {
                return;
            }
            member = new Member();
            member.setPhone(Integer.parseInt(etRgPhone.getText().toString().trim()));
            //?????????????????????????????????
            JsonObject reqJson = new JsonObject();
            reqJson.addProperty("action", "checkPhone");
            reqJson.addProperty("member", new Gson().toJson(member));
            String resp = RemoteAccess.getJsonData(url, reqJson.toString());
            JsonObject respJson = new Gson().fromJson(resp, JsonObject.class);
            if (!respJson.get("pass").getAsBoolean()) {
                Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            //????????????????????????
            member.setNameL(etRgNameL.getText().toString().trim());
            member.setNameF(etRgNameF.getText().toString().trim());
            if (rbRgMan.isChecked()) {
                member.setGender(1);
            } else if (rbRgWoman.isChecked()) {
                member.setGender(2);
            }
            member.setId(etRgId.getText().toString().trim());
            member.setAddress(etRgAddress.getText().toString().trim());
            member.setBirthady(Timestamp.valueOf(etRgBirthday.getText().toString().replace("/", "-") + " " + "00:00:00"));
            member.setMail(etRgMail.getText().toString().trim());
            //?????????
            baos = new ByteArrayOutputStream();
            ((BitmapDrawable) ivRgHeadshot.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            picUri = uploadImage(baos.toByteArray(), "Headshot");
            member.setHeadshot(picUri);
            //???????????????
            baos = new ByteArrayOutputStream();
            ((BitmapDrawable) ivRgIdPicB.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            picUri = uploadImage(baos.toByteArray(), "IdImgb");
            member.setIdImgb(picUri);
            //???????????????
            baos = new ByteArrayOutputStream();
            ((BitmapDrawable) ivRgIdPicF.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            picUri = uploadImage(baos.toByteArray(), "IdImgf");
            member.setIdImgf(picUri);
            //?????????????????????
            if (sBundle.equals("Landlord")) {
                //?????????
                baos = new ByteArrayOutputStream();
                ((BitmapDrawable) ivRgGoodPeople.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                picUri = uploadImage(baos.toByteArray(), "Citizen");
                member.setCitizen(picUri);
            }
            //???????????????JSON
            reqJson.addProperty("action", "register");
            reqJson.addProperty("member", new GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(member));
            resp = RemoteAccess.getJsonData(url, reqJson.toString());
            respJson = new Gson().fromJson(resp, JsonObject.class);
            if (respJson.get("status").getAsBoolean()) {
                Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack(R.id.registerFragment, true);
                Navigation.findNavController(v)
                        .navigate(R.id.signinInFragment);
            } else {
                Toast.makeText(activity, "??????????????????????????????", Toast.LENGTH_SHORT).show();
            }

        });

        btRgCancel.setOnClickListener(v -> {
            //?????????
            Navigation.findNavController(v).popBackStack(R.id.registerFragment, true);
            //?????????
            Navigation.findNavController(v).popBackStack(R.id.registIntroductionFragment, true);
            Navigation.findNavController(v)
                    .navigate(R.id.registIntroductionFragment);

        });

        ivRgHeadshot.setOnClickListener(v -> {
            clickHeadshot = true;
            bottomSheetDialog.show();
        });
        ivRgIdPicB.setOnClickListener(v -> {
            clickIdPicB = true;
            bottomSheetDialog.show();
        });
        ivRgIdPicF.setOnClickListener(v -> {
            clickIdPicF = true;
            bottomSheetDialog.show();
        });
        ivRgGoodPeople.setOnClickListener(v -> {
            clickGoodPeople = true;
            bottomSheetDialog.show();
        });
        btPickpic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        });
        btTakepic.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        });
        btCancel.setOnClickListener(v -> {
            //??????bottomsheet
            bottomSheetDialog.dismiss();
            clickHeadshot = false;
            clickIdPicF = false;
            clickIdPicB = false;
            clickGoodPeople = false;
        });
        bottomSheetDialog.setOnCancelListener(dialog -> {
            //??????bottomsheet
            clickHeadshot = false;
            clickIdPicF = false;
            clickIdPicB = false;
            clickGoodPeople = false;
        });

    }

    //??????
    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
    }

    //????????????
    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                crop(result.getData().getData());
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri destinationUri = Uri.fromFile(file);
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
//                .withAspectRatio(16, 9) // ??????????????????
//                .withMaxResultSize(500, 500) // ??????????????????????????????????????????
                .getIntent(activity);
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri resultUri = UCrop.getOutput(result.getData());
            try {
                //????????????
                //?????????????????????
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    bitmap = BitmapFactory.decodeStream(
                            activity.getContentResolver().openInputStream(resultUri));
                } else {
                    ImageDecoder.Source source =
                            ImageDecoder.createSource(activity.getContentResolver(), resultUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                if (clickHeadshot) {
                    ivRgHeadshot.setImageBitmap(bitmap);
                    uploadHeadshot = true;
                } else if (clickIdPicB) {
                    ivRgIdPicB.setImageBitmap(bitmap);
                    uploadIdPicB = true;
                } else if (clickIdPicF) {
                    ivRgIdPicF.setImageBitmap(bitmap);
                    uploadIdPicF = true;
                } else if (clickGoodPeople) {
                    ivRgGoodPeople.setImageBitmap(bitmap);
                    uploadGoodPeople = true;
                }
                //  ucrop????????????
                bottomSheetDialog.dismiss();
                clickHeadshot = false;
                clickIdPicF = false;
                clickIdPicB = false;
                clickGoodPeople = false;

            } catch (IOException e) {
                Log.e("??????cropPictureResult?????????", e.toString());

            }

        }
    }

    //??????Firebase storage?????????
    private String uploadImage(byte[] imageByte, String imgSort) {
        // ??????storage???????????????
        StorageReference rootRef = storage.getReference();
        //  ????????????????????????
        final String imagePath = getString(R.string.app_name) + "/Person/" + member.getPhone() + "/" + imgSort;
        // ??????????????????????????????
        final StorageReference imageRef = rootRef.child(imagePath);
        // ????????????imageVIew???????????????
        imageRef.putBytes(imageByte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("??????Firebase?????????????????????", "????????????");
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("??????Firebase?????????????????????", errorMessage);
                    }
                });
        return imageRef.getPath();
    }
}