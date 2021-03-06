package idv.tfp10105.project_forfun.membercenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.RemoteAccess;

import static android.app.Activity.RESULT_OK;


public class MemberCenterPersonalInformationFragment extends Fragment {
    private Activity activity;
    private Uri contentUri;
    private Member member;
    //BottomSheet?????????
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;
    private Button btPickpic,btTakepic,btCancel;
    private ImageButton btPIEdit, btPIApply;
    private EditText etNameL,etNameF, etId, etBirthday, etPhone, etMail,etAddress;
    private ImageView ivHeadshot, ivIdPicF, ivIdPicB, ivGoodPeople;
    private TextView tvGoodPeople,tvGoodPeopleNote,tvRole,GPNote,HSNote;
    private RadioButton rbMan, rbWoman;
    private ScrollView scrollView;
    //????????????????????????
    private int btPIEditClick = 0; // 0->?????????????????? 1->??????(????????????) 2->??????(????????????)
    private int btPIApplyClick = 0;  // 0->??????????????? 1->??????
    //??????????????????????????????
    private boolean HSisClick=false;
    private boolean GPisClick=false;
    //???????????????????????????
    private boolean upNewHS=false;
    private boolean upNewGP=false;
    private SimpleDateFormat sdf;
    private FirebaseStorage storage;
    private String picUri; //???????????????
    private ByteArrayOutputStream baos; //?????????
    private String serverresp;
    private SharedPreferences sharedPreferences;
    private final String url = Common.URL + "memberCenterPersonalInformation";

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_membercenter_personalinformation, container, false);
        //bottomeSheet
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet,null);
        bottomSheetDialog.setContentView(bottomSheetView);
        ViewGroup parent = (ViewGroup) bottomSheetView.getParent();
        parent.setBackgroundResource(android.R.color.transparent);
        //------
        findView(view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ????????????????????????
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture.jpg");
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".fileProvider", file);
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);
        //?????????????????????
        JsonObject clientreq = new JsonObject();
        clientreq.addProperty("action", "getMember");
        int memberId=sharedPreferences.getInt("memberId",-1);
        clientreq.addProperty("member_id",memberId);
        serverresp = RemoteAccess.getJsonData(url, clientreq.toString());
        handleData();
        handleClick();

    }

    private void findView(View view) {
        btPIEdit = view.findViewById(R.id.btPIEdit);
        btPIApply = view.findViewById(R.id.btPIApply);
        etNameL = view.findViewById(R.id.etNameL);
        etNameF = view.findViewById(R.id.etNameF);
        etId = view.findViewById(R.id.etId);
        etBirthday = view.findViewById(R.id.etBirthday);
        etPhone = view.findViewById(R.id.etPhone);
        etMail = view.findViewById(R.id.etMail);
        etAddress=view.findViewById(R.id.etAddress);
        ivHeadshot = view.findViewById(R.id.ivHeadshot);
        ivIdPicF = view.findViewById(R.id.ivIdPicF);
        ivIdPicB = view.findViewById(R.id.ivIdPicB);
        ivGoodPeople = view.findViewById(R.id.ivGoodPeople);
        rbMan = view.findViewById(R.id.rbMan);
        rbWoman = view.findViewById(R.id.rbWoman);
        tvGoodPeople=view.findViewById(R.id.tvGoodPeople);
        tvGoodPeopleNote=view.findViewById(R.id.tvGoodPeopleNote);
        tvRole=view.findViewById(R.id.tvRole);
        scrollView=view.findViewById(R.id.scrollView);
        GPNote=view.findViewById(R.id.GPNote);
        HSNote=view.findViewById(R.id.HSNote);
        //bottomsheet
        btTakepic=bottomSheetView.findViewById(R.id. btTakepic);
        btPickpic=bottomSheetView.findViewById(R.id.btPickpic);
        btCancel=bottomSheetView.findViewById(R.id.btCancel);
    }

    private void handleData() {
        if (RemoteAccess.networkCheck(activity)) {
            //???????????????????????????(?????????????????????)
            if (serverresp.equals("error")) {
                Toast.makeText(activity, "????????????????????????", Toast.LENGTH_SHORT).show();
                btPIEdit.setEnabled(false);
                btPIApply.setEnabled(false);
                return;
            }

            member = new Gson().fromJson(serverresp, Member.class);
            //?????????????????????
            String name = member.getNameL() + member.getNameF();
            int gender = member.getGender();
            String id = member.getId();
            String phone = "0"+member.getPhone();
            String address=member.getAddress();
            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            String birthday = sdf.format(member.getBirthady());
            String role;
            if(member.getRole()==1){
                role="????????????:??????";
                if(member.getCitizen()!=null&&!member.getCitizen().isEmpty()){
                    role +="\t(???????????????????????????)";
                    tvGoodPeople.setVisibility(View.VISIBLE);
                    ivGoodPeople.setVisibility(View.VISIBLE);
                }
            }
            else if(member.getRole()==2){
                role="????????????:???????????????";
                btPIApply.setVisibility(View.GONE);
                tvGoodPeople.setVisibility(View.VISIBLE);
                ivGoodPeople.setVisibility(View.VISIBLE);

            }
            else{
                role="????????????:";
            }
            //???????????????????????????
            tvRole.setText(role);
            //??????
            if(member.getMail()!=null) {
                etMail.setText(member.getMail());
            }
            //?????????
            if (member.getHeadshot() != null&&!member.getHeadshot().isEmpty()) {
                getImage(ivHeadshot, member.getHeadshot());
            }
            //?????????
            if(member.getCitizen() != null &&!member.getCitizen().isEmpty()) {
                getImage(ivGoodPeople, member.getCitizen());
            }
            //?????????
            if(member.getIdImgb()!=null&&member.getIdImgf()!=null&&!member.getIdImgb().isEmpty()&&!member.getIdImgf().isEmpty()){
                getImage(ivIdPicF, member.getIdImgf());
                getImage(ivIdPicB, member.getIdImgb());
            }
            //??????????????????
            etNameL.setText(name);
            //??????
            if (gender == 1) {
                rbMan.setChecked(true);
            } else if (gender == 2) {
                rbWoman.setChecked(true);
            }
            etId.setText(id);
            etPhone.setText(phone);
            etBirthday.setText(birthday);
            etAddress.setText(address);
        }
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
    //??????Firebase storage?????????
    private String uploadImage(byte[] imageByte,String imgSort) {
        // ??????storage???????????????
        StorageReference rootRef = storage.getReference();
        //  ????????????????????????
        final String imagePath = getString(R.string.app_name) + "/Person/"+member.getPhone()+"/"+ imgSort;
        // ??????????????????????????????
        final StorageReference imageRef = rootRef.child(imagePath);
        // ????????????imageVIew???????????????
        imageRef.putBytes(imageByte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("??????Firebase?????????????????????","????????????");
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("??????Firebase?????????????????????", errorMessage);
                    }
                });
        return imageRef.getPath();
    }


    private void handleClick() {
        btPIEdit.setOnClickListener(v -> {
            //??????????????????
            if (btPIEditClick == 0) {
                //????????????
                btPIEditClick = 1;//????????????????????????
                btPIApplyClick = 1;//??????????????????
                btPIEdit.setBackgroundResource(R.drawable.bt_sure);
                btPIApply.setVisibility(View.VISIBLE);
                btPIApply.setBackgroundResource(R.drawable.bt_cancel);
                HSNote.setVisibility(View.VISIBLE);
                etNameL.setEnabled(true); //??????
                etNameL.setText(member.getNameL());
                etNameF.setVisibility(View.VISIBLE); //??????????????????
                etNameF.setEnabled(true);
                etNameF.setText(member.getNameF());
//                etId.setEnabled(true);
//                etBirthday.setEnabled(true);
//                etBirthday.setInputType(InputType.TYPE_NULL);
//                etPhone.setEnabled(true); //?????????
                etMail.setEnabled(true); //???email
                etAddress.setEnabled(true); //???address

            }
            //????????????(????????????????????????)
            else if (btPIEditClick == 1||btPIEditClick == 2) {
                //?????????????????????????????????
                if(etNameF.getVisibility()==View.VISIBLE) {
                    if (etNameL.getText().toString().trim().isEmpty()) {
                        etNameL.setError("???????????????");
                        return;

                    } else if (etNameF.getText().toString().trim().isEmpty()) {
                        etNameF.setError("??????????????????");
                        return;

                    } else if (etPhone.getText().toString().trim().isEmpty()) {
                        etPhone.setError("??????????????????");
                        return;

                    } else if (etAddress.getText().toString().trim().isEmpty()) {
                        etAddress.setError("??????????????????");
                        return;
                    }
                     else if (!etMail.getText().toString().trim().isEmpty()&&!android.util.Patterns.EMAIL_ADDRESS.matcher(etMail.getText().toString().trim()).matches()) {
                        etMail.setError("???????????????????????????");
                        return;
                    }
                    else if (etPhone.getText().toString().trim().length()!=10) {
                        etPhone.setError("???????????????????????????");
                        return;
                    }
                    member.setNameL(etNameL.getText().toString().trim());
                    member.setNameF(etNameF.getText().toString().trim());
                    member.setPhone(Integer.parseInt(etPhone.getText().toString().trim()));
                    member.setAddress(etAddress.getText().toString().trim());
                    member.setMail(etMail.getText().toString().trim());
                    //????????????????????????????????????
                    if(upNewHS) {
                        baos = new ByteArrayOutputStream();
                        ((BitmapDrawable) ivHeadshot.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        picUri = uploadImage(baos.toByteArray(),"Headshot");
                        if(picUri.isEmpty()){
                            Toast.makeText(activity, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                            upNewHS=false;
                            return;
                        }
                        member.setHeadshot(picUri);
                        upNewHS=false;
                    }
                }
                //????????????????????????????????????
               if(btPIEditClick == 2){
                    //?????????????????????
                    if (upNewGP) {
                        baos = new ByteArrayOutputStream();
                        ((BitmapDrawable) ivGoodPeople.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        picUri = uploadImage(baos.toByteArray(),"Citizen");
                        if(picUri.isEmpty()){
                            Toast.makeText(activity, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                            upNewGP = false;
                            return;
                        }
                        member.setCitizen(picUri);
                        //???????????????
//                        upNewGP = false;
                    } else {
                        Toast.makeText(activity, "????????????????????????", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //??????member??????
                String updateMember = new Gson().toJson(member);
                JsonObject clientreq=new JsonObject();
                clientreq.addProperty("action","updateMember");
                clientreq.addProperty("member",updateMember);
                serverresp = RemoteAccess.getJsonData(url, clientreq.toString());
                if(serverresp.equals("true")){
                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                    String citizen=member.getCitizen()==null?"":member.getCitizen();
                    sharedPreferences.edit()
                            .putString("name",member.getNameL()+member.getNameF())
                            .putString("address",member.getAddress())
                            .putString("mail",member.getMail())
                            .putString("headshot",member.getHeadshot())
                            .putString("citizen",citizen)
                            .apply();

                    //------------------
                    //?????????????????????????????????
                    //????????????????????????????????????
                    if(member.getRole()==1) {
                        if (upNewGP) {
                                tvRole.setText("????????????:??????\t(???????????????????????????)");
                        }
                    }
                    //???????????????
                    if(member.getRole()==2) {
                        btPIApply.setVisibility(View.GONE);
                    }
                    //????????????
                    btPIEditClick = 0;
                    btPIApplyClick = 0;
                    HSisClick=false;
                    GPisClick=false;
                    upNewHS=false;
                    upNewGP=false;
                    btPIEdit.setBackgroundResource(R.drawable.bt_edit);
                    btPIApply.setBackgroundResource(R.drawable.bt_apply);
                    etNameL.setEnabled(false); //??????
                    String name=member.getNameL()+member.getNameF();
                    etNameL.setText(name);
                    etNameF.setVisibility(View.GONE); //??????????????????
                    etMail.setEnabled(false); //???email
                    etAddress.setEnabled(false); //???address
                    tvGoodPeopleNote.setVisibility(View.GONE);
                    GPNote.setVisibility(View.GONE);
                    HSNote.setVisibility(View.GONE);
                    //------------------
                    //??????bug????????????????????????
//                    Navigation.findNavController(v).popBackStack(R.id.MemberCenterPersonalInformationFragment,true);
//                    Navigation.findNavController(v)
//                            .navigate(R.id.MemberCenterPersonalInformationFragment);


                }
                else{
                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btPIApply.setOnClickListener(v -> {
            //??????????????????
            if (btPIApplyClick == 0) {
                btPIEditClick = 2;//?????????????????????
                btPIApplyClick = 1;
                btPIApply.setBackgroundResource(R.drawable.bt_cancel); //??????????????????
                btPIEdit.setBackgroundResource(R.drawable.bt_sure); //??????????????????
                tvGoodPeople.setVisibility(View.VISIBLE);
                ivGoodPeople.setVisibility(View.VISIBLE);
                tvGoodPeopleNote.setVisibility(View.VISIBLE);
                scrollView.fullScroll(View.FOCUS_DOWN);
                GPNote.setVisibility(View.VISIBLE);
            }
            //????????????
            else if (btPIApplyClick == 1) {
                Navigation.findNavController(v).popBackStack(R.id.meberCenterPersonalInformationFragment,true);
                Navigation.findNavController(v)
                        .navigate(R.id.meberCenterPersonalInformationFragment);

            }

        });
            ivHeadshot.setOnClickListener(v->{
                //?????????????????????????????????
                if(btPIEditClick==1) {
                    HSisClick = true;
                    bottomSheetDialog.show();
                }
            });
            ivGoodPeople.setOnClickListener(v->{
                //????????????
                if(btPIEditClick==2) {
                    GPisClick = true;
                    bottomSheetDialog.show();
                }
            });
            btPickpic.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPictureLauncher.launch(intent);
            });
            btTakepic.setOnClickListener(v->{
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                try {
                    takePictureLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(activity,"???????????????????????????", Toast.LENGTH_SHORT).show();
                }
            });
            btCancel.setOnClickListener(v->{
                bottomSheetDialog.dismiss();
                HSisClick=false;
                GPisClick=false;
            });
            bottomSheetDialog.setOnCancelListener(dialog -> {
                HSisClick=false;
                GPisClick=false;
            });



    }

    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
        //??????bottomsheet????????????
//        else{
//            HSisClick=false;
//            GPisClick=false;
//        }
    }


    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                crop(result.getData().getData());
            }
        }
        //??????bottomsheet????????????
//        else{
//            HSisClick=false;
//            GPisClick=false;
//        }
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
                //???????????????
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
                //??????????????????
                if(HSisClick){
                    ivHeadshot.setImageBitmap(bitmap);
                    //?????????????????????
                    upNewHS=true;
                    HSisClick=false;
                    bottomSheetDialog.dismiss();
                }
                //?????????????????????
                else if(GPisClick){
                    ivGoodPeople.setImageBitmap(bitmap);
                    //?????????????????????
                    upNewGP=true;
                    GPisClick=false;
                    bottomSheetDialog.dismiss();
                }

            } catch (IOException e) {
                Log.e("??????cropPictureResult?????????", e.toString());

            }

        }
        //??????bottomsheet????????????
//        else{
//            HSisClick=false;
//            GPisClick=false;
//        }
    }
}