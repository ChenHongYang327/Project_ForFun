package idv.tfp10105.project_forfun.discussionboard.controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.KeyboardUtils;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Post;

import static android.app.Activity.RESULT_OK;

public class DiscussionInsertFragment extends Fragment {
    private static final String TAG = "TAG_dis_InsertFragment";
    private FragmentActivity activity;
    private EditText etTitle, etContext;
    private ImageButton insert_bt_push;
    private CircularImageView insert_bt_memberHead;
    private TextView insert_MemberName, insert_board;
    private Spinner insert_spinner;
    private String imagePath = "Project_ForFun/Discussion_insert/no_image.jpg";
    private FirebaseStorage storage;
    private byte[] image;
    private File file;
    private Uri contentUri;
    private ImageView insert_bt_picture;
    private boolean pictureTaken;
    private String url = Common.URL ;
    private SharedPreferences sharedPreferences;
    private String name, headshot;
    private Bundle bundle;
    private Integer memberId;
    private BottomSheetDialog bottomSheetDialog;


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
        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        name = sharedPreferences.getString("name","");
        headshot = sharedPreferences.getString("headshot", "");
        memberId = sharedPreferences.getInt("memberId",-1);

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
        insert_MemberName.setText(name);
        showImage(insert_bt_memberHead ,headshot);

    }



    private void findViews(View view) {
        insert_bt_picture = view.findViewById(R.id.insert_bt_image);
        insert_bt_memberHead = view.findViewById(R.id.insert_bt_memberHead);
        insert_bt_push = view.findViewById(R.id.insert_bt_insert);
        insert_spinner = view.findViewById(R.id.insert_spinner);
        etTitle = view.findViewById(R.id.insert_et_title);
        etContext = view.findViewById(R.id.insert_et_context);
        insert_MemberName = view.findViewById(R.id.insert_memberName_text);
//        insert_board = view.findViewById(R.id.insert_board);

    }

    //??????spinner
    private String handleSpinner( ) {
        //index:0    index:1   index:2
        List<String> itemList = Arrays.asList("????????????", "????????????", "?????????");
        //?????????Adapter?????????????????????????????????
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, itemList);
        //????????????????????????
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //??????Adapter
        insert_spinner.setAdapter(adapter);
        //??????????????????
        insert_spinner.setSelection(0, false);

        //??????/?????? ????????????????????????
        insert_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

//                insert_board.setText(insert_spinner.getSelectedItem().toString());

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                TextView errorText = (TextView) insert_spinner.getSelectedView();
                errorText.setError("???????????????");
                //just to highlight that this is an error
                errorText.setTextColor(Color.RED);
                //changes the selected item text to this
                errorText.setText("???????????????");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, new String[]{""});
                insert_spinner.setAdapter(adapter);

            }
        });
        return  insert_spinner.getSelectedItem().toString();
    }

    private void handleInsert_bt_picture() {

        //?????????BottomSheet
        bottomSheetDialog = new BottomSheetDialog(activity);
        //???????????????
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet, null);
        //????????????????????????
        Button btCancel = view.findViewById(R.id.btCancel);
        Button bt_pickPicture = view.findViewById(R.id.btPickpic);
        Button bt_takePicture = view.findViewById(R.id.btTakepic);
        //??????????????????BottomSheet???
        bottomSheetDialog.setContentView(view);
        //??????BottomSheet????????????
        ViewGroup parent = (ViewGroup) view.getParent();
        //??????????????????????????????????????????
        parent.setBackgroundResource(android.R.color.transparent);

        //????????????
        bt_takePicture.setOnClickListener(v -> {
            //????????????
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //??????????????????
            file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            file = new File(file, "picture.jpg");
            contentUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", file);
            //????????????
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            bottomSheetDialog.dismiss();
            try {
                takePictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "???????????????????????????", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });


        bt_pickPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent);
        });

        insert_bt_picture.setOnClickListener((v)->{
            //??????BottomSheet
            bottomSheetDialog.show();
        });

        btCancel.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

        });

    }

    private void handleFinishInsert() {
        insert_bt_push.setOnClickListener(v -> {
            //??????user????????????
            String title = etTitle.getText().toString().trim();
            KeyboardUtils.hideKeyboard(activity);
            if (title.length() <= 0) {
                etTitle.setError("???????????????");
                return;
            }
            String context = etContext.getText().toString().trim();
            KeyboardUtils.hideKeyboard(activity);
            if (context.length() <= 0){
                etContext.setError("???????????????");
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                //???json????????????
                url += "DiscussionBoardController";
                Post post = new Post(0, insert_spinner.getSelectedItem().toString(), memberId , title, context, imagePath);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "postInsert");
                jsonObject.addProperty("post", new Gson().toJson(post));
                int count;
                //??????????????????
                String result = RemoteAccess.getJsonData(url, jsonObject.toString());
                //????????????
                count = Integer.parseInt(result);
                //?????????0
                if (count == 0) {
//                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();

                    //?????????????????????
                    bundle = new Bundle();
                    bundle.putString("board",insert_spinner.getSelectedItem().toString());
                    //????????????
                    Navigation.findNavController(v).popBackStack(R.id.discussionInsertFragment,true);
                    Navigation.findNavController(v).navigate(R.id.discussionBoardFragment,bundle);

                }
            } else {
                Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            //????????????
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

    //???????????????
    private void crop(Uri sourceImageUri) {
        //????????????????????????
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        //????????????URI
        Uri destinationUri = Uri.fromFile(file);
        //UCrop.of??????????????????????????????Intent??????
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri).getIntent(activity);
        //??????cropPictureLauncher ??????Ucrop
        cropPictureLauncher.launch(cropIntent);
    }

    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            //????????????????????????
            Uri resultUri = UCrop.getOutput(result.getData());
            if (resultUri != null) {
                uploadImage(resultUri);
            }
        }
        bottomSheetDialog.dismiss();
    }

    private String uploadImage(Uri resultUri) {

        //???????????????
        StorageReference rootRef = storage.getReference();
        imagePath = getString(R.string.app_name) + "/Discussion_insert/" + System.currentTimeMillis();

        //??????????????????????????????
        final StorageReference imageRef = rootRef.child(imagePath);
        //????????????????????? ??????
        imageRef.putFile(resultUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "????????????";
                        Log.d(TAG, message);
                        Toast.makeText(activity, "??????????????? " + message, Toast.LENGTH_SHORT).show();
                        //????????????????????????
                        downloadImage(imagePath);

                    } else {
                        String message = task.getException() == null ? "????????????" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
        return imagePath;
    }

    private void downloadImage(String imagePath) {
        final int ONE_MEGABYTE = 1024 * 1024 * 10;
        StorageReference imageRef = storage.getReference().child(imagePath);
        //??????????????????????????????
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        image = task.getResult();
                        //???bitmap????????????
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        insert_bt_picture.setImageBitmap(bitmap);
                    } else {
                        String message  = task.getException() == null ? "????????????" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        insert_bt_picture.setImageResource(R.drawable.no_image);
//                        Toast.makeText(activity, message , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ??????Firebase storage?????????????????????ImageView???
    private void showImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 10;
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ?
                                "????????????" + ": " + path : task.getException().getMessage() + ": " + path;
                        imageView.setImageResource(R.drawable.no_image);
                        Log.e(TAG, message);
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


