package idv.tfp10105.project_forfun.orderconfirm;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.orderconfirm.ocf.OcrAdapter;

public class Orderconfirm_mainfragment extends Fragment {
    private AppCompatActivity activity;
    private View view;
    private SwitchCompat switchCompat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_orderconfirm_mainfragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        switchCompat = view.findViewById(R.id.orderconfirm_mainHO_switch);
//        switchCompat.setChecked(true);
//        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                Toast.makeText(activity,"switch",Toast.LENGTH_SHORT).show();
//                Navigation.findNavController(view).navigate(R.id.action_orderconfirm_mainfragment_to_orderconfirm_houseSnapshot);
//            }
//        });

        view.findViewById(R.id.bt_orderconfirm_mainfragment_HouseOwner).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_orderconfirm_mainfragment_to_orderconfirm_mainfragment_ho);

        });


        TabLayout tabLayout = view.findViewById(R.id.orderconfirm_main_tabs);
        ViewPager2 viewPager2 = view.findViewById(R.id.orderconfirm_main_viewpage2);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        OcrAdapter adapter = new OcrAdapter(fm,getLifecycle());
        viewPager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("待預約"));
        tabLayout.addTab(tabLayout.newTab().setText("待下訂"));
        tabLayout.addTab(tabLayout.newTab().setText("待簽約"));
        tabLayout.addTab(tabLayout.newTab().setText("待付款"));
        tabLayout.addTab(tabLayout.newTab().setText("已完成"));
        tabLayout.addTab(tabLayout.newTab().setText("已取消"));
        tabLayout.addTab(tabLayout.newTab().setText("已繳費"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });


    }
}