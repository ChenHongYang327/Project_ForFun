package idv.tfp10105.project_forfun.orderconfirm.ocf_houseOwner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Order;
import idv.tfp10105.project_forfun.common.bean.OtherPay;
import idv.tfp10105.project_forfun.common.bean.Publish;

public class OcrHO_payarrive extends Fragment {
    private int TAPNUMBER = 17; //此頁面編號
    private int OrderStatusNumber = 7; //訂單流程的狀態編號
    private Activity activity;
    private RecyclerView recyclerView;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private List<OtherPay> otherPays;
    private int signInId;
    private Gson gson = new Gson();
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvHint;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE); //共用的
        storage = FirebaseStorage.getInstance();
        otherPays = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ocr_h_o_payarrive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHint = view.findViewById(R.id.tv_ocrHO_payarrive_HintText);

        signInId = sharedPreferences.getInt("memberId", -1);

        recyclerView = view.findViewById(R.id.recycleview_ocrHO_payarrive);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        //顯示adapter
        //showAlls();

        //更新的功能
        swipeRefreshLayout = view.findViewById(R.id.swipe_ocrHO_payarrive);
        //下拉可更新，要配合ui元件
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //動畫開始
            swipeRefreshLayout.setRefreshing(true);
            showAlls();
            //動畫結束
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        showAlls();
    }

    //顯示全部
    private void showAlls() {
        if (!otherPays.isEmpty()) {
            otherPays.clear();
            otherPays = getOtherpayListInfo(OrderStatusNumber, signInId);
            if(otherPays.isEmpty()){
                tvHint.setText("尚未有訂單！");
            }else{
                setOrderList(otherPays);
            }
        } else {
            otherPays = getOtherpayListInfo(OrderStatusNumber, signInId);
            if(otherPays.isEmpty()){
                tvHint.setText("尚未有訂單！");
            }else{
                setOrderList(otherPays);
            }
        }
    }

    //拿後Otherpay端對應資料
    private List<OtherPay> getOtherpayListInfo(int status, int memberId) {
        List<OtherPay> otherPays = null;
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "OrderConfirm";
            //後端先拿預載資料
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("RESULTCODE", 8);
            jsonObject.addProperty("STATUS", status);
            jsonObject.addProperty("SIGNINID", memberId); //房客
            String jsonin = RemoteAccess.getJsonData(url, jsonObject.toString());

            JsonObject tmp = gson.fromJson(jsonin, JsonObject.class);
            Type listType = new TypeToken<List<OtherPay>>() {
            }.getType();

            otherPays = gson.fromJson(tmp.get("OTHERPAYLIST").getAsString(), listType);

            Log.d("ORDER", otherPays.toString());

            return otherPays;
        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //set recycleView Adapter
    private void setOrderList(List<OtherPay> otherPayList) {
        MyAdaptor myAdaptor = (MyAdaptor) recyclerView.getAdapter();
        if (myAdaptor == null) {
            myAdaptor = new MyAdaptor(activity, otherPayList);
            recyclerView.setAdapter(myAdaptor);
        }
        myAdaptor.setOtherpayList(otherPayList);
        myAdaptor.notifyDataSetChanged();
    }

    //recycleView
    private class MyAdaptor extends RecyclerView.Adapter<MyAdaptor.Holder> {
        private List<OtherPay> otherPayList;
        private Context context;
        private final int imageSize;


        public MyAdaptor(Context context, List<OtherPay> otherPayList) {
            this.context = context;
            this.otherPayList = otherPayList;
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setOtherpayList(List<OtherPay> otherPayList) {
            this.otherPayList = otherPayList;
        }


        //view Holder
        class Holder extends RecyclerView.ViewHolder {
            ImageView imgPic;
            TextView tvTitle, tvArea, tvControlText;
            ImageView btClick;

            public Holder(@NonNull View itemView) {
                super(itemView);
                imgPic = itemView.findViewById(R.id.img_ocrItrmRV_pic);
                tvTitle = itemView.findViewById(R.id.tv_ocrItrmRV_Title);
                tvArea = itemView.findViewById(R.id.tv_ocrItrmRV_Area);
                btClick = itemView.findViewById(R.id.bt_ocrItrmRV_con1);
                tvControlText = itemView.findViewById(R.id.tv_ocrItrmRV_con1);
            }

        }

        @Override
        public int getItemCount() {
            return otherPayList == null ? 0 : otherPayList.size();
        }

        @Override
        public MyAdaptor.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemview = LayoutInflater.from(activity).
                    inflate(R.layout.ocr_item_rvview, parent, false);
            return new MyAdaptor.Holder(itemview);
        }

        @Override
        public void onBindViewHolder(@NonNull  MyAdaptor.Holder holder, int position) {

            final OtherPay otherPay = otherPayList.get(position);

            Order order = getOrder(otherPay.getOtherpayId());
            int orderId = order.getOrderId();
            int publishId = order.getPublishId();
            Publish publish = getPublish(publishId);

            //圖片
            String imgPath = otherPay.getSuggestImg();
            if (imgPath != null) {
                setImgFromFireStorage(imgPath, holder.imgPic);
            } else {
                holder.imgPic.setImageResource(R.drawable.no_image);
            }

            holder.tvTitle.setText(publish.getTitle());
            //holder.tvArea.setText(publish.getAddress());

            holder.tvControlText.setText("確認資訊"); //bt上顯示的字
            holder.btClick.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("OCR", TAPNUMBER);
                bundle.putInt("PUBLISHID",publishId);
                bundle.putInt("SIGNINID", signInId);
                bundle.putInt("ORDREID",orderId);

                Navigation.findNavController(v).navigate(R.id.orderconfirm_houseSnapshot, bundle);
            });
        }
    }

    //拿刊登單物件
    private Publish getPublish(int publishId) {
        Publish publish = new Publish();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "OrderConfirm";
            //後端先拿預載資料
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("RESULTCODE", 2);
            jsonObject.addProperty("PUBLISHID", publishId);
            String jsonin = RemoteAccess.getJsonData(url, jsonObject.toString());

            JsonObject tmp = gson.fromJson(jsonin, JsonObject.class);
            String punStr = tmp.get("PUBLISH").getAsString();

            publish = gson.fromJson(punStr, Publish.class);

            Log.d("PUB", publish.toString());

            return publish;

        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    //拿order物件
    @Nullable
    private Order getOrder(int otherpayId) {
        Order order = new Order();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "OrderConfirm";
            //後端先拿預載資料
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("RESULTCODE", 7);
            jsonObject.addProperty("OTHERPAYID", otherpayId);
            String jsonin = RemoteAccess.getJsonData(url, jsonObject.toString());

            JsonObject tmp = gson.fromJson(jsonin, JsonObject.class);
            String punStr = tmp.get("ORDER").getAsString();

            order = gson.fromJson(punStr, Order.class);

            //Log.d("PUB", order.toString());

            return order;

        } else {
            Toast.makeText(activity, "網路連線失敗", Toast.LENGTH_SHORT).show();
        }
        return null;
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
}