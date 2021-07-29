package idv.tfp10105.project_forfun.signin.Report_page;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;


public class ReportFragment extends Fragment {
    private Activity activity;
    private Resources resources;
    private Resources requestCode;


    private Spinner spinner;
    private EditText edDetailedStatus, textView;
    private ImageButton btConfirm, btCancel;
    private String options;
    private int ITEM, POST_ID, REPORTED_ID, WHISTLEBLOWER_ID, CHATROOM_ID;


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
        handleSpinner();
        handleButton();

//        bundle練習
//        int a;
//        Bundle bundle = new Bundle();
//        bundle.putInt("ji", a);
//
//        int b;
//        Bundle bundle1 = getArguments();
//        b = bundle1.getInt("ji");
//
//
        //會員ID(檢舉者）
        Bundle bundle = getArguments();
        WHISTLEBLOWER_ID = bundle.getInt("WHISTLEBLOWER_ID");

        //會員ID（被檢舉者）
        Bundle bundle2 = getArguments();
        REPORTED_ID = bundle2.getInt("REPORTED_ID");


        //貼文ID

        Bundle bundle3 = getArguments();
        POST_ID = bundle3.getInt("POST_ID");

        //留言ID
        Bundle bundle5 = getArguments();
        CHATROOM_ID = bundle5.getInt("CHATROOM_ID");


        //檢舉項目
        Bundle bundle4 = getArguments();
        ITEM = bundle4.getInt("ITEM");


        //判斷進哪個

    }




    private void findViews(View view) {
        spinner = view.findViewById(R.id.report_page_spinner); // 檢舉類別選項
        edDetailedStatus = view.findViewById(R.id.report_page_ed_Detailed_status); // 詳細狀況輸入欄位
        btConfirm = view.findViewById(R.id.report_page_bt_confirm); // 按鈕 確認
        btCancel = view.findViewById(R.id.report_page_bt_cancel); // 按鈕 取消
        textView = view.findViewById(R.id.report_page_ed_text);


    }

    private void handleSpinner(){
        // onItemSelected是判斷Spinner選項的方法
        spinner.setSelection(0, true);
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> Parent, View view, int pos, long id) {
                options = Parent.getItemAtPosition(pos).toString();
//                textView.setText(options);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        };
        spinner.setOnItemSelectedListener(listener);

    }


    private void handleButton() {
        //按鈕 確認
        btConfirm.setOnClickListener(v -> {


            // 確認詳細狀況不可為空
            final String detailedStatus = String.valueOf(edDetailedStatus.getText());
            if (detailedStatus.isEmpty()) {
                edDetailedStatus.setError(resources.getString(R.string.textedMailRequired));
                return;
            }

            //抽屜編號轉換字
            spinner.getSelectedItemPosition();
            Log.d("AAA", spinner.getSelectedItemPosition()+"");

//            int nb = -1 ;
//            switch (options) {
//                case 0;
//                    System.out.println("冒充他人");
//                    break;
//            }


            switch (ITEM) {

                // 0 貼文類 (post)
                case 0 :
                   if(RemoteAccess.networkCheck(activity)) {
                       String url = Common.URL + "REPORT_Servlet";
                       JsonObject jsonObject = new JsonObject();
                       jsonObject.addProperty("action", "Post");
                       jsonObject.addProperty("WHISTLEBLOWER_ID", WHISTLEBLOWER_ID);//會員ID(檢舉者
                       jsonObject.addProperty("REPORTED_ID", REPORTED_ID);//會員ID（被檢舉者）
                       jsonObject.addProperty("MESSAGE", detailedStatus);//反應內容
                       jsonObject.addProperty("REPORT_CLASS", spinner.getSelectedItemPosition());//檢舉類別
                       jsonObject.addProperty("POST_ID", POST_ID);//貼文ID
                       jsonObject.addProperty("ITEM", ITEM);//檢舉項目
                       Log.d("SSS", REPORTED_ID + "") ;


                       String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
                       Log.d("HI", jsonIn);
                       JsonObject jObject = new Gson().fromJson(jsonIn, JsonObject.class);
                       if (jObject.get("status").getAsBoolean()) {
                           Toast.makeText(activity, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                           Navigation.findNavController(v).popBackStack();
                       } else {
                           Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                       }
                   }else {
                           Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                       }

                break;



                //1 留言類(Charoom)
            case 1 :
                if(RemoteAccess.networkCheck(activity)) {
                    String url = Common.URL + "REPORT_Servlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "Charoom");
                    jsonObject.addProperty("WHISTLEBLOWER_ID", WHISTLEBLOWER_ID);//會員ID(檢舉者
                    jsonObject.addProperty("REPORTED_ID", REPORTED_ID);//會員ID（被檢舉者）
                    jsonObject.addProperty("MESSAGE", detailedStatus);//反應內容
                    jsonObject.addProperty("REPORT_CLASS", spinner.getSelectedItemPosition());//檢舉類別
                    jsonObject.addProperty("CHATROOM_ID", CHATROOM_ID);//檢舉項目


                        String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
                        JsonObject jObject = new Gson().fromJson(jsonIn, JsonObject.class);
                        if (jObject.get("status").getAsBoolean()) {
                            Toast.makeText(activity, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).popBackStack();
                        } else {
                            Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                    }
                    break;



                // 2 用戶類(user)
                case 2 :
                    if(RemoteAccess.networkCheck(activity)) {
                        String url = Common.URL + "REPORT_Servlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "User");
                        jsonObject.addProperty("WHISTLEBLOWER_ID", WHISTLEBLOWER_ID);//會員ID(檢舉者
                        jsonObject.addProperty("REPORTED_ID", REPORTED_ID);//會員ID（被檢舉者）
                        jsonObject.addProperty("MESSAGE", detailedStatus);//反應內容
                        jsonObject.addProperty("REPORT_CLASS", spinner.getSelectedItemPosition());//檢舉類別
                        jsonObject.addProperty("ITEM", ITEM);//檢舉項目


                        String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
                        JsonObject jObject = new Gson().fromJson(jsonIn, JsonObject.class);
                        if (jObject.get("status").getAsBoolean()) {
                            Toast.makeText(activity, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).popBackStack();
                        } else {
                            Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(activity, "Inserted Failed", Toast.LENGTH_SHORT).show();
                    }

                    break;


                default:
                    Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).popBackStack();
                break;

            }
        });






//        //按鈕 取消
        btCancel.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();


        });


    }
}




