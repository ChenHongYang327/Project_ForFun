package idv.tfp10105.project_forfun;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private NavController navController;


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
//        SharedPreferences sharedPreferences;
//        int role;
//        sharedPreferences = getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);
        //介面
        ActionBar actionBar;
        View view;
        ImageButton btBell;//actionbar 中的ImageView
        TextView tvTitle;//actionbar 中的TextView
        navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  //客製化actionbar主題
            actionBar.setCustomView(R.layout.actionbar_layout);         //客製化actionbar
            view = actionBar.getCustomView();                             //取得view
            btBell = view.findViewById(R.id.btBell);              //取得元件
            tvTitle = view.findViewById(R.id.tvTitle);
            btBell.setOnClickListener(v -> {
                Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
            });
            //navController監聽器
            //設置actionbar title及顯示狀態
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                tvTitle.setText(Objects.requireNonNull(navController.getCurrentDestination()).getLabel());
                //bottom功能管理
//                role =sharedPreferences.getInt("role",-1);
//                  if (role == 3) {
//                        Toast.makeText(this, "進", Toast.LENGTH_SHORT).show();
//                        bottomNavigationView.getMenu().findItem(R.id.publishFragment).setEnabled(true);
//                        bottomNavigationView.getMenu().findItem(R.id.discussionBoardFragment).setEnabled(true);
//                    }
                //隱藏bottomNav的頁面(未完成五個主頁面)
                if(navController.getCurrentDestination().getId()==R.id.homeFragment||
                    navController.getCurrentDestination().getId()==R.id.memberCenterFragment||
                    navController.getCurrentDestination().getId()==R.id.discussionBoardFragment ||
                    navController.getCurrentDestination().getId() == R.id.publishFragment
                ){
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
                else {
                    bottomNavigationView.setVisibility(View.GONE);
                }
                //隱藏actionbar的頁面
                if(navController.getCurrentDestination().getId()==R.id.signinInFragment||
                    navController.getCurrentDestination().getId()==R.id.registIntroductionFragment||
                    navController.getCurrentDestination().getId()==R.id.registerFragment
                ){
                    actionBar.hide();
                }
                else{
                    actionBar.show();
                }
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
        //未同意的權限
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