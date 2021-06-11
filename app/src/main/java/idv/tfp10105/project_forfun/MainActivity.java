package idv.tfp10105.project_forfun;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        handleView();
    }

    private void handleView() {
        NavController navController;
        ActionBar actionBar;
        View view;
        ImageView imgBell;//actionbar 中的ImageView
        navController= Navigation.findNavController(this, R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        actionBar=getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  //客製化actionbar主題
            actionBar.setCustomView(R.layout.actionbar_layout);         //客製化actionbar
            view=actionBar.getCustomView();                             //取得view
            imgBell=view.findViewById(R.id.imgBell);              //取得元件
            imgBell.setOnClickListener(v->{
                Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
            });
        }
    }



}