package idv.tfp10105.project_forfun.membercenter.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.bean.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHodler> {
   private Context context;
   private List<Notification> notifications;
   private List<String> customersHeadShot;

    public NotificationAdapter(Context context, List<Notification> notifications, List<String> customersHeadShot) {
        this.context = context;
        this.notifications = notifications;
        this.customersHeadShot = customersHeadShot;
    }

    @NonNull
    @Override
    public NotificationHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.notification_itemview,parent,false);
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
        //設定提醒內容
        if(notification.getCommentId()!=0){
            holder.tvNotificationTitle.setText("您的文章有一則新留言");
        }
        else if(notification.getAppointmentId()!=0){
            holder.tvNotificationTitle.setText("您有一筆新的看房預約");
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
            holder.tvNotificationReadStatus.setTextColor(context.getResources().getColor(R.color.black));
            holder.cvNotification.setForeground(context.getResources().getDrawable(R.drawable.publishlist_shade));
        }
        else{
            holder.tvNotificationReadStatus.setText("未讀");
            holder.tvNotificationReadStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.cvNotification.setForeground(null);
        }
        holder.cvNotification.setOnClickListener(v->{
            //跳轉
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
                        Toast.makeText(context, "圖片取得錯誤", Toast.LENGTH_SHORT).show();
                        Log.d("顯示Firebase取得圖片的錯誤", errorMessage);

                    }
                });

    }

}
