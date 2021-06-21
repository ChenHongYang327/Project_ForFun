package idv.tfp10105.project_forfun.signin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import idv.tfp10105.project_forfun.R;


public class signin_in extends Fragment {

    private Activity activity;
    private TextView edPhonee, edVerificationCode, txPrompt, tvSendTheVerificationCode, tvTourist;
    private ImageButton btSignIn, btRegistered, btAssist;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        return inflater.inflate(R.layout.fragment_signin_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
    }

    private void findViews(View view) {
        edPhonee = view.findViewById(R.id.ed_phone); // 手機號碼輸入欄位
        edVerificationCode = view.findViewById(R.id.ed_Verification_code); // 驗證碼輸入欄位
        txPrompt = view.findViewById(R.id.text_prompt); // 提示訊息
        btSignIn = view.findViewById(R.id.signin_bt_Sign_in); // 按鈕 會員登入
        btRegistered = view.findViewById(R.id.signin_bt_registered); // 按鈕 註冊
        btAssist = view.findViewById(R.id.signin_assist); // 按鈕 協助（右上角的問號）
        tvSendTheVerificationCode = view.findViewById(R.id.signin_tv_Send_the_verification_code); // 弱按鈕 發送驗證碼
        tvTourist = view.findViewById(R.id.signin_tv_tourist); // 弱按鈕 遊客登入
    }
}


