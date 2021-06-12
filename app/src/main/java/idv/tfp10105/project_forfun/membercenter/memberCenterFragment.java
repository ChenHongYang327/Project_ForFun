package idv.tfp10105.project_forfun.membercenter;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import idv.tfp10105.project_forfun.R;

public class memberCenterFragment extends Fragment {
    private Activity activity;
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

    private void findeView(View view) {
    }
}