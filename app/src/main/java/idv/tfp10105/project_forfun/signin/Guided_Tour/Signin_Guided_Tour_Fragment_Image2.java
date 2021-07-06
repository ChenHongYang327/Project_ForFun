package idv.tfp10105.project_forfun.signin.Guided_Tour;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import idv.tfp10105.project_forfun.R;


public class Signin_Guided_Tour_Fragment_Image2 extends Fragment {
    private Activity activity;
    private ImageButton signin_skip;
    private SharedPreferences sharedPreferences;
    private int role;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);
        role=sharedPreferences.getInt("role",-1);
        return inflater.inflate(R.layout.fragment_signin__guided__tour___image2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.signin_skip).setOnClickListener(v->{
            if(role==-1) {
                Navigation.findNavController(v).popBackStack();
                Navigation.findNavController(v)
                        .navigate(R.id.signinInFragment);
            }
            else{
                Navigation.findNavController(v).popBackStack();
            }
        });
    }
}