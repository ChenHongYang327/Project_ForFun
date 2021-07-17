package idv.tfp10105.project_forfun.orderconfirm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.Publish;

public class Orderconfirm_houseSnapshot extends Fragment {
    private Activity activity;
    private ImageView btcon0, btCon1, btCon2, btCon3, btConn;
    private TextView tvcon0, tvCon1, tvCon2, tvCon3, tvConntText,tv_bottomsheet_Title;
    private Button bt_bottomsheet_Confirm, bt_bottomsheet_Cancel;
    private TextView tvTitle, tvArea, tvSquare, tvType, tvName;
    private ImageView imgPic, imgHeadShot;
    private Gson gson = new Gson();
    private int sidninId, orderId, publishId, orderStatus,ocr_agmt;
    private FirebaseStorage storage;
    private Member member;
    // BottomSheet
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;
    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences("OrderSharedPre", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orderconfirm_house_snapshot, container, false);
        // BottomSheet
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_housesnap, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        ViewGroup parent = (ViewGroup) bottomSheetView.getParent();
        parent.setBackgroundResource(android.R.color.transparent);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tv_HSShot_Title);
        tvArea = view.findViewById(R.id.tv_HSShot_Area);
        tvSquare = view.findViewById(R.id.tv_HSShot_Square);
        tvType = view.findViewById(R.id.tv_HSShot_Type);
        tvName = view.findViewById(R.id.tv_HSShot_Name);
        tvCon1 = view.findViewById(R.id.tv_HSShot_con1);
        tvCon2 = view.findViewById(R.id.tv_HSShot_con2);
        tvCon3 = view.findViewById(R.id.tv_HSShot_con3);
        tvcon0 = view.findViewById(R.id.tv_HSShot_con0);
        tvConntText = view.findViewById(R.id.tv_HSShot_ConnLandloreText);
        btcon0 = view.findViewById(R.id.bt_HSShot_con0);
        btCon1 = view.findViewById(R.id.bt_HSShot_con1);
        btCon2 = view.findViewById(R.id.bt_HSShot_con2);
        btCon3 = view.findViewById(R.id.bt_HSShot_con3);
        btConn = view.findViewById(R.id.bt_HSShot_Conn);
        imgPic = view.findViewById(R.id.img_HSShot_pic1);
        imgHeadShot = view.findViewById(R.id.img_HSShot_Headshot);

        //bottomsheet
        tv_bottomsheet_Title = bottomSheetView.findViewById(R.id.tv_bottomsheet_TitleText);
        bt_bottomsheet_Confirm = bottomSheetView.findViewById(R.id.bt_bottomsheet_Comfirm);
        bt_bottomsheet_Cancel = bottomSheetView.findViewById(R.id.bt_bottomsheet_close);

        // 拿前頁bundle 來的值
        Bundle bundle = getArguments();
        sidninId = bundle.getInt("SIGNINID", -1);
        publishId = bundle.getInt("PUBLISHID", -1);
        orderId = bundle.getInt("ORDERID", -1);
        int ocrId = bundle.getInt("OCR",-1);

//        sidninId = 3;
//        publishId = 9;
//        orderId = 2;
//        int ocrId = 2;

        //判斷跳轉頁面來源
        switch (ocrId) {
//            case 1:
//                preloadInfo();
//                ocrReserve1();
//                break;
            case 2:
                preloadInfo();
                ocrOrder2();
                break;
            case 3:
                preloadInfo();
                ocrSign3();
                break;
            case 4:
                preloadInfo();
                ocrPay4();
                break;
            case 5:
                preloadInfo();
                ocrComplete5();
                break;
//            case 6: //取消做在RecycleView上 連到詳細資訊去重新預約
//                preloadInfo();
//                ocrCancel6();
//                break;
//            case 7:
//                preloadInfo();
//                ocrPaid7();
//                break;
//            case 11:
//                preloadInfo();
//                ocrHOReserve11();
//                break;
            case 12:
                preloadInfo();
                ocrHOOrder12();
                break;
            case 13:
                preloadInfo();
                ocrHOSign13();
                break;
            case 14:
                preloadInfo();
                ocrHOPay14();
                break;
            case 15:
                preloadInfo();
                ocrHOCompelete15();
                break;
//            case 16: //取消做在RecycleView上 連到詳細資訊去重新預約
//                preloadInfo();
//                ocrHOCancel16();
//                break;
//            case 17:
//                preloadInfo();
//                ocrHOPayarrive17();
//                break;
            default:
                Toast.makeText(activity, "查無資料", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.homeFragment);
                break;
        }

        //跳轉 聯絡人
        btConn.setOnClickListener(v->{
            Bundle bundle2PerSnap = new Bundle();
            bundle2PerSnap.putSerializable("SelectUser",member);
            Navigation.findNavController(v).navigate(R.id.personalSnapshotFragment,bundle2PerSnap);
        });
    }

