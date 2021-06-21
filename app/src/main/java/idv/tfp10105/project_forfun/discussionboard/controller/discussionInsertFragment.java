package idv.tfp10105.project_forfun.discussionboard.controller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import idv.tfp10105.project_forfun.R;

import static android.app.Activity.RESULT_OK;

public class discussionInsertFragment extends Fragment {
    private static final String TAG = "TAG_dis_InsertFragment";
    private FragmentActivity activity;
    private EditText etTitle, etContext;
    private ImageButton insert_bt_picture, insert_bt_push, insert_bt_memberHead;
    private TextView insert_MemberName, insert_board;
    private Spinner insert_spinner;
    private String imagePath;
    private FirebaseStorage storage;
    private byte[] image;
    private File file;
    private Uri contentUri;

    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion_insert, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleSpinner();
        handleInsert_bt_picture();
        handleFinishInsert();

    }

    private void findViews(View view) {
        insert_bt_picture = view.findViewById(R.id.insert_bt_image);
        insert_bt_memberHead = view.findViewById(R.id.insert_bt_memberHead);
        insert_bt_push = view.findViewById(R.id.insert_bt_insert);
        insert_spinner = view.findViewById(R.id.insert_spinner);
        etTitle = view.findViewById(R.id.insert_et_title);
        etContext = view.findViewById(R.id.insert_et_title);
        insert_MemberName = view.findViewById(R.id.insert_memberName_text);
        insert_board = view.findViewById(R.id.insert_board);

    }

    private void handleSpinner() {
        List<String> itemList = Arrays.asList("租屋交流", "知識問答", "需求單");
        //實例化Adapter物件，並設定選項的外觀
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, itemList);
        //設定展開時的外觀
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //設定Adapter
        insert_spinner.setAdapter(adapter);
        //設定預選選項
        insert_spinner.setSelection(0, true);

        //註冊/實作 選項被選取監聽器
        insert_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sPos = String.valueOf(position);
                String sInfo = parent.getItemAtPosition(position).toString();
                insert_board.setText(sInfo);

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                TextView errorText = (TextView) insert_spinner.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);//just to highlight that this is an error
                errorText.setText("請選擇板塊");//changes the selected item text to this

                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, new String[]{""});
                insert_spinner.setAdapter(adapter);

            }
        });
    }

    private void handleInsert_bt_picture() {

        //初始化BottomSheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        //連結的介面
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet, null);
        Button btCancel = view.findViewById(R.id.btCancel);
        Button bt_pickPicture = view.findViewById(R.id.btPickpic);
        Button bt_takePicture = view.findViewById(R.id.btTakepic);
        //將介面載入至BottomSheet內
        bottomSheetDialog.setContentView(view);
        //取得BottomSheet介面設定
        ViewGroup parent = (ViewGroup) view.getParent();
        //將背景設為透明，否則預設白底
        parent.setBackgroundResource(android.R.color.transparent);

        bt_takePicture.setOnClickListener(v -> {
            //開啟拍照
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //指定儲存路徑
            file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            file = new File(file, "picture.jpg");
            contentUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
            //取得原圖
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "no camera app found", Toast.LENGTH_SHORT).show();
            }
        });


        bt_pickPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        });

        btCancel.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        insert_bt_picture.setOnClickListener((v)->{
            //顯示BottomSheet
            bottomSheetDialog.show();
        });
    }

    private void handleFinishInsert() {
        insert_bt_push.setOnClickListener(v -> {
            //取得user輸入的值
            String title = etTitle.getText().toString().trim();
            if (title.length() == 0) {
                Toast.makeText(activity, "Title is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            String context = etTitle.getText().toString().trim();
            Toast.makeText(activity, "context is invalid", Toast.LENGTH_SHORT).show();
            return;
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
        imagePath = getString(R.string.app_name) + "/Discussion_insert/" + System.currentTimeMillis();
        //建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);
        //將儲存照片上傳 檔案
        imageRef.putFile(resultUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "上傳成功";
                        Log.d(TAG, message);
                        Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
                        //下載剛上傳的照片
                        downloadImage(imagePath);
                    } else {
                        String message = task.getException() == null ? "Upload fail" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
        return imagePath;
    }

    private void downloadImage(String imagePath) {
        final int ONE_MEGABYTE = 1024 * 1024;
        StorageReference imageRef = storage.getReference().child(imagePath);
        //最多能暫存記憶體的量
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        image = task.getResult();
                        //轉bitmap呈現前端
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        insert_bt_picture.setImageBitmap(bitmap);
                    } else {
                        String message  = task.getException() == null ? "Download fail" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(activity, message , Toast.LENGTH_SHORT).show();
                    }
                });

    }

}

