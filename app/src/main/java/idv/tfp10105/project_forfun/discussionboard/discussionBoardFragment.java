package idv.tfp10105.project_forfun.discussionboard;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
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


public class discussionBoardFragment extends Fragment {
    private Activity activity;
    private List<Fragment> list = new ArrayList<>();
    private String[] title = {"租屋交流", "知識問答", "需求單"};
    private ViewPager2 viewPager;
    private TabLayout disTab;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_discussion_board, container, false);
        findViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        viewPager.setAdapter(new MyAdapter(activity));
        TabLayoutMediator tab = new TabLayoutMediator(disTab, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("租屋交流");
                        break;
                    case 1:
                        tab.setText("知識問答");
                        break;
                    case 2:
                        tab.setText("需求單");
                        break;
                }
            }
        });
        tab.attach();
    }

    private void findViews(View view) {
        viewPager = view.findViewById(R.id.dis_viewPage2);
        disTab = view.findViewById(R.id.dis_tabview);
    }

    public class MyAdapter extends FragmentStateAdapter {
        public MyAdapter(@NonNull Activity fragmentActivity) {
            super((FragmentActivity) fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {

           switch (position)
           {
               case 0:
                   return new Fragment(R.layout.fragment_discussion_board_rent_house);
               case 1:
                   return new Fragment(R.layout.fragment_discussion_board_knowledge);
               default:
                   return new Fragment(R.layout.fragment_discussion_board_rent_seeking);
           }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}