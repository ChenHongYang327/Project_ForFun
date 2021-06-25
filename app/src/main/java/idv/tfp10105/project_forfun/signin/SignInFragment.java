package idv.tfp10105.project_forfun.signin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Member;


public class SignInFragment extends Fragment {
    private Activity activity;

    private TextView tvSendTheVerificationCode,tvResendCode, tvTourist,tvCode;
    private EditText etPhone,etVerificationCode;
    private ImageView imageView;//快速登入
    private ImageButton btSignIn, btRegistered, btAssist;
    private String verificationId,phone;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private final String url = Common.URL + "signInController";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences",Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_sign_in, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleClick();
    }

    private void findViews(View view) {
        etPhone = view.findViewById(R.id.ed_phone); // 手機號碼輸入欄位
        etVerificationCode = view.findViewById(R.id.ed_Verification_code); // 驗證碼輸入欄位
        btSignIn = view.findViewById(R.id.signin_bt_Sign_in); // 按鈕 會員登入
        btRegistered = view.findViewById(R.id.signin_bt_registered); // 按鈕 註冊
        btAssist = view.findViewById(R.id.signin_assist); // 按鈕 協助（右上角的問號）
        tvSendTheVerificationCode = view.findViewById(R.id.signin_tv_Send_the_verification_code); // 弱按鈕 發送驗證碼
        tvTourist = view.findViewById(R.id.signin_tv_tourist); // 弱按鈕 遊客登入
        tvResendCode= view.findViewById(R.id.tvResendCode);
        tvCode= view.findViewById(R.id.tvCode); // 驗證碼標題
        imageView= view.findViewById(R.id.imageView); // 快速登入
    }

    @Override
    public void onStart() {
        super.onStart();
        // 檢查電話號碼是否驗證成功過
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if(sharedPreferences.getBoolean("firstOpen",true)) {
                //跳轉至導覽頁後跳轉首頁
            }
            if(new Date().getTime()-sharedPreferences.getLong("lastlogin",new Date().getTime())>10*60*1000){
                Toast.makeText(activity, "離上次登入超過十分鐘", Toast.LENGTH_SHORT).show();
                auth.signOut();
                JsonObject req=new JsonObject();
                req.addProperty("action","clearToken");
                req.addProperty("memberId",sharedPreferences.getInt("memberId",-1));
                RemoteAccess.getJsonData(url,req.toString());//不接回覆
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit()
                .putBoolean("firstOpen",false)
                        .apply();

            }
            else{
              Navigation.findNavController(btSignIn)
                            .navigate(R.id.homeFragment);
                }
        }
        else{
            if(sharedPreferences.getBoolean("firstOpen",true)) {
                sharedPreferences.edit().putBoolean("firstOpen",false).apply();
                Toast.makeText(activity, "進入導覽頁", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void handleClick() {
        //點選驗證手機號碼
        tvSendTheVerificationCode.setOnClickListener(v->{
            phone=etPhone.getText().toString().trim();
            if(phone.isEmpty()){
                etPhone.setError("電話號碼不可為空");
                return;
            }
            else if(phone.length()!=10){
                etPhone.setError("手機號碼格式錯誤");
                return;
            }
            tvCode.setVisibility(View.VISIBLE);
            etVerificationCode.setVisibility(View.VISIBLE);
            tvResendCode.setVisibility(View.VISIBLE);
            btSignIn.setVisibility(View.VISIBLE);
            requestVerificationCode("+886" + phone);
        });
        //重新發送
        tvResendCode.setOnClickListener(v->{
            phone=etPhone.getText().toString().trim();
            if(phone.isEmpty()){
                etPhone.setError("電話號碼不可為空");
                return;
            }
            else if(phone.length()!=10){
                etPhone.setError("手機號碼格式錯誤");
                return;
            }
            Toast.makeText(activity, phone, Toast.LENGTH_SHORT).show();
            resendVerificationCode("+886" + phone, resendToken);
        });
        //驗證登入
        btSignIn.setOnClickListener(v->{
            String verificationCode = etVerificationCode.getText().toString().trim();
            if (verificationCode.isEmpty()) {
                etVerificationCode.setError("驗證碼不可為空");
                return;
            }
            // 將應用程式收到的驗證識別代號(verificationId)與user輸入的簡訊驗證碼(verificationCode)送至Firebase
            verifyIDAndCode(verificationId, verificationCode);
        });
        //快速登入
        imageView.setOnLongClickListener(v -> {
            etPhone.setText("0921371162");;
            etVerificationCode.setText("123456");
            return true;
        });
        btRegistered.setOnClickListener(v->{
            Navigation.findNavController(v)
                    .navigate(R.id.registIntroductionFragment);
        });
        //遊客
        tvTourist.setOnClickListener(v->{
            sharedPreferences.edit()
                .putInt("role",3)
                    .apply();
            Navigation.findNavController(v)
                    .navigate(R.id.homeFragment);
        });
    }

    private void requestVerificationCode(String phone) {
        auth.setLanguageCode("zh-Hant");
        PhoneAuthOptions phoneAuthOptions =
                PhoneAuthOptions.newBuilder(auth)
                        // 電話號碼，驗證碼寄送的電話號碼
                        .setPhoneNumber(phone)
                        // 驗證碼失效時間，設為60秒代表即使多次請求驗證碼，過了60秒才會發送第2次
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        // 監控電話驗證的狀態
                        .setCallbacks(verifyCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void resendVerificationCode(String phone,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions phoneAuthOptions =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(verifyCallbacks)
                        // 驗證碼發送後，verifyCallbacks.onCodeSent()會傳來token，
                        // user要求重傳驗證碼必須提供token
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void verifyIDAndCode(String verificationId, String verificationCode) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
        firebaseAuthWithPhoneNumber(credential);
    }

    private void firebaseAuthWithPhoneNumber(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        //驗證碼驗證成功的動作
                        phoneSure();
                    } else {
                        Exception exception = task.getException();
                        //驗證錯誤或輸入錯誤
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        Log.d("顯示驗證碼驗證錯誤",message);
                        // user輸入的驗證碼與簡訊傳來的不同會產生錯誤
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            etVerificationCode.setError("驗證碼輸入錯誤");

                        }
                    }
                });
    }

    private void phoneSure() {
        if(RemoteAccess.networkCheck(activity)) {
            JsonObject req=new JsonObject();
            req.addProperty("action","singIn");
            req.addProperty("phone",phone);
            String resp=RemoteAccess.getJsonData(url,req.toString());
            JsonObject respJson=new Gson().fromJson(resp,JsonObject.class);
            boolean pass=respJson.get("pass").getAsBoolean();
            if(pass) {
                Member member=new Gson().fromJson(respJson.get("imformation").getAsString(),Member.class);
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            String token = task.getResult();
                            member.setToken(token);
                            sharedPreferences.edit()
                                    .putString("token",token)
                                    .apply();
                            Log.d("顯示裝置的token", token);
                            req.addProperty("action","updateToken");
                            req.addProperty("member",new Gson().toJson(member));
                            RemoteAccess.getJsonData(url,req.toString());//不接回覆
                        }
                    }
                });

                String citizen=member.getCitizen()==null?"": member.getCitizen();
                sharedPreferences.edit()
                        .putInt("memberId",member.getMemberId())
                        .putInt("role", member.getRole())
                        .putString("name",member.getNameL()+member.getNameF())
                        .putInt("phone",member.getPhone())
                        .putString("headshot",member.getHeadshot())
                        .putInt("gender",member.getGender())
                        .putString("id", member.getId())
                        .putString("birthday",new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(member.getBirthady()))
                        .putString("address",member.getAddress())
                        .putString("mail", member.getMail())
                        .putInt("type", member.getType())
                        .putString("token",member.getToken())
                        .putString("idImgf", member.getIdImgf())
                        .putString("idImgd",member.getIdImgf())
                        .putString("citizen",citizen)
                        .putBoolean("firstOpen",false)//第一次開啟
                        .putLong("lastlogin",new Date().getTime())//登入時間

                        .apply();
                Navigation.findNavController(btSignIn)
                        .navigate(R.id.homeFragment);
            }
            else{
                Toast.makeText(activity, "手機號碼錯誤", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(activity, "網路不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks verifyCallbacks
            = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        /** This callback will be invoked in two situations:
         1 - Instant verification. In some cases the phone number can be instantly
         verified without needing to send or enter a verification code.
         2 - Auto-retrieval. On some devices Google Play services can automatically
         detect the incoming verification SMS and perform verification without
         user action. */
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            Log.d("顯示驗證碼錯誤", "onVerificationCompleted: " + credential);
        }

        /**
         * 發送驗證碼填入的電話號碼格式錯誤，或是使用模擬器發送都會產生發送錯誤，
         * 使用模擬器發送會產生下列執行錯誤訊息：
         * App validation failed. Is app running on a physical device?
         */
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.e("顯示驗證碼錯誤", "onVerificationFailed: " + e.getMessage());

        }

        /**
         * The SMS verification code has been sent to the provided phone number,
         * we now need to ask the user to enter the code and then construct a credential
         * by combining the code with a verification ID.
         */
        @Override
        public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d("顯示驗證碼錯誤", "onCodeSent: " + id);
            verificationId = id;
            resendToken = token;
        }
    };

}

