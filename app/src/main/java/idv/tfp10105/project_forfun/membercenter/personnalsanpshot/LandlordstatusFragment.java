package idv.tfp10105.project_forfun.membercenter.personnalsanpshot;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.PersonEvaluation;


public class LandlordstatusFragment extends Fragment {
    private Activity activity;
    private List<PersonEvaluation> personEvaluations  =new ArrayList<>();
    private Member user,selectUser;
    private RecyclerView rvLand;

    public LandlordstatusFragment(Member user, Member selectUser) {
        this.user = user;
        this.selectUser = selectUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_landlordstatus, container, false);
        findView(view);
        handleLanData();
        return view;
    }

    private void findView(View view) {
        rvLand=view.findViewById(R.id.rvLand);
    }

    private void handleLanData() {
    }
}