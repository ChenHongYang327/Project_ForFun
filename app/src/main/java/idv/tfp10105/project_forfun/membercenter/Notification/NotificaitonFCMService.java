package idv.tfp10105.project_forfun.membercenter.Notification;

import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import idv.tfp10105.project_forfun.MainActivity;

public class NotificaitonFCMService extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = "";
        int body = 0;
        if (notification != null) {
            title = notification.getTitle();
            body =  Integer.parseInt(notification.getBody());
        }
        Message msg = new Message();
        //自定義消息代碼
        msg.what=1;
        //要傳送的物件
        msg.obj=body;
        MainActivity.handler.sendMessage(msg);

    }
}
