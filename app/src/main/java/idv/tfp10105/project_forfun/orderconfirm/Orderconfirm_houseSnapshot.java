package idv.tfp10105.project_forfun.orderconfirm;

import android.app.ActionBar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;

public class Orderconfirm_houseSnapshot extends Fragment {
    private AppCompatActivity activity;
    //private Button btCon1,btCon2,btCon3;
    private TextView tvCon1,tvCon2,tvCon3;
    private TextView tvTitle,tvArea,tvSquare,tvType,tvName;
    private ImageView imgPic,imgHeadShot;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderconfirm_house_snapshot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tv_HSShot_Title);
        tvArea = view.findViewById(R.id.tv_HSShot_Area);
        tvSquare = view.findViewById(R.id.tv_HSShot_Square);
        tvType = view.findViewById(R.id.tv_HSShot_Type);
        tvName = view.findViewById(R.id.tv_HSShot_Name);
        tvCon1 = view.findViewById(R.id.tv_HSShot_con1);
        tvCon2 = view.findViewById(R.id.tv_HSShot_con2);
        tvCon3 = view.findViewById(R.id.tv_HSShot_con3);

        //TODO: 應該還要判斷房東或房客點進來
        //判斷跳轉頁面來源
        Bundle bundle = getArguments();
        switch (bundle.getInt("ocr")){
            case 1:
                Toast.makeText(activity,"reserve",Toast.LENGTH_SHORT).show();
                reserveEvent(view);
                return;
            case 2:
                Toast.makeText(activity,"order",Toast.LENGTH_SHORT).show();
                return;
            case 3:
                Toast.makeText(activity,"pay",Toast.LENGTH_SHORT).show();
                return;
            case 4:
                Toast.makeText(activity,"sign",Toast.LENGTH_SHORT).show();
                return;
            case 5:
                Toast.makeText(activity,"complete",Toast.LENGTH_SHORT).show();
                completeEvent(view);
                return;
            case 6:
                Toast.makeText(activity,"cancel",Toast.LENGTH_SHORT).show();
                return;
            case 7:
                Toast.makeText(activity,"paid",Toast.LENGTH_SHORT).show();
                return;
            case 8:
                Toast.makeText(activity,"payarrive",Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(activity,"TO DO NGEvent",Toast.LENGTH_SHORT).show();
                return;

        }
    }

    private void reserveEvent(View view) {
        tvCon1.setText("修改預約");
        tvCon2.setText("取消預約");
        view.findViewById(R.id.bt_HSShot_con3).setVisibility(View.GONE);

        view.findViewById(R.id.bt_HSShot_con1).setOnClickListener(v->{
            Toast.makeText(activity,"Click",Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.bt_HSShot_con2).setOnClickListener(v->{
            Toast.makeText(activity,"Click",Toast.LENGTH_SHORT).show();
        });

    }

    private void completeEvent(View view) {
        tvCon1.setText("我的合約");
        tvCon2.setText("我要續租");
        tvCon3.setText("測試寫錯頁後面再改");
        view.findViewById(R.id.bt_HSShot_con1).setOnClickListener(v->{
            Toast.makeText(activity,"Click",Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.bt_HSShot_con2).setOnClickListener(v->{
            Toast.makeText(activity,"Click",Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.bt_HSShot_con3).setOnClickListener(v->{
            Toast.makeText(activity,"Err",Toast.LENGTH_SHORT).show();
        });
    }
}