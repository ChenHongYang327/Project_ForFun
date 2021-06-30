package idv.tfp10105.project_forfun.signin.Report_page;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import idv.tfp10105.project_forfun.R;


public class ReportFragment extends Fragment {
    private Activity activity;

    private Spinner spinner;
    private EditText edDetailedStatus;
    private ImageButton btConfirm, btCancel;


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
        spinner = view.findViewById(R.id.report_page_spinner); // 檢舉類別選項
        edDetailedStatus = view.findViewById(R.id.report_page_ed_Detailed_status); // 詳細狀況輸入欄位
        btConfirm = view.findViewById(R.id.report_page_bt_confirm); // 按鈕 確認
        btCancel = view.findViewById(R.id.report_page_bt_cancel); // 按鈕 取消


    }
}


