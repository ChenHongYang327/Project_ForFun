package idv.tfp10105.project_forfun.membercenter.personnalsanpshot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

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
    private TextView tvPSName,tvPSGender,tvPSAddress,tvPSCreatTime,tvRole2;
    private CircularImageView ivPSHS;
    private TabLayout tlPS;
    private ViewPager2 vpPSStar;
    private final List<Fragment> tabList=new ArrayList<>();
    private Member selectUser;
    private ImageButton btPSMessage,btPSReport;
    private SharedPreferences sharedPreferences;
    private int userId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
        //??????
        selectUser=getArguments()!=null?(Member)getArguments().getSerializable("SelectUser"):null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_personalsnapshot, container, false);
       findView(view);
       sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
       userId = sharedPreferences.getInt("memberId", -1);
       //??????
       if(userId==-1){
           btPSMessage.setEnabled(false);
           //??????mutate()?????????????????????????????????????????????????????????????????????????????????????????????
           btPSMessage.getBackground().mutate().setAlpha(120);
           btPSReport.setEnabled(false);
           btPSReport.getBackground().mutate().setAlpha(120);
       }
      if(selectUser!=null) {
          if (userId == selectUser.getMemberId()) {
                btPSMessage.setEnabled(false);
                //??????mutate()?????????????????????????????????????????????????????????????????????????????????????????????
                btPSMessage.getBackground().mutate().setAlpha(120);
                btPSReport.setEnabled(false);
                btPSReport.getBackground().mutate().setAlpha(120);
          }
      }
       return view;
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
        tvRole2=view.findViewById(R.id.tvRole2);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleTab();
        handleView();
    }

    private void handleTab() {
        //Fragment??????list
        if(tabList.size()!=2) {
            tabList.add(new TenantstatusFragment(selectUser));//??????
            tabList.add(new LandlordstatusFragment(selectUser));//??????
        }
        //list??????Adapter
        PersonnalVPAdapter myAdapter = new PersonnalVPAdapter(this, tabList);
        vpPSStar.setAdapter(myAdapter);
        //TabLayout???ViewPager?????????
        TabLayoutMediator tabLayoutMediator=  new TabLayoutMediator(tlPS, vpPSStar, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                //??????tab??????
                if(position==0){
                    tab.setText("?????????????????????");
                }
                if(position==1){
                    tab.setText("?????????????????????");
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
            String resp=RemoteAccess.getJsonData(url,clientreq.toString());//??????
            Member member=new Gson().fromJson(resp,Member.class);
            // ??????????????????
            String name=member.getNameL()+member.getNameF();
//            if(member.getRole()==2){
//                tvRole2.setVisibility(View.VISIBLE);
//            }
            tvPSName.setText("??????:"+name);
            if(member.getGender()==1){
                tvPSGender.setText("??????:???");
            }else if(member.getGender()==2){
                tvPSGender.setText("??????:???");
            }
            String address=member.getAddress();
           tvPSAddress.setText("??????:"+address);
           String creatTime=new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(member.getCreateTime());
           tvPSCreatTime.setText("????????????:"+creatTime);
            getImage(ivPSHS,member.getHeadshot());
            //????????????bundle
            btPSMessage.setOnClickListener(v->{
                String url = Common.URL + "ChatRoomController";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "selectChatRoomId");
                jsonObject.addProperty("receivedMemberId", selectUser.getMemberId());
                jsonObject.addProperty("sendMemberId", userId);
                int chatroomId;
                String result = RemoteAccess.getJsonData(url,jsonObject.toString());
                chatroomId = Integer.parseInt(result);
                if (chatroomId == 0) {
                    Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(activity, "?????????????????????", Toast.LENGTH_SHORT).show();

                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectUser", member);
                bundle.putInt("chatroomId", chatroomId);
                Navigation.findNavController(v).navigate(R.id.chatMessageFragment,bundle);
            });

            //????????????bundle
            btPSReport.setOnClickListener(v->{
                Bundle bundle=new Bundle();
                //?????????
                bundle.putInt("WHISTLEBLOWER_ID",userId);
                //????????????
                bundle.putInt("REPORTED_ID",selectUser.getMemberId());
                bundle.putInt("ITEM",2);
                Navigation.findNavController(v).navigate(R.id.reportFragment,bundle);
            });

        }
        else {
            Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
        }
    }

    //??????Firebase storage?????????
    public void getImage(final ImageView imageView, final String path) {
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //????????????
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                        Log.d("??????Firebase?????????????????????", errorMessage);

                    }
                });

    }

}