//    private void ocrReserve1() {
//        btCon3.setVisibility(View.GONE);
//        tvCon1.setText("修改預約");
//        tvCon2.setText("取消預約");
//
//        btcon0.setOnClickListener(v -> {
//            // Navigation.findNavController(v).navigate(R.id.homeFragment);
//        });
//        btCon1.setOnClickListener(v -> {
//            //  Navigation.findNavController(v).navigate();
//        });
//        btCon2.setOnClickListener(v -> {
//            Toast.makeText(activity, "cancel", Toast.LENGTH_SHORT).show();
//        });
//    }

    private void ocrOrder2() {
        btCon3.setVisibility(View.GONE);
        tvCon1.setText("確認下訂");
        tvCon2.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            tv_bottomsheet_Title.setText("是否下訂？");
            bottomSheetDialog.show();
            bt_bottomsheet_Confirm.setOnClickListener(view->{
                //改變訂單狀態2->12  跳完回首頁
                orderStatus = 12;
                orderChangeStatus(orderStatus);
                Navigation.findNavController(v).navigate(R.id.homeFragment);
            });
            bt_bottomsheet_Cancel.setOnClickListener(view->{
                bottomSheetDialog.dismiss();
            });
        });
        btCon2.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrSign3() {
        btCon3.setVisibility(View.GONE);
        tvCon1.setText("我要簽約");
        tvCon2.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 3;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrPay4() {
        tvCon1.setText("我的合約");
        tvCon2.setText("我要付款");
        tvCon3.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 5;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(v -> {
            //帶職＆刪除頁面
            sharedPreferences.edit().putInt("ORDERID",orderId);
            sharedPreferences.edit().putString("TAB","order");
            Intent intent = new Intent(getActivity(), TappayActivity.class);
            startActivity(intent);
            Navigation.findNavController(v).popBackStack(R.id.orderconfirm_houseSnapshot,true);
        });
        btCon3.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrComplete5() {
        tvCon1.setText("我的合約");
        tvCon2.setText("我要續租"); //跳去預約
        tvCon3.setText("我要評分");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 5;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(this::navgateToPublishDetail);
        btCon3.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("SCORE",5);
            bundle.putInt("ORDERID",orderId);
            Navigation.findNavController(v).navigate(R.id.orderconfirm_score,bundle);
        });
    }

//    private void ocrCancel6() {
//        tvCon1.setText("我的合約");
//        tvCon2.setText("我要續租");
//        tvCon3.setText("我要評分");
//
//    }

//    private void ocrPaid7() {
//        tvCon1.setText("我的合約");
//        tvCon2.setText("我要續租");
//        tvCon3.setText("我要評分");
//
//    }

//    private void ocrHOReserve11() {
//        btCon3.setVisibility(View.GONE);
//        tvConntText.setText("聯絡房客");
//        tvCon1.setText("確認預約");
//        tvCon2.setText("取消預約");
//
//        btcon0.setOnClickListener(v -> {
//
//        });
//        btCon1.setOnClickListener(v -> {
//
//        });
//        btCon2.setOnClickListener(v -> {
//
//        });
//
//    }

    private void ocrHOOrder12() {
        btCon3.setVisibility(View.GONE);
        tvConntText.setText("聯絡房客");
        tvCon1.setText("確認下定");
        tvCon2.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            tv_bottomsheet_Title.setText("是否同意房客訂單？");
            bottomSheetDialog.show();
            bt_bottomsheet_Confirm.setOnClickListener(view->{
                Toast.makeText(activity,"請去開啟合約",Toast.LENGTH_SHORT).show();
                //改變訂單狀態12->12  跳完回首頁
                orderStatus = 13;
                orderChangeStatus(orderStatus);
                Navigation.findNavController(v).navigate(R.id.homeFragment);
            });
            bt_bottomsheet_Cancel.setOnClickListener(view->{
                bottomSheetDialog.dismiss();
            });
        });
        btCon2.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrHOSign13() {
        btCon3.setVisibility(View.GONE);
        tvConntText.setText("聯絡房客");
        tvCon1.setText("建立合約");
        tvCon2.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 13;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrHOPay14() {
        tvConntText.setText("聯絡房客");
        tvCon1.setText("我的合約");
        tvCon2.setText("產生付款連結");
        tvCon3.setText("取消此次交易");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 5;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(v -> {
            Toast.makeText(activity,"已產生連結，待房客付款",Toast.LENGTH_SHORT).show();
        });
        btCon3.setOnClickListener(v -> {
            //改變訂單狀態->6  跳完回首頁
            orderStatus = 6;
            orderChangeStatus(orderStatus);
        });
    }

    private void ocrHOCompelete15() {
        tvConntText.setText("聯絡房客");
        tvCon1.setText("我的合約");
        tvCon2.setText("我要評分");
        tvCon3.setText("新增其他款項");

        btcon0.setOnClickListener(this::navgateToPublishDetail);
        btCon1.setOnClickListener(v -> {
            ocr_agmt = 5;
            navgateToAgreement(v,ocr_agmt);
        });
        btCon2.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("SCORE",15);
            bundle.putInt("ORDERID",orderId);
            Navigation.findNavController(v).navigate(R.id.orderconfirm_score,bundle);
        });
        btCon3.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("OCR",15);
            Navigation.findNavController(v).navigate(R.id.orderconfirm_otherpay,bundle);
        });
    }

