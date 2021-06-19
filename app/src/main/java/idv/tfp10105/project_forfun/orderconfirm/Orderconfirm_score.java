package idv.tfp10105.project_forfun.orderconfirm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;

public class Orderconfirm_score extends Fragment {
    private AppCompatActivity activity;
    RatingBar ratingBarP, ratingBarH;
    ImageView btConfirm; //bt


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderconfirm_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBarP = view.findViewById(R.id.ratingBar_ocrScore_people);
        ratingBarH = view.findViewById(R.id.ratingBar_ocrScore_House);
        btConfirm = view.findViewById(R.id.bt_ocrScore_confirm);

        //bt confirmEvent
        btConfirmEvent(view);

    }

    private void btConfirmEvent(View view) {
        btConfirm.setOnClickListener(v->{
            int countStarP = (int) ratingBarP.getRating();
            int countStarH = (int) ratingBarH.getRating();

            String starNumP = String.valueOf(countStarP);
            String starNumH = String.valueOf(countStarH);

            Toast.makeText(activity,starNumP+"\tPeople stars",Toast.LENGTH_SHORT).show();
            Toast.makeText(activity,starNumH+"\tHouse stars",Toast.LENGTH_LONG).show();

        });
    }
}