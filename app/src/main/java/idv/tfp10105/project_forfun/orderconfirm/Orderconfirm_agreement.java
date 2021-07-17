package idv.tfp10105.project_forfun.orderconfirm;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Agreement;

public class Orderconfirm_agreement extends Fragment {
    private Activity activity;

    private TextView tvDateStart, tvDateEnd, tvRent, tvSignHO, tvSignCus, tvAddress, tvConfirmText, tvCancelText;
    private ImageView btConfirm, btCancel, imgSignHO, imgSignCus;
    private Bitmap bitmapSignHO, bitmapSign;
    private int orderId, resultcode, agreementId;
    private String dateStart, dateEnd, result_Add, imgPath, address;
    private Bundle bundleIn = getArguments();
    private Gson gson = new Gson();
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private String url = Common.URL + "Agreement";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
    private DateFormat sdfSave = new SimpleDateFormat("yyyy - mm - dd hh:mm:ss");
    private Date dateS = new Date();
    private Date dateE = new Date();
    private byte[] bytes;
    private Boolean ifdown = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE); //共用的
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderconfirm_agreement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //參照元件
        tvDateStart = view.findViewById(R.id.tv_ocrAgreement_dateStart);
        tvDateEnd = view.findViewById(R.id.tv_ocrAgreement_dateEnd);
        tvRent = view.findViewById(R.id.tv_ocrAgreement_rent);
        tvSignHO = view.findViewById(R.id.tv_ocrAgreement_HOCanvas_Text);
        tvSignCus = view.findViewById(R.id.tv_ocrAgreement_Canvas_Text);
        btCancel = view.findViewById(R.id.bt_ocrAgreement_cancel);
        btConfirm = view.findViewById(R.id.bt_ocrAgreement_confirm);
        imgSignHO = view.findViewById(R.id.imgview_ocrAgreement_HOCanvas);
        imgSignCus = view.findViewById(R.id.imgview_ocrAgreement_Canvas);
        tvAddress = view.findViewById(R.id.tv_ocrAgreement_add);
        tvConfirmText = view.findViewById(R.id.tv_ocrAgreement_confirmText);
        tvCancelText = view.findViewById(R.id.tv_ocrAgreement_cancelText);

        int tapNum = bundleIn.getInt("OCR");
        orderId = bundleIn.getInt("ORDERID");
        agreementId = bundleIn.getInt("AGREEMENTID");

        switch (tapNum) {
            case 13:
                //房東頁面，拿地址，存值新建
                /**
                 * 訂單table “訂單狀態”要調整 -> 3 待簽約
                 */
                houseOwnweEvent();
                break;

            case 3:
                //房客頁面，拿值 ＆ 存簽名圖而已！
                /**
                 * 訂單table “訂單狀態”要調整 -> 4 待付款
                 */
                tenantEvent();
                break;

            case 5:
                //瀏覽模式（房東房客都簽完名了）
                btConfirm.setVisibility(View.GONE);
                tvConfirmText.setText("");

                Agreement agmt = getAgreementInfo(agreementId);
                tvDateStart.setText(sdf.format(agmt.getStartDate()));
                tvDateEnd.setText(sdf.format(agmt.getEndDate()));
                tvRent.setText(String.valueOf(agmt.getAgreementMoney()));
                String imgPathHO = agmt.getLandlordSign();
                String imgPath = agmt.getTenantSign();

                //set img
                setImgFromFireStorage(imgPathHO, imgSignHO);
                setImgFromFireStorage(imgPath, imgSignCus);
                tvSignHO.setText("");
                tvSignCus.setText("");

                //set Address
                tvAddress.setText(getAddress(orderId));
                break;

//            case 15:
//                //房東 瀏覽模式
//                btConfirm.setVisibility(View.GONE);
//                tvConfirmText.setText("");
//
//                Agreement agmtH = getAgreementInfo(agreementId);
//                tvDateStart.setText(sdf.format(agmtH.getStartDate()));
//                tvDateEnd.setText(sdf.format(agmtH.getEndDate()));
//                tvRent.setText(String.valueOf(agmtH.getAgreementMoney()));
//                String igPathHO = agmtH.getLandlordSign();
//                String igPath = agmtH.getTenantSign();
//
//                //set img
//                setImgFromFireStorage(igPathHO, imgSignHO);
//                setImgFromFireStorage(igPath, imgSignCus);
//                tvSignHO.setText("");
//                tvSignCus.setText("");
//
//                //set Address
//                tvAddress.setText(getAddress(orderId));
//                break;

            default:
                Bundle bundleOut = new Bundle();
                bundleOut.putInt("OCR", tapNum);
                Toast.makeText(activity, "查無內容", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.orderconfirm_houseSnapshot, bundleOut);
                break;
        }

        //取消按鈕
        btCancel.setOnClickListener(v -> {
            Bundle bundleOut = new Bundle();
            bundleOut.putInt("OCR", tapNum);
            Navigation.findNavController(v).navigate(R.id.orderconfirm_houseSnapshot, bundleOut);
        });
    }

    private void houseOwnweEvent() {

        imgSignCus.setEnabled(false);

        //檢查網路連線
        if (RemoteAccess.networkCheck(activity)) {
            //預載地址
            tvAddress.setText(getAddress(orderId));

            // set Date
            tvDateStart.setOnClickListener(view -> {
                Calendar m_Calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener datepicker = (vew, year, month, dayOfMonth) -> {
                    m_Calendar.set(Calendar.YEAR, year);
                    m_Calendar.set(Calendar.MONTH, month);
                    m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateS = m_Calendar.getTime();
                    tvDateStart.setText(sdf.format(dateS));
                };
                DatePickerDialog dialog = new DatePickerDialog(activity,
                        datepicker,
                        m_Calendar.get(Calendar.YEAR),
                        m_Calendar.get(Calendar.MONTH),
                        m_Calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            });
            tvDateEnd.setOnClickListener(view -> {
                Calendar m_Calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener datepicker = (vew, year, month, dayOfMonth) -> {
                    m_Calendar.set(Calendar.YEAR, year);
                    m_Calendar.set(Calendar.MONTH, month);
                    m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateE = m_Calendar.getTime();
                    tvDateEnd.setText(sdf.format(dateE));
                };
                DatePickerDialog dialog = new DatePickerDialog(activity,
                        datepicker,
                        m_Calendar.get(Calendar.YEAR),
                        m_Calendar.get(Calendar.MONTH),
                        m_Calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            });

            // 上傳簽名圖給firebase，路徑存回db
            imgSignHO.setOnClickListener(v -> {
                final SignatureView signatureView = new SignatureView(activity, null);
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_Design_BottomSheetDialog);
                builder.setTitle("簽名板")
                        .setView(signatureView)
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bitmap bitmap = signatureView.getContentDataURI();

                                imgSignHO.setImageBitmap(bitmap);
                                tvSignHO.setText("");

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                                bytes = bos.toByteArray();
                            }
                        })
                        .setNeutralButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            });

            // 確認後再儲存
            btConfirm.setOnClickListener(v -> {
                //上傳圖片到firebase
                imgPath = getImgPath(bytes);

                String money = tvRent.getText().toString().trim();
                int rent = Integer.parseInt(money);

                //傳去後端
                Agreement agreement = new Agreement();
                agreement.setOrderId(orderId);
                agreement.setStartDate(Timestamp.valueOf(tvDateStart.getText().toString().replace("/", "-") + " " + "00:00:00"));
                agreement.setEndDate(Timestamp.valueOf(tvDateEnd.getText().toString().replace("/", "-") + " " + "00:00:00"));
                agreement.setAgreementMoney(rent);
                agreement.setLandlordSign(imgPath);

                //resultcode 3
                JsonObject objectH = new JsonObject();
                String agmtStr = gson.toJson(agreement);
                objectH.addProperty("AGREEMENT", agmtStr);
                objectH.addProperty("RESULTCODE", 3);

                Log.d("TEST_H",objectH.toString());

                String jsonH = RemoteAccess.getJsonData(url, objectH.toString());
                JsonObject jsonIn_H = gson.fromJson(jsonH, JsonObject.class);
                resultcode = jsonIn_H.get("RESULT").getAsInt();

                if (resultcode == 200) {
                    Toast.makeText(activity, "合約建立成功", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).navigate(R.id.homeFragment);
                } else {
                    Toast.makeText(activity, "更新失敗", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private void tenantEvent() {

        imgSignHO.setEnabled(false);
        tvSignHO.setText("");
        tvAddress.setEnabled(false);
        tvRent.setEnabled(false);
        tvDateStart.setEnabled(false);
        tvDateEnd.setEnabled(false);

        //預載值
        Agreement agrmt = getAgreementInfo(agreementId);

        //要轉 agrmt.getStartDate()
        tvDateStart.setText(sdf.format(agrmt.getStartDate()));
        tvDateEnd.setText(sdf.format(agrmt.getEndDate()));
        tvRent.setText(String.valueOf(agrmt.getAgreementMoney()));

        //set Address
        tvAddress.setText(getAddress(orderId));
        //set img
        String imgPathHO = agrmt.getLandlordSign();
        setImgFromFireStorage(imgPathHO, imgSignHO);

        //簽名板
        imgSignCus.setOnClickListener(v -> {
            final SignatureView signatureView = new SignatureView(activity, null);
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_Design_BottomSheetDialog);
            builder.setTitle("簽名板")
                    .setView(signatureView)
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bitmap bitmap = signatureView.getContentDataURI();

                            imgSignCus.setImageBitmap(bitmap);
                            tvSignCus.setText("");

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            bytes = bos.toByteArray();
                        }
                    })
                    .setNeutralButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

        });

        //確認後存去後端
        btConfirm.setOnClickListener(v -> {
            //檢查網路連線
            if (RemoteAccess.networkCheck(activity)) {

                //上傳圖片到firebase
                imgPath = getImgPath(bytes);

                //resultcode 4
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("RESULTCODE", 4);
                jsonObject.addProperty("IMGPATH_T", imgPath);
                jsonObject.addProperty("AGREEMENTID", agreementId);

                String jsonin = RemoteAccess.getJsonData(url, jsonObject.toString());
                JsonObject object = gson.fromJson(jsonin, JsonObject.class);
                resultcode = object.get("RESULT").getAsInt();

                if (resultcode == 200) {
                    Toast.makeText(activity, "合約已完成，請記得付款", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).navigate(R.id.homeFragment);
                } else {
                    Toast.makeText(activity, "連線失敗", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //預覽模式 , 抓值
    //resultcode 2
    private Agreement getAgreementInfo(int agreementId) {
        if (RemoteAccess.networkCheck(activity)) {
            JsonObject object_All = new JsonObject();
            object_All.addProperty("RESULTCODE", 2);
            object_All.addProperty("AGREEMENTID", agreementId);

            String josn = RemoteAccess.getJsonData(url, object_All.toString());
            JsonObject object = gson.fromJson(josn, JsonObject.class);
            resultcode = object.get("RESULT").getAsInt();

            Agreement agreement = gson.fromJson(object.get("AGREEMENT").getAsString(), Agreement.class);

            if (resultcode != 200) {
                Toast.makeText(activity, "連線失敗", Toast.LENGTH_SHORT).show();
            }
            return agreement;

        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    //拿地址 訂單id->刊登單id->地址
    //resultcode 1
    private String getAddress(int orderId) {

        if (RemoteAccess.networkCheck(activity)) {
            //後端先拿預載資料
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("RESULTCODE", 1);
            jsonObject1.addProperty("ORDERID", orderId);
            String add = RemoteAccess.getJsonData(url, jsonObject1.toString());
            JsonObject jsonAdd = gson.fromJson(add, JsonObject.class);
            result_Add = jsonAdd.get("ADDRESS").getAsString();
            resultcode = jsonAdd.get("RESULT").getAsInt();
            if (resultcode != 200) {
                Toast.makeText(activity, "連線失敗", Toast.LENGTH_SHORT).show();
            }
            return result_Add;
        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    //上傳Firebase storage的照片
    private String getImgPath(byte[] imgbyte) {

        // 取得storage根目錄位置
        StorageReference rootRef = storage.getReference();

        String order_Id = String.valueOf(orderId);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //  回傳資料庫的路徑
        final String imagePath = getString(R.string.app_name) + "/Agreement/" + order_Id + "/" + timeStamp;
        // 建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);

        imageRef.putBytes(imgbyte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("顯示Firebase上傳圖片的狀態", "上傳成功");
                    } else {
                        String errorMessage = task.getException() == null ? "" : task.getException().getMessage();
                        Log.d("顯示Firebase上傳圖片的錯誤", errorMessage);
                    }
                });
        return imageRef.getPath();
    }

    //下載firebase 照片
    private void setImgFromFireStorage(String imgPath, ImageView showImg) {
        StorageReference imgRef = storage.getReference().child(imgPath);
        final int ONE_MEGBYTE = 1024 * 1024;
        imgRef.getBytes(ONE_MEGBYTE).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                byte[] bytes = task.getResult();
                Bitmap bitmapPc = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                showImg.setImageBitmap(bitmapPc);
            } else {
                String message = task.getException() == null ?
                        "ImgDownloadFail" + ": " + imgPath :
                        task.getException().getMessage() + ": " + imgPath;
                //Log.e("updateFragment", message);
            }
        });
    }

}