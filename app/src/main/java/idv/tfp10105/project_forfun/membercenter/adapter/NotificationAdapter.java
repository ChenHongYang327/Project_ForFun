package idv.tfp10105.project_forfun.membercenter.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Notification;
import idv.tfp10105.project_forfun.common.bean.Post;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHodler> {
   private Activity activity;
   private List<Notification> notifications;
   private List<String> customersHeadShot;

    public NotificationAdapter(Activity activity, List<Notification> notifications, List<String> customersHeadShot) {
        this.activity = activity;
        this.notifications = notifications;
        this.customersHeadShot = customersHeadShot;
    }

    @NonNull
    @Override
    public NotificationHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(activity).inflate(R.layout.notification_itemview,parent,false);
        return new NotificationHodler(view);
    }

    @Override
    public int getItemCount() {
        return notifications==null?0:notifications.size();
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHodler holder, int position) {
        Notification notification=notifications.get(position);
        String headShot=customersHeadShot.get(position)==null?"/Project_ForFun/no image.jpg":customersHeadShot.get(position);
        //提醒者的頭像
        getImage(holder.ivNotification,headShot);
        final String url= Common.URL+"NotificationController";
        //取得Title
        JsonObject jsonObject=new JsonObject();
        //設定提醒內容
        if(notification.getCommentId()!=0){
            jsonObject.addProperty("action","getPostTitle");
            jsonObject.addProperty("commentId",notification.getCommentId());
            String postTitle=RemoteAccess.getJsonData(url,jsonObject.toString());
            holder.tvNotificationTitle.setText("您的"+"「"+postTitle+"」"+"文章有新留言");
        }
        else if(notification.getAppointmentId()!=0){
            jsonObject.addProperty("action","getPublishTitle");
            jsonObject.addProperty("appointmentId",notification.getAppointmentId());
            String publishTitle=RemoteAccess.getJsonData(url,jsonObject.toString());
            holder.tvNotificationTitle.setText("您的"+"「"+ publishTitle+"」"+"刊登單有新的看房預約");
        }
        else if(notification.getOrderId()!=0){
            holder.tvNotificationTitle.setText("您有一筆新的訂單");
        }
        else if(notification.getMessageId()!=0){
            holder.tvNotificationTitle.setText("您有一則新訊息");
        }
        holder.tvNotificationCreatTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.TAIWAN).format(notification.getCreateTime()));
        //提醒狀態
        if(notification.getRead()){
            holder.tvNotificationReadStatus.setText("已讀");
            holder.tvNotificationReadStatus.setTextColor(activity.getResources().getColor(R.color.black));
            holder.cvNotification.setForeground(activity.getResources().getDrawable(R.drawable.publishlist_shade));
        }
        else{
            holder.tvNotificationReadStatus.setText("未讀");
            holder.tvNotificationReadStatus.setTextColor(activity.getResources().getColor(R.color.red));
            holder.cvNotification.setForeground(null);
        }
        holder.cvNotification.setOnClickListener(v->{
            if(RemoteAccess.networkCheck(activity)) {
                if (notification.getCommentId() != 0) {
                    JsonObject req=new JsonObject();
                    req.addProperty("action","getPostId");
                    req.addProperty("commentId",notification.getCommentId());
                    Post post=new Gson().fromJson(RemoteAccess.getJsonData(url,req.toString()),Post.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("post",post);
                    Navigation.findNavController(v).navigate(R.id.discussionDetailFragment,bundle);
                } else if (notification.getAppointmentId() != 0) {
                    //看房預約單
                } else if (notification.getOrderId() != 0) {
                    //新訂單
                } else if (notification.getMessageId() != 0) {
                    //新訊息
                }
            }
            else {
                Toast.makeText(activity, "請檢察網路連線", Toast.LENGTH_SHORT).show();
            }
        });
    }



    class NotificationHodler extends RecyclerView.ViewHolder{
        CardView cvNotification;
        CircularImageView ivNotification;
        TextView tvNotificationTitle,tvNotificationCreatTime,tvNotificationReadStatus;
        public NotificationHodler(@NonNull View itemView) {
            super(itemView);
            ivNotification=itemView.findViewById(R.id.ivNotification);
            tvNotificationTitle=itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationCreatTime=itemView.findViewById(R.id.tvNotificationCreatTime);
            tvNotificationReadStatus=itemView.findViewById(R.id.tvNotificationReadStatus);
            cvNotification=itemView.findViewById(R.id.cvNotification);

        }
    }

    //下載Firebase storage的照片
    public void getImage(final ImageView imageView, final String path) {
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
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

}
