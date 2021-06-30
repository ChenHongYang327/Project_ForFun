package idv.tfp10105.project_forfun.orderconfirm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;

public class Orderconfirm_score extends Fragment {
    private Activity activity;
    private RatingBar ratingBarP, ratingBarH;
    private ImageView btConfirm, btCancel; //button
    private TextView tvHOmsg, tvmsg, tvbtConfirmText, tvHouseMsgText, tvHouseTitle, tvTitle;
    private Bundle bundleIn = getArguments() , bundleOut = new Bundle();
    private int tapNum = -1, orderId = -1;
    private Gson gson = new Gson();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderconfirm_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBarP = view.findViewById(R.id.ratingBar_ocrScore_people);
        ratingBarH = view.findViewById(R.id.ratingBar_ocrScore_House);
        btConfirm = view.findViewById(R.id.bt_ocrScore_confirm);
        btCancel = view.findViewById(R.id.bt_ocrScore_cancel);
        tvbtConfirmText = view.findViewById(R.id.tv_ocrScore_confirmText);
        tvTitle = view.findViewById(R.id.tv_ocrScore_peopleTitle);
        tvHouseTitle = view.findViewById(R.id.tv_ocrScore_HouseTitle);
        tvHouseMsgText = view.findViewById(R.id.tv_ocrScore_house_Visibility);
        tvHOmsg = view.findViewById(R.id.tv_ocrScore_HouseMsg);
        tvmsg = view.findViewById(R.id.tv_ocrScore_peopleMsg);

        try{
            tapNum =  bundleIn.getInt("SCORE");
            orderId = bundleIn.getInt("ORDERID");
        }catch (Exception e){
            tapNum = -1;
        }
        bundleOut.putInt("OCR",tapNum);
        bundleOut.putInt("ORDERID",orderId);

        //取消按鈕回上頁
        btCancel.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.orderconfirm_houseSnapshot,bundleOut);
        });

        // 判斷

        switch (tapNum){
            case 5:
                // 房客
                handleTenant();
                break;

            case 15:
                // 房東
                handleOwner();
                break;

            default:
                Navigation.findNavController(view).navigate(R.id.orderconfirm_houseSnapshot,bundleOut);
                Toast.makeText(activity, "已完成評價", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleTenant() {

        btConfirm.setOnClickListener(v->{

            //檢查網路連線 person % house 需傳到不同地方存
            if(!RemoteAccess.networkCheck(activity)){ Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show(); return; }

            // save person
            String personURL = Common.URL + "PersonEvaluation";

            int countStarP = (int) ratingBarP.getRating();
            String msg_P = tvmsg.getText().toString().trim();

            JsonObject presonJson = new JsonObject();
            presonJson.addProperty("STARS",countStarP);
            presonJson.addProperty("MSG",msg_P);
            presonJson.addProperty("ORDERID",orderId);
            String jsonIn_P = RemoteAccess.getJsonData(personURL,presonJson.toString());

            JsonObject result_P = gson.fromJson(jsonIn_P,JsonObject.class);
            int resoltcode_P = result_P.get("RESULT").getAsInt();

            // save house
            String houseURL = Common.URL + "Order";

            int countStarH = (int) ratingBarH.getRating();
            String msg_H = tvHOmsg.getText().toString().trim();

            JsonObject houseJson = new JsonObject();
            houseJson.addProperty("STARS",countStarH);
            houseJson.addProperty("MSG",msg_H);
            houseJson.addProperty("ORDERID",orderId);
            String jsonIn_H = RemoteAccess.getJsonData(houseURL,houseJson.toString());

            JsonObject result_H = gson.fromJson(jsonIn_H,JsonObject.class);
            int resoltcode_H = result_H.get("RESULT").getAsInt();


            if(resoltcode_H == 200 && resoltcode_P == 200){
                //TODO: goto homeFragment
                //   Navigation.findNavController(v).navigate(R.id.);
            }else{
                Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleOwner() {
        ratingBarH.setVisibility(View.GONE);
        tvHouseTitle.setVisibility(View.GONE);
        tvHouseMsgText.setVisibility(View.GONE);
        tvHOmsg.setVisibility(View.GONE);
        tvTitle.setText("> 給房客評價：");

        btConfirm.setOnClickListener(v->{

            //檢查網路連線
            if(!RemoteAccess.networkCheck(activity)){ Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show(); return; }

            // take value
            int countStarP = (int) ratingBarP.getRating();
            String msg_HO = tvmsg.getText().toString().trim();

            String url = Common.URL + "PersonEvaluation";

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("STARS",countStarP);
            jsonObject.addProperty("MSG",msg_HO);
            jsonObject.addProperty("ORDERID",orderId);
            String jsonIn = RemoteAccess.getJsonData(url,jsonObject.toString());

            JsonObject result = gson.fromJson(jsonIn,JsonObject.class);
            int resoltcode = result.get("RESULT").getAsInt();

            if(resoltcode == 200){
                //TODO: goto homeFragment
             //   Navigation.findNavController(v).navigate(R.id.);

            }else{ Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show(); }

        });
    }

}