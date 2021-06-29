package idv.tfp10105.project_forfun.signin.Guided_Tour;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.signin.Guided_Tour.Signin_Guided_Tour_Fragment_Image;
import idv.tfp10105.project_forfun.signin.Guided_Tour.Signin_Guided_Tour_Fragment_Image2;
import idv.tfp10105.project_forfun.signin.Guided_Tour.Signin_Guided_Tour_Fragment_Image3;

public class FragmentAdapter extends FragmentStateAdapter {
    public FragmentAdapter(@NonNull @NotNull FragmentManager fragmentManager, @NonNull @NotNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 1:
                return new Signin_Guided_Tour_Fragment_Image2();
            case 2:
                return new Signin_Guided_Tour_Fragment_Image3();
        }

        return new Signin_Guided_Tour_Fragment_Image();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
