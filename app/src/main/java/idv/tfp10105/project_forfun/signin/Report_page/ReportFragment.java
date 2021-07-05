package idv.tfp10105.project_forfun.signin.Report_page;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;


public class ReportFragment extends Fragment {
    private Activity activity;
    private Resources resources;


    private Spinner spinner;
    private EditText edDetailedStatus;
    private ImageButton btConfirm, btCancel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化
        activity = getActivity();
        // 取得資源物件
        // 此物件可用來存取資源，和對資源做一些進階操作
        resources = getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleButton();
    }


    private void findViews(View view) {
        spinner = view.findViewById(R.id.report_page_spinner); // 檢舉類別選項
        edDetailedStatus = view.findViewById(R.id.report_page_ed_Detailed_status); // 詳細狀況輸入欄位
        btConfirm = view.findViewById(R.id.report_page_bt_confirm); // 按鈕 確認
        btCancel = view.findViewById(R.id.report_page_bt_cancel); // 按鈕 取消


    }

    private void handleButton() {
        //按鈕 確認
        btConfirm.setOnClickListener(V -> {
            // XXXXX是判斷Spinner選項的方法
            // 確認檢舉類別選項不可為空
            final String Spinner = String.valueOf(spinner.getText());
            if (spinner.isEmpty()) {
                spinner.setError(resources.getString(R.string.textedNameRequired));
                return;
            }
            // 確認詳細狀況不可為空
            final String detailedStatus = String.valueOf(edDetailedStatus.getText());
            if (detailedStatus.isEmpty()) {
                edDetailedStatus.setError(resources.getString(R.string.textedMailRequired));
                return;
            }

            if (RemoteAccess.networkCheck(activity)) {
                String url = Common.URL + "CUSTOMER_SERVICE_Servlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "CUSTOMER_SERVICE");
                jsonObject.addProperty("REPORT_CLASS", Spinner);
                jsonObject.addProperty("MESSAGE", detailedStatus);
                String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
                Log.d("HI",jsonIn);
                JsonObject jObject = new Gson().fromJson(jsonIn, JsonObject.class);
                if (jObject.get("status").getAsBoolean()) {
                    Toast.makeText(activity, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            }
        });



        //按鈕 取消
        btCancel.setOnClickListener(V -> {
            // XXXXX是判斷Spinner選項的方法
            // 確認檢舉類別選項刪除
            final String Spinner = String.valueOf(spinner.getText());
            if (spinner.) {
                spinner.setError(resources.getString(R.string.textedNameRequired));
                return;
            }
            // 確認詳細狀況刪除
            final String detailedStatus = String.valueOf(edDetailedStatus.getText());
            if (detailedStatus.) {
                edDetailedStatus.setError(resources.getString(R.string.textedMailRequired));
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                String url = Common.URL + "CUSTOMER_SERVICE_Servlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "CUSTOMER_SERVICE");
                jsonObject.addProperty("REPORT_CLASS", Spinner);
                jsonObject.addProperty("MESSAGE", detailedStatus);
                String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
                Log.d("HI",jsonIn);
                JsonObject jObject = new Gson().fromJson(jsonIn, JsonObject.class);
                if (jObject.get("status").getAsBoolean()) {
                    Toast.makeText(activity, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemClickListener();

    }
}



