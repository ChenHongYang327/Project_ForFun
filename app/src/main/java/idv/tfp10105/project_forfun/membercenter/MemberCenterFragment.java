package idv.tfp10105.project_forfun.membercenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;

public class MemberCenterFragment extends Fragment {
    private Activity activity;
    private TextView tvPersonalInformation,tvFavoriteList,tvOrderList,
            tvFunctionTour,tvMyRating,tvLogOut;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_membercenter, container, false);
        findeView(view);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleClick();
    }

    private void findeView(View view) {
        tvPersonalInformation=view.findViewById(R.id.tvPersonalInformation);
        tvFavoriteList=view.findViewById(R.id.tvFavoriteList);
        tvOrderList=view.findViewById(R.id.tvOrderList);
        tvFunctionTour=view.findViewById(R.id. tvFunctionTour);
        tvMyRating=view.findViewById(R.id.tvMyRating);
        tvLogOut=view.findViewById(R.id.tvLogOut);
    }

    private void handleClick() {
        int role=sharedPreferences.getInt("role",-1);
        tvPersonalInformation.setOnClickListener(v->{
            if(role==3){
                Toast.makeText(activity, "請登入會員", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v)
                        .navigate(R.id.signinInFragment);
                return;
            }
            Navigation.findNavController(v)
                    .navigate(R.id.meberCenterPersonalInformationFragment);
        });

        tvFavoriteList.setOnClickListener(v->{

        });

        tvOrderList.setOnClickListener(v->{
            Navigation.findNavController(v)
                    .navigate(R.id.orderconfirm_mainfragment);
        });


        tvFunctionTour.setOnClickListener(v->{


        });

        tvMyRating.setOnClickListener(v->{
            Navigation.findNavController(v)
                    .navigate(R.id.myEvaluationnFragment);
        });

        tvLogOut.setOnClickListener(v->{
            AlertDialog.Builder logOutDialog = new AlertDialog.Builder(activity);
            logOutDialog.setTitle(R.string.log_out);  //設置標題
            logOutDialog.setIcon(R.mipmap.ic_launcher_round); //標題前面那個小圖示
            logOutDialog.setMessage(R.string.log_out_dialog); //提示訊息
            logOutDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
                auth.signOut();
                JsonObject req=new JsonObject();
                req.addProperty("action","clearToken");
                req.addProperty("memberId",sharedPreferences.getInt("memberId",-1));
                String url = Common.URL + "signInController";
                RemoteAccess.getJsonData(url,req.toString());//不接回覆
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit()
                        .putBoolean("firstOpen",false)
                        .apply();
                Navigation.findNavController(v)
                        .navigate(R.id.action_memberCenterFragment_to_signinInFragment);
                Navigation.findNavController(v).popBackStack(R.id.memberCenterFragment,true);

            });
            logOutDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {

            });
            //設定對話框顏色
            Window window=logOutDialog.show().getWindow();
            Button btSure=window.findViewById(android.R.id.button1);
            Button btCancel=window.findViewById(android.R.id.button2);
            btSure.setTextColor(getResources().getColor(R.color.black));
            btCancel.setTextColor(getResources().getColor(R.color.black));

        });

    }
}