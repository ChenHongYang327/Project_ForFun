package idv.tfp10105.project_forfun.membercenter.personnalsanpshot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.membercenter.adapter.PersonnalVPAdapter;


public class PersonalSnapshotFragment extends Fragment {
    private final String url = Common.URL +"personalSnapshot";
    private Activity activity;
    private TextView tvPSName,tvPSGender,tvPSAddress,tvPSCreatTime;
    private CircularImageView ivPSHS;
    private TabLayout tlPS;
    private ViewPager2 vpPSStar;
    private final List<Fragment> tabList=new ArrayList<>();
    private Member selectUser;
    private ImageButton btPSMessage,btPSReport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
        //傳值
        selectUser=getArguments()!=null?(Member)getArguments().getSerializable("SelectUser"):null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_personalsnapshot, container, false);
       findView(view);
       fakedata();
       handleTab();
       handleView();
       return view;
    }


    private void fakedata() {
        selectUser=new Member();
        selectUser.setMemberId(3);
    }

    private void findView(View view) {
        tlPS=view.findViewById(R.id.tlPS);
        vpPSStar=view.findViewById(R.id.vpPSStar);
        tvPSName =view.findViewById(R.id.tvPSName);
        tvPSGender=view.findViewById(R.id.tvPSGender);
        tvPSAddress=view.findViewById(R.id.tvPSAddress);
        tvPSCreatTime=view.findViewById(R.id.tvPSCreatTime);
        ivPSHS=view.findViewById(R.id.ivPSHS);
        btPSMessage=view.findViewById(R.id.btPSMessage);
        btPSReport=view.findViewById(R.id.btPSReport);
    }


    private void handleTab() {
        //Fragment放入list
        tabList.add(new LandlordstatusFragment(selectUser));//房客
        tabList.add(new TenantstatusFragment(selectUser));//房東
        //list放入Adapter
        PersonnalVPAdapter myAdapter = new PersonnalVPAdapter(this, tabList);
        vpPSStar.setAdapter(myAdapter);
        //TabLayout和ViewPager的綁定
        TabLayoutMediator tabLayoutMediator=  new TabLayoutMediator(tlPS, vpPSStar, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                //設定tab名稱
                if(position==0){
                    tab.setText("房客身分的評價");
                }
                if(position==1){
                    tab.setText("房東身分的評價");
                }
            }
        });
        tabLayoutMediator.attach();
    }

    private void handleView() {
        if(RemoteAccess.networkCheck(activity)){
            JsonObject clientreq=new JsonObject();
            clientreq.addProperty("action","personalSnapshot");
            clientreq.addProperty("memberID",selectUser.getMemberId());
            String resp=RemoteAccess.getJsonData(url,clientreq.toString());
            Member member=new Gson().fromJson(resp,Member.class);
            // 此頁面的用戶
            String name=member.getNameL()+member.getNameF();
            tvPSName.setText("姓名:"+name);
            if(member.getGender()==1){
                tvPSGender.setText("性別:男");
            }else if(member.getGender()==2){
                tvPSGender.setText("性別:女");
            }
            String address=member.getAddress();
           tvPSAddress.setText("地址:"+address);
           String creatTime=new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(member.getCreateTime());
           tvPSCreatTime.setText("註冊時間:"+creatTime);
            getImage(ivPSHS,member.getHeadshot());
            //私訊跳轉bundle
            btPSMessage.setOnClickListener(v->{

            });
            //檢舉跳轉bundle
            btPSReport.setOnClickListener(v->{

            });




        }
        else {
            Toast.makeText(activity, "網路錯誤", Toast.LENGTH_SHORT).show();
        }
    }

    //下載Firebase storage的照片
    public void getImage(final ImageView imageView, final String path) {
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //設定上限
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Toast.makeText(activity, "圖片取得錯誤", Toast.LENGTH_SHORT).show();
                        Log.d("顯示Firebase取得圖片的錯誤", errorMessage);

                    }
                });

    }

}