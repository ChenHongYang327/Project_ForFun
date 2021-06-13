package idv.tfp10105.project_forfun.membercenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;

public class memberCenterFragment extends Fragment {
    private Activity activity;
    private TextView tvPersonalInformation,tvFavoriteList,tvOrderList,
            tvPublishList,tvFunctionTour,tvMyRating,tvLogOut;
    private ActionBar actionBar;
    private View actionBarView;
    private TextView tvTitle;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_member_center, container, false);
        findeView(view);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Actionbar title
        if(getActivity()!=null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBarView = actionBar.getCustomView();
                tvTitle=actionBarView.findViewById(R.id.tvTitle);
            }
        }
        handleClick();
    }

    private void findeView(View view) {
        tvPersonalInformation=view.findViewById(R.id.tvPersonalInformation);
        tvFavoriteList=view.findViewById(R.id.tvFavoriteList);
        tvOrderList=view.findViewById(R.id.tvOrderList);
        tvPublishList=view.findViewById(R.id.tvPublishList);
        tvFunctionTour=view.findViewById(R.id. tvFunctionTour);
        tvMyRating=view.findViewById(R.id.tvMyRating);
        tvLogOut=view.findViewById(R.id.tvLogOut);
    }

    private void handleClick() {
        tvPersonalInformation.setOnClickListener(v->{
            tvTitle.setText(R.string.personal_information);
            Navigation.findNavController(v)
                    .navigate(R.id.meberCenterPersonalInformationFragment);
        });

        tvFavoriteList.setOnClickListener(v->{
            tvTitle.setText(R.string.favorite_list);
        });

        tvOrderList.setOnClickListener(v->{
            tvTitle.setText(R.string.order_list);
            Navigation.findNavController(v)
                    .navigate(R.id.orderconfirm_mainfragment);
        });

        tvPublishList.setOnClickListener(v->{
            tvTitle.setText(R.string.publish_list);

        });

        tvFunctionTour.setOnClickListener(v->{
            tvTitle.setText(R.string.function_tour);

        });

        tvMyRating.setOnClickListener(v->{
            tvTitle.setText(R.string.my_rating);

        });

        tvLogOut.setOnClickListener(v->{
            AlertDialog.Builder logOutDialog = new AlertDialog.Builder(activity);
            logOutDialog.setTitle(R.string.log_out);  //設置標題
            logOutDialog.setIcon(R.mipmap.ic_launcher_round); //標題前面那個小圖示
            logOutDialog.setMessage(R.string.log_out_dialog); //提示訊息
            logOutDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
                Toast.makeText(activity, "sure", Toast.LENGTH_SHORT).show();
            });
            logOutDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {
                Toast.makeText(activity, "cancel", Toast.LENGTH_SHORT).show();
            });
            Window window=logOutDialog.show().getWindow();
            Button btSure=window.findViewById(android.R.id.button1);
            Button btCancel=window.findViewById(android.R.id.button2);
            btSure.setTextColor(getResources().getColor(R.color.black));
            btCancel.setTextColor(getResources().getColor(R.color.black));

        });

    }
}