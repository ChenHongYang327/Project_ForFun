package idv.tfp10105.project_forfun.appointment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Timestamp;
import java.util.Calendar;

import idv.tfp10105.project_forfun.MainActivity;
import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Appointment;
import idv.tfp10105.project_forfun.common.bean.Publish;

public class AppointmentFragment extends Fragment {
    MainActivity activity;
    private Gson gson;
    private Geocoder geocoder;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;

    private TextInputEditText editAppointmentDate, editAppointmentTime;
    private ImageView imgAppointmentTitle, imgAppointmentDate, imgAppointmentTime;
    private TextView textAppointmentTitle;
    private Button btnAppointmentClick;

    private int userId = 0;
    private int appointmentId = 0;
    private Publish publish;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        gson = new Gson();
        geocoder = new Geocoder(activity);
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);

        userId = sharedPreferences.getInt("memberId",-1);

        return inflater.inflate(R.layout.fragment_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int publishId = getArguments() != null ? getArguments().getInt("publishId") : 0;
//        Log.d("home", "publishId = " + publishId);
        publish = getPublishDataById(publishId);
//        Log.d("home", "publish = " + gson.toJson(publish));

        imgAppointmentTitle = view.findViewById(R.id.imgAppointmentTitle);
        textAppointmentTitle = view.findViewById(R.id.textAppointmentTitle);

        editAppointmentDate = view.findViewById(R.id.editAppointmentDate);
        editAppointmentTime = view.findViewById(R.id.editAppointmentTime);
        imgAppointmentDate = view.findViewById(R.id.imgAppointmentDate);
        imgAppointmentTime = view.findViewById(R.id.imgAppointmentTime);

        btnAppointmentClick = view.findViewById(R.id.btnAppointmentClick);

        setPublishData(publish);
        handleAppointmentPicker();
        handleButton();
    }

    private Publish getPublishDataById(int publishId) {
        Publish publish = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/getPublishData";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getByPublishId");
            request.addProperty("publishId", publishId);

            String jsonResule = RemoteAccess.getJsonData(url, gson.toJson(request));

            JsonObject response = gson.fromJson(jsonResule, JsonObject.class);
            String publishJson = response.get("publish").getAsString();
            publish = gson.fromJson(publishJson, Publish.class);
        }

        return publish;
    }

    //下載Firebase storage的照片
    public void getImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 6; //設定上限
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Toast.makeText(activity, "圖片取得錯誤", Toast.LENGTH_SHORT).show();
                        Log.d("顯示Firebase取得圖片的錯誤", errorMessage);

                    }
                });

    }

    private void setPublishData(Publish publish) {
        getImage(imgAppointmentTitle, publish.getTitleImg());
        textAppointmentTitle.setText(publish.getTitle());
    }

    private void handleAppointmentPicker() {
        imgAppointmentDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    activity,
                    (view1, year, month, dayOfMonth) -> {
                        editAppointmentDate.setText(String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );


            // 透過DatePicker來設定可選的日期範圍
            DatePicker datePicker = datePickerDialog.getDatePicker();
            // 設定最小日期
            datePicker.setMinDate(calendar.getTimeInMillis());
            // 設定最大日期
            calendar.add(Calendar.MONTH, 1);
            datePicker.setMaxDate(calendar.getTimeInMillis());

            // 顯示DatePickerDialog
            datePickerDialog.show();

            // 設定按鈕文字顏色
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        });

        imgAppointmentTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    activity,
                    (view12, hourOfDay, minute) -> {
                        editAppointmentTime.setText(String.format("%02d:%02d:00", hourOfDay, minute));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );

            timePickerDialog.show();

            // 設定按鈕文字顏色
            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        });
    }

    private void handleButton() {
        btnAppointmentClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canSubmit = true;

                // 各種資料檢查
                if (editAppointmentDate.getText().toString().trim().isEmpty()) {
                    editAppointmentDate.setError("請選擇日期");
                    canSubmit = false;
                }

                if (editAppointmentTime.getText().toString().trim().isEmpty()) {
                    editAppointmentTime.setError("請選擇時間");
                    canSubmit = false;
                }

                if (!canSubmit) {
                    Toast.makeText(activity, "請填入必填資訊", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 資料整理
                String dateTime = editAppointmentDate.getText().toString().trim() + " " + editAppointmentTime.getText().toString().trim();
//                Log.d("home", dateTime);
                Timestamp appointmentTime = Timestamp.valueOf(dateTime);
//                Log.d("home", appoinemntTime.toString());

                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentId);
                appointment.setPublishId(publish.getPublishId());
                appointment.setOwnerId(publish.getOwnerId());
                appointment.setTenantId(userId);
                appointment.setAppointmentTime(appointmentTime);
                appointment.setRead(false);

//                Log.d("home", gson.toJson(appointment));

                //往server送資料
                if (RemoteAccess.networkCheck(activity)) {
                    String url = Common.URL + "/appoiment";
                    JsonObject request = new JsonObject();
                    request.addProperty("action", "makeAppointment");
                    request.addProperty("appointment", gson.toJson(appointment));

                    String jsonIn = RemoteAccess.getJsonData(url, gson.toJson(request));

                    JsonObject response = gson.fromJson(jsonIn, JsonObject.class);
                    if ("1".equals(response.get("result_code").getAsString())) {
                        Toast.makeText(activity, "預約新增/修改成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "預約新增/修改失敗", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}