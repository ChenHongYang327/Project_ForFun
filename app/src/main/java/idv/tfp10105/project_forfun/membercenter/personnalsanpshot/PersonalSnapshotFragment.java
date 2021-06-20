package idv.tfp10105.project_forfun.membercenter.personnalsanpshot;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.membercenter.personnalsanpshot.adapter.PersonnalVPAdapter;


public class PersonalSnapshotFragment extends Fragment {
    private Activity activity;
    private TabLayout tlPS;
    private ViewPager2 vpPSStar;
    private List<Fragment> tabList=new ArrayList<>();
    private Member user,selectUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
        //傳值
        user=getArguments()!=null?(Member)getArguments().getSerializable("User"):null;
        selectUser=getArguments()!=null?(Member)getArguments().getSerializable("SelectUser"):null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_personal_snapshot, container, false);
       findView(view);
        fakedata();
       handleTab();
       return view;
    }

    private void fakedata() {
        user=new Member();
        user.setMemberId(2);
        selectUser=new Member();
        selectUser.setMemberId(3);
    }

    private void findView(View view) {
        tlPS=view.findViewById(R.id.tlPS);
        vpPSStar=view.findViewById(R.id.vpPSStar);
    }


    private void handleTab() {
        //Fragment放入list
        LandlordstatusFragment Landlordstatus=new LandlordstatusFragment(user,selectUser);
        TenantstatusFragment Tenantstatus=new TenantstatusFragment(user,selectUser);
        tabList.add(Landlordstatus);
        tabList.add(Tenantstatus);
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

}