//    private void ocrHOCancel16() {
//        tvConntText.setText("聯絡房客");
//        tvCon1.setText("我的合約");
//        tvCon2.setText("我要評分");
//        tvCon3.setText("新增其他款項");
//
//    }

//    private void ocrHOPayarrive17() {
//        tvConntText.setText("聯絡房客");
//        tvCon1.setText("我的合約");
//        tvCon2.setText("我要評分");
//        tvCon3.setText("新增其他款項");
//
//    }

    //資料預抓
    private void preloadInfo() {
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "HouseSnapsShot";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ORDERID", orderId);
            jsonObject.addProperty("SIGNINID", sidninId);
            jsonObject.addProperty("RESULTCODE", 1);

            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            JsonObject object = gson.fromJson(jsonIn, JsonObject.class);
            String memberStr = object.get("MEMBER").getAsString();
            member = gson.fromJson(memberStr, Member.class);
            String publishStr = object.get("PUBLISH").getAsString();
            Publish publish = gson.fromJson(publishStr, Publish.class);

            //set Info
            String imgPath_House = publish.getTitleImg();
            if (imgPath_House == null || imgPath_House == "") {
                imgPic.setImageResource(R.drawable.no_image);
            } else {
                setImgFromFireStorage(imgPath_House, imgPic);
            }

            String imgPath_Head = member.getHeadshot();
            if (imgPath_Head == null || imgPath_Head == "") {
                imgHeadShot.setImageResource(R.drawable.ic_account_black_48dp);
            } else {
                setImgFromFireStorage(imgPath_Head, imgHeadShot);
            }

            tvTitle.setText(publish.getTitle());
            tvArea.setText(publish.getAddress());
            tvSquare.setText(String.valueOf(publish.getSquare()));
            tvName.setText(member.getNameL() + member.getNameF());

            switch (publish.getType()) {
                case 0:
                    tvType.setText("套房");
                    break;
                case 1:
                    tvType.setText("雅房");
                    break;
                default:
                    tvType.setText("無提供資訊");
                    break;
            }
        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
    }

    //下載firebase 照片
    private void setImgFromFireStorage(final String imgPath, final ImageView showImg) {
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

    //bottomsheetEvent
    private void bottomsheetCancelEvent(int orderStatus){
        tv_bottomsheet_Title.setText("是否取消此訂單？");
        bottomSheetDialog.show();
        bt_bottomsheet_Confirm.setOnClickListener(v->{
            Toast.makeText(activity,"OK",Toast.LENGTH_SHORT).show();
            orderChangeStatus(orderStatus);
            Navigation.findNavController(v).navigate(R.id.homeFragment);
        });
        bt_bottomsheet_Cancel.setOnClickListener(v->{
            bottomSheetDialog.dismiss();
        });
    }

    //跳轉至詳細頁面
    private void navgateToPublishDetail(View view){
        Bundle bundle = new Bundle();
        bundle.putInt("publishId",publishId);
        Navigation.findNavController(view).navigate(R.id.publishDetailFragment,bundle);
    }

    // orderChangeStatus
    private void orderChangeStatus(int orderStatus) {
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "HouseSnapsShot";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ORDERID", orderId);
            jsonObject.addProperty("STATUS", orderStatus);
            jsonObject.addProperty("RESULTCODE", 2);

            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            JsonObject object = gson.fromJson(jsonIn, JsonObject.class);
            int result = object.get("RESULT").getAsInt();

            if(result == 200){
                Toast.makeText(activity, "成功", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(activity, "連線失敗", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
    }

    // 跳轉至合約頁面
    private void navgateToAgreement(View view,int ocr){
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "HouseSnapsShot";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ORDERID", orderId);
            jsonObject.addProperty("RESULTCODE", 3);

            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            JsonObject object = gson.fromJson(jsonIn, JsonObject.class);
            int agreementId = object.get("AGREEMENTID").getAsInt();
            int result = object.get("RESULT").getAsInt();

            if(result == 200){
                Bundle bundle = new Bundle();
                bundle.putInt("OCR",ocr);
                bundle.putInt("ORDERID",orderId);
                bundle.putInt("AGREEMENTID",agreementId);
                Navigation.findNavController(view).navigate(R.id.orderconfirm_agreement,bundle);
            }else {
                Toast.makeText(activity, "連線失敗", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
    }

}