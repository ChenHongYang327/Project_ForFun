package idv.tfp10105.project_forfun.signin.Customer_Service_Page;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;

import idv.tfp10105.project_forfun.R;


public class Customer_Service_Fragment extends Fragment {
    private Activity activity;

    private EditText edName, edMail, edPhone, edMessage;
    private ImageButton btCallCustomerService, btSendEmail;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_signin, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
    }


    private void findViews(View view) {
        edName = view.findViewById(R.id.customer_service_ed_name); // 姓名輸入欄位
        edMail = view.findViewById(R.id.customer_service_ed_mail); // 信箱輸入欄位
        edPhone = view.findViewById(R.id.customer_service_ed_phone); // 手機號碼輸入欄位
        edMessage = view.findViewById(R.id.customer_service_ed_message); // 留言輸入欄位
        btCallCustomerService = view.findViewById(R.id.customer_service_bt_CallCustomerService); // 按鈕 撥打客服電話
        btSendEmail = view.findViewById(R.id.customer_service_bt_sendEmail); // 按鈕 發送郵件

    }
}

