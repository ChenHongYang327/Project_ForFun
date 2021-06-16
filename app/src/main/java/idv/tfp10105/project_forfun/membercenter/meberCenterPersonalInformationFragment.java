package idv.tfp10105.project_forfun.membercenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.commend.Commend;
import idv.tfp10105.project_forfun.commend.Member;
import idv.tfp10105.project_forfun.commend.RemoteAccess;

import static android.app.Activity.RESULT_OK;


public class meberCenterPersonalInformationFragment extends Fragment {
    private Activity activity;
    private File file;
    private Uri contentUri;
    private Member member;
    private ImageButton btPIEdit, btPIApply,btPTakePic,btPPickPic,btGTakePic,btGPickPic;
    private EditText etNameL,etNameF, etId, etBirthday, etPhone, etMail,etAddress;
    private ImageView ivHeadshot, ivIdPic1, ivIdPic2, ivGoodPeople;
    private TextView tvGoodPeopleNote;
    private RadioButton rbMan, rbWoman;
    private ScrollView scrollView;
    private int btPIEditClick = 0;
    private int btPIApplyClick = 0;
    private SimpleDateFormat sdf;
    private Bitmap bitmap = null;
    private boolean HSisClick=false;
    private boolean GPisClick=false;
    private boolean upNewHS=false;
    private boolean upNewGP=false;
    private FirebaseStorage storage;
    private String picUri; //上傳用
    private ByteArrayOutputStream baos; //上傳用
    private String url = Commend.URL + "meberCenterPersonalInformation";

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
        // 指定照片存檔路徑
        file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture.jpg");
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".fileProvider", file);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meber_center_personal_information, container, false);
        findView(view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleData();
        handleClick();
    }

    private void findView(View view) {
        btPIEdit = view.findViewById(R.id.btPIEdit);
        btPIApply = view.findViewById(R.id.btPIApply);
        btPTakePic = view.findViewById(R.id.btPTakePic);
        btPPickPic = view.findViewById(R.id.btPPickPic);
        btGTakePic = view.findViewById(R.id.btGTakePic);
        btGPickPic = view.findViewById(R.id.btGPickPic);
        etNameL = view.findViewById(R.id.etNameL);
        etNameF = view.findViewById(R.id.etNameF);
        etId = view.findViewById(R.id.etId);
        etBirthday = view.findViewById(R.id.etBirthday);
        etPhone = view.findViewById(R.id.etPhone);
        etMail = view.findViewById(R.id.etMail);
        etAddress=view.findViewById(R.id.etAddress);
        ivHeadshot = view.findViewById(R.id.ivHeadshot);
        ivIdPic1 = view.findViewById(R.id.ivIdPic1);
        ivIdPic2 = view.findViewById(R.id.ivIdPic2);
        ivGoodPeople = view.findViewById(R.id.ivGoodPeople);
        rbMan = view.findViewById(R.id.rbMan);
        rbWoman = view.findViewById(R.id.rbWoman);
        scrollView=view.findViewById(R.id.scrollView);
        tvGoodPeopleNote=view.findViewById(R.id.tvGoodPeopleNote);
    }

    private void handleData() {
        if (RemoteAccess.networkCheck(activity)) {
            //防沒連到伺服器閃退
            if (RemoteAccess.getJsonData(url, null).equals("error")) {
                Toast.makeText(activity, "與伺服器連線錯誤", Toast.LENGTH_SHORT).show();
                btPIEdit.setEnabled(false);
                btPIApply.setEnabled(false);
                return;
            }
            //跟後端提出請求
            JsonObject clientreq = new JsonObject();
            clientreq.addProperty("action", "getMember");
            clientreq.addProperty("member_id", 3);  //要改
            String serverresp = RemoteAccess.getJsonData(url, clientreq.toString());
            member = new Gson().fromJson(serverresp, Member.class);
            //整理回傳的資訊
            String name = member.getNameL() + member.getNameF();
            int gender = member.getGender();
            String id = member.getId();
            String phone = member.getPhone() + "";
            String address=member.getAddress();
            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            String birthday = sdf.format(member.getBirthady());
            String mail = member.getMail();
            //設定欄位資料
            //設定大頭貼
            if (member.getHeadshot() != null) {
                getImage(ivHeadshot, member.getHeadshot());
            }
            //設定良民證
            if(member.getCitizen() != null) {
                getImage(ivGoodPeople, member.getCitizen());
            }
            etNameL.setText(name);
            if (gender == 0) {
                rbMan.setChecked(true);
            } else if (gender == 1) {
                rbWoman.setChecked(true);
            }
            etId.setText(id);
            etPhone.setText(phone);
            etBirthday.setText(birthday);
            etMail.setText(mail);
            etAddress.setText(address);
        }
    }
    //下載Firebase storage的照片
    public void getImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 3; //設定上限
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("顯示Firebase取得圖片的錯誤", errorMessage);

                    }
                });

    }
    //上傳Firebase storage的照片
    private String uploadImage(byte[] imageByte) {
        // 取得storage根目錄位置
        StorageReference rootRef = storage.getReference();
        //  回傳資料庫的路徑
        final String imagePath = getString(R.string.app_name) + "/Person/"+member.getMemberId()+"/"+ System.currentTimeMillis();
        // 建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);
        // 將儲存在imageVIew的照片上傳
        imageRef.putBytes(imageByte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("顯示Firebase上傳圖片的狀態","上傳成功");
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("顯示Firebase上傳圖片的錯誤", errorMessage);
                        Toast.makeText(activity,"圖片上傳失敗", Toast.LENGTH_SHORT).show();
                    }
                });
        return imagePath;
    }


    private void handleClick() {
        //圖片用

        btPIEdit.setOnClickListener(v -> {
            //編輯個人資料
            if (btPIEditClick == 0) {
                //變更按鈕
                btPIEditClick = 1;
                btPIApplyClick = 1;
                btPIEdit.setImageResource(R.drawable.bt_sure);
                btPIApply.setImageResource(R.drawable.bt_cancel);
                etNameL.setEnabled(true); //改姓
                etNameL.setText(member.getNameL());
                etNameF.setVisibility(View.VISIBLE); //顯示名的欄位
                etNameF.setEnabled(true);
                etNameF.setText(member.getNameF());
//                etId.setEnabled(true);
//                etBirthday.setEnabled(true);
//                etBirthday.setInputType(InputType.TYPE_NULL);
                etPhone.setEnabled(true); //改電話
                etMail.setEnabled(true); //改email
                etAddress.setEnabled(true); //改address
                //修改照片按鈕
                btPPickPic.setVisibility(View.VISIBLE);
                btPTakePic.setVisibility(View.VISIBLE);

            }
            //點擊完成
            else if (btPIEditClick == 1) {
                //判斷是否為編輯個人資料
                if(etNameF.getVisibility()==View.VISIBLE) {
                    if (etNameL.getText().toString().isEmpty()) {
                        etNameL.setError("姓不可為空");
                        return;

                    } else if (etNameF.getText().toString().isEmpty()) {
                        etNameF.setError("名字不可為空");
                        return;

                    } else if (etPhone.getText().toString().isEmpty()) {
                        etPhone.setError("電話不可為空");
                        return;

                    } else if (etAddress.getText().toString().isEmpty()) {
                        etAddress.setError("地址不可為空");
                        return;
                    } else if (etMail.getText().toString().isEmpty()) {
                        etMail.setError("地址不可為空");
                        return;
                    }
                    member.setNameL(etNameL.getText().toString());
                    member.setNameF(etNameF.getText().toString());
                    member.setPhone(Integer.parseInt(etPhone.getText().toString()));
                    member.setAddress(etAddress.getText().toString());
                    //上傳大頭貼圖片
                    if(upNewHS) {
                        baos = new ByteArrayOutputStream();
                        ((BitmapDrawable) ivHeadshot.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        picUri = uploadImage(baos.toByteArray());
                        member.setHeadshot(picUri);
                        upNewHS=false;
                    }
                }
                //申請成為房東
               else {
                    //上傳良民證圖片
                    if (upNewGP) {
                        baos = new ByteArrayOutputStream();
                        ((BitmapDrawable) ivGoodPeople.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        picUri = uploadImage(baos.toByteArray());
                        member.setCitizen(picUri);
                        upNewGP = false;
                    } else {
                        Toast.makeText(activity, "未更新良民證照片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //轉成member物件
                String updateMember = new Gson().toJson(member);
                JsonObject clientreq=new JsonObject();
                clientreq.addProperty("action","updateMember");
                clientreq.addProperty("member",updateMember);
                String serverresp = RemoteAccess.getJsonData(url, clientreq.toString());
                if(serverresp.equals("true")){
//                btPIEditClick = 0;
//                btPIEdit.setImageResource(R.drawable.bt_edit);
//                etNameL.setEnabled(false);
//                etNameF.setVisibility(View.GONE);
//                etId.setEnabled(false);
//                etBirthday.setEnabled(false);
//                etPhone.setEnabled(false);
//                etMail.setEnabled(false);
//                btPPickPic.setVisibility(View.GONE);
//                btPTakePic.setVisibility(View.GONE);
//                btGPickPic.setVisibility(View.GONE);
//                btGTakePic.setVisibility(View.GONE);
//                btPIApplyClick=0;
//                btPIApply.setImageResource(R.drawable.bt_apply);
                    Toast.makeText(activity, "更新成功", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v)
                            .navigate(R.id.meberCenterPersonalInformationFragment);
                }
                else{
                    Toast.makeText(activity, "更新失敗", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btPIApply.setOnClickListener(v -> {
            //點選申請房東
            if (btPIApplyClick == 0) {
                btPIEditClick = 1;
                btPIApplyClick = 1;
                btGPickPic.setVisibility(View.VISIBLE);
                btGTakePic.setVisibility(View.VISIBLE);
                btPIApply.setImageResource(R.drawable.bt_cancel); //改為取消圖片
                btPIEdit.setImageResource(R.drawable.bt_sure); //改為確定圖片
                scrollView.fullScroll(View.FOCUS_DOWN);
                tvGoodPeopleNote.setVisibility(View.VISIBLE);
            }
            //點選取消
            else if (btPIApplyClick == 1) {
                Navigation.findNavController(v)
                        .navigate(R.id.meberCenterPersonalInformationFragment);
            }

        });

        //改生日用
//        etBirthday.setOnClickListener(v -> {
//            Calendar m_Calendar = Calendar.getInstance();
//            DatePickerDialog.OnDateSetListener datepicker = (view, year, month, dayOfMonth) -> {
//                m_Calendar.set(Calendar.YEAR, year);
//                m_Calendar.set(Calendar.MONTH, month);
//                m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                etBirthday.setText(sdf.format(m_Calendar.getTime()));
//            };
//            DatePickerDialog dialog = new DatePickerDialog(activity,
//                    datepicker,
//                    m_Calendar.get(Calendar.YEAR),
//                    m_Calendar.get(Calendar.MONTH),
//                    m_Calendar.get(Calendar.DAY_OF_MONTH));
//            dialog.show();
//
//        });


        //編輯照片
        btPTakePic.setOnClickListener(v->{
            HSisClick=true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity,"找不到相機應用程式", Toast.LENGTH_SHORT).show();
            }

        });

        btPPickPic.setOnClickListener(v->{
            HSisClick=true;
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);

        });

        btGTakePic.setOnClickListener(v->{
            GPisClick=true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity,"找不到相機應用程式", Toast.LENGTH_SHORT).show();
            }

        });

        btGPickPic.setOnClickListener(v->{
            GPisClick=true;
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);


        });


    }

    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
        else{
            HSisClick=false;
            GPisClick=false;
        }
    }


    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                crop(result.getData().getData());
            }
        }
        else{
            HSisClick=false;
            GPisClick=false;
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri destinationUri = Uri.fromFile(file);
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
//                .withAspectRatio(16, 9) // 設定裁減比例
//                .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
                .getIntent(activity);
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri resultUri = UCrop.getOutput(result.getData());

            try {
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
                if(HSisClick){
                    ivHeadshot.setImageBitmap(bitmap);
                    upNewHS=true;
                    HSisClick=false;
                }
                else if(GPisClick){
                    ivGoodPeople.setImageBitmap(bitmap);
                    upNewGP=true;
                    GPisClick=false;
                }

            } catch (IOException e) {
                Log.e("顯示cropPictureResult的錯誤", e.toString());

            }

        }
        else{
            HSisClick=false;
            GPisClick=false;
        }
    }
}