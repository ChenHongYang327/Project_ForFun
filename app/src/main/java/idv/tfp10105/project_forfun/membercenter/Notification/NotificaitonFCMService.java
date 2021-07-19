package idv.tfp10105.project_forfun.membercenter.Notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import idv.tfp10105.project_forfun.MainActivity;

public class NotificaitonFCMService extends FirebaseMessagingService{
    //在前景執行時會呼叫(背景時不會)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        RemoteMessage.Notification notification = remoteMessage.getNotification();
//        String title = "";
//        String body = "";
//        if (notification != null) {
//            title = notification.getTitle();
//            body =  notification.getBody();
//        }
        //更改通知圖案
        Message msg = new Message();
        //自定義消息代碼
        msg.what=1;
        //要傳送的物件
//        msg.obj=1;
        //主執行緒才能控制元件
        MainActivity.handler.sendMessage(msg);

    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
//        Log.d("顯示裝置Token",token);

    }
}