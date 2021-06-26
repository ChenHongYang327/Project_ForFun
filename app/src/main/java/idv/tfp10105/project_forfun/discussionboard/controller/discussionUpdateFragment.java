package idv.tfp10105.project_forfun.discussionboard.controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Post;

import static android.app.Activity.RESULT_OK;

public class discussionUpdateFragment extends Fragment {
    private static final String TAG = "TAG_dis_InsertFragment";
    private FragmentActivity activity;
    private EditText update_context_edtext, update_title_edtext;
    private ImageButton update_bt_save, update_bt_memberhead;
    private TextView update_memberName_text, update_time_text ;
    private String imagePath;
    private FirebaseStorage storage;
    private byte[] image;
    private File file;
    private Uri contentUri;
    private ImageView update_bt_imageView;
    private String url = Common.URL ;
    private SharedPreferences sharedPreferences;
    private Bundle bundle;
    private Post post;


    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::takePictureResult);

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::pickPictureResult);

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::cropPictureResult);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences("PreferencesName", Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion_update, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if(bundle == null || bundle.getSerializable("post") == null) {
            Toast.makeText(activity, "沒有貼文", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }
        post = (Post) bundle.getSerializable("post");
        showPost();

        handleUpdate_bt_picture();
        handleFinishInsert();
    }


    private void findViews(View view) {
        update_bt_imageView = view.findViewById(R.id.update_imageView);
        update_bt_memberhead = view.findViewById(R.id.update_bt_memberhead);
        update_bt_save = view.findViewById(R.id.update_bt_save);
        update_title_edtext = view.findViewById(R.id.update_title_edtext);
        update_context_edtext = view.findViewById(R.id.update_context_edtext);
        update_memberName_text = view.findViewById(R.id.update_memberName_text);
        update_time_text = view.findViewById(R.id.update_time_text);

    }

    //從bundle取資料
    private void showPost() {

        if (imagePath != "") {
            showImage(post.getPostImg());
        } else {

            update_bt_imageView.setImageResource(R.drawable.no_image);
        }
        update_title_edtext.setText(post.getPostTitle());
        update_context_edtext.setText(post.getPostContext());
//        update_time_text.setText(post.getUpdateTime().toString());
    }


    private void handleUpdate_bt_picture() {

        //初始化BottomSheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        //連結的介面
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet, null);
        //自定義的三個按鈕
        Button btCancel = view.findViewById(R.id.btCancel);
        Button bt_pickPicture = view.findViewById(R.id.btPickpic);
        Button bt_takePicture = view.findViewById(R.id.btTakepic);
        //將介面載入至BottomSheet內
        bottomSheetDialog.setContentView(view);
        //取得BottomSheet介面設定
        ViewGroup parent = (ViewGroup) view.getParent();
        //將背景設為透明，否則預設白底
        parent.setBackgroundResource(android.R.color.transparent);

        //按鈕控制
        bt_takePicture.setOnClickListener(v -> {
            //開啟拍照
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //指定儲存路徑
            file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            file = new File(file, "picture.jpg");
            contentUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", file);
            //取得原圖
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "找不到相機應用程式", Toast.LENGTH_SHORT).show();
            }
        });


        bt_pickPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        });

        update_bt_imageView.setOnClickListener((v)->{
            //顯示BottomSheet
            bottomSheetDialog.show();
        });

        btCancel.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

        });

    }

    private void handleFinishInsert() {
        update_bt_save.setOnClickListener(v -> {
            //取得user輸入的值
            String context = update_context_edtext.getText().toString().trim();
            if (context.length() <= 0){
                Toast.makeText(activity, "請輸入內文", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = update_title_edtext.getText().toString().trim();
            if (context.length() <= 0) {
                Toast.makeText(activity, "請輸入標題", Toast.LENGTH_SHORT).show();
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                //用json傳至後端
                url += "DiscussionBoardController";
                int id = post.getPostId();
                post.setFiles(id, 0, title, context, imagePath);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "postUpdate");
                jsonObject.addProperty("post", new Gson().toJson(post));
                int count;
                //執行緒池物件
                String result = RemoteAccess.getJsonData(url, jsonObject.toString());
                //新增筆數
                count = Integer.parseInt(result);
                //筆數為0
                if (count == 0) {
                    Toast.makeText(activity, "修改失敗", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT).show();


                    //抽掉頁面
                    Navigation.findNavController(v).popBackStack(R.id.discussionInsertFragment,true);
                    Navigation.findNavController(v).navigate(R.id.discussionBoardFragment,bundle);

                }
            } else {
                Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            //呼叫截圖
            crop(contentUri);
        }
    }

    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                crop(result.getData().getData());
            }
        }
    }

    //來源圖路徑
    private void crop(Uri sourceImageUri) {
        //截完圖要放的路徑
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        //要儲存的URI
        Uri destinationUri = Uri.fromFile(file);
        //UCrop.of：給來源給目的並建立Intent物件
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri).getIntent(activity);
        //呼叫cropPictureLauncher 開啟Ucrop
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            //取出截完的結果圖
            Uri resultUri = UCrop.getOutput(result.getData());
            if (resultUri != null) {
                uploadImage(resultUri);
            }
        }
    }

    private String uploadImage(Uri resultUri) {

        //取得根目錄
        StorageReference rootRef = storage.getReference();
        imagePath = getString(R.string.app_name) + "/Discussion_update/" + System.currentTimeMillis();

        //建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);
        //將儲存照片上傳 檔案
        imageRef.putFile(resultUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "上傳成功";
                        Log.d(TAG, message);
                        Toast.makeText(activity, "上傳結果： " + message, Toast.LENGTH_SHORT).show();
                        //下載剛上傳的照片
                        showImage(imagePath);

                    } else {
                        String message = task.getException() == null ? "Upload fail" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
        return imagePath;
    }

//    private void downloadImage(final ImageView imageView, final String path) {
//        final int ONE_MEGABYTE = 1024 * 1024;
//        StorageReference imageRef = storage.getReference().child(path);
//        //最多能暫存記憶體的量
//        imageRef.getBytes(ONE_MEGABYTE)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        image = task.getResult();
//                        //轉bitmap呈現前端
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                        imageView.setImageBitmap(bitmap);
//                    } else {
//                        String message  = task.getException() == null ? "Download fail" : task.getException().getMessage();
//                        Log.e(TAG, "message: " + message);
//                        imageView.setImageResource(R.drawable.no_image);
//                        Toast.makeText(activity, message , Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//     下載Firebase storage的照片並顯示在ImageView上
    private void showImage(final String imagePath) {
        final int ONE_MEGABYTE = 1024 * 1024;
        StorageReference imageRef = storage.getReference().child(imagePath);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        update_bt_imageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ?
                                "Image download Failed" + ": " + imagePath : task.getException().getMessage() + ": " + imagePath;
                        Log.e(TAG, message);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}