package idv.tfp10105.project_forfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
        TextView tvTitle;//actionbar 中的TextView
        navController= Navigation.findNavController(this, R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        actionBar=getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  //客製化actionbar主題
            actionBar.setCustomView(R.layout.actionbar_layout);         //客製化actionbar
            view=actionBar.getCustomView();                             //取得view
            imgBell=view.findViewById(R.id.imgBell);              //取得元件
            tvTitle=view.findViewById(R.id.tvTitle);
            tvTitle.setText("首頁");
            imgBell.setOnClickListener(v->{
                Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
            });
            //設定Title
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                if (item.getOrder()==1) {
                    tvTitle.setText("首頁");
                    return true;
                }
                else if (item.getOrder()==2){
                    tvTitle.setText("討論區");
                    return true;
                }
                else if (item.getOrder()==3){
                    tvTitle.setText("刊登房屋");
                    return true;
                }
                else if (item.getOrder()==4){
                    tvTitle.setText("私訊");
                    return true;
                }
                else if (item.getOrder()==5){
                    tvTitle.setText("討論區");
                    return true;
                }
                return true;
                    });
        }
    }



}