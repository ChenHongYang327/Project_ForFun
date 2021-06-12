package idv.tfp10105.project_forfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        handleView();
    }


    @Override
    protected void onStart() {
        super.onStart();
        handleAccess();
    }

    private void handleView() {
        //判斷第一次開啟
        SharedPreferences sharedPreferences;
        sharedPreferences = this.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("firstOpen",true)) {
//            Toast.makeText(this, "首次啟動", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(this, 導覽.class);
//            startActivity(intent);
        }
        sharedPreferences.edit()
                .putBoolean("firstOpen",false)
            .apply();
        //介面
        NavController navController;
        ActionBar actionBar;
        View view;
        ImageView imgBell;//actionbar 中的ImageView
        TextView tvTitle;//actionbar 中的TextView
        navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  //客製化actionbar主題
            actionBar.setCustomView(R.layout.actionbar_layout);         //客製化actionbar
            view = actionBar.getCustomView();                             //取得view
            imgBell = view.findViewById(R.id.imgBell);              //取得元件
            tvTitle = view.findViewById(R.id.tvTitle);
            tvTitle.setText("首頁");
            imgBell.setOnClickListener(v -> {
                Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
            });
            //設定Title
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                if (item.getOrder() == 1) {
                        tvTitle.setText(R.string.home);
                    } else if (item.getOrder() == 2) {
                        tvTitle.setText(R.string.discuss_area);
                    } else if (item.getOrder() == 3) {
                        tvTitle.setText(R.string.publish);
                    } else if (item.getOrder() == 4) {
                        tvTitle.setText(R.string.message);
                    } else if (item.getOrder() == 5) {
                        tvTitle.setText(R.string.member_center);
                    }
                    return NavigationUI.onNavDestinationSelected(item, navController);
            });
        }
    }


    private void handleAccess() {
        //需使用者允許的權限
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};
        //不同意的權限
        Set<String> permissionsRequest = new HashSet<>();
        //過濾權限狀態
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }
        //詢問權限 (跳出對話框)
        if (permissionsRequest.size() != 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
                for (int index = 0; index < grantResults.length; index++) {
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        // 已同意權限的後續處理
                    } else {
                        Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
                        //讓使用者跳轉到應用程式設定開啟權限
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                    }
                }
        }
    }
}