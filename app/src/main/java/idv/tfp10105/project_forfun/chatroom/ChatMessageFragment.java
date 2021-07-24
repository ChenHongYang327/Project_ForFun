package idv.tfp10105.project_forfun.chatroom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import idv.tfp10105.project_forfun.MainActivity;
import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.ChatRoom;
import idv.tfp10105.project_forfun.common.bean.ChatRoomMessage;
import idv.tfp10105.project_forfun.common.bean.Member;

public class ChatMessageFragment extends Fragment {
    private static final String TAG = "chatMessageFragment";
    private  Activity activity;
    private CircularImageView memberImg;
    private TextView memberName;
    private EditText edMessage;
    private ImageButton btSend;
    private  RecyclerView rvChatMessage;
    private List<ChatRoomMessage> chatRoomMessages;
    private  ChatRoom chatRoom;
    private SharedPreferences sharedPreferences;
    private  Integer memberId;
    private String memberToken;
    private List<Member> members;
    private FirebaseStorage storage;
    private ChatRoomMessageAdapter chatRoomMessageAdapter;
    public static Handler handler;
    private String url = Common.URL + "MessageController";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        //取bundle資料
        chatRoom = (ChatRoom) (getArguments() != null ? getArguments().getSerializable("chatRooms") : null);

        //取偏好設定檔
        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        memberId = sharedPreferences.getInt("memberId", -1);
        ChatMessageFragment.handler = new Handler(Looper.myLooper(), msg -> {
            Log.d(TAG,"handler");
            if (msg.what == 2) {
                if (chatRoomMessageAdapter == null) {
                    chatRoomMessageAdapter = new ChatRoomMessageAdapter(activity, getChatRoomMessage());
                    return true;
                }
                getChatRoomMessage();
                chatRoomMessageAdapter.notifyDataSetChanged();
                return true;
            }
            return true;
        });



        // 每次取得registration token就傳送至server儲存，
        // 因為當MyFCMService.onNewToken()傳送token至server時可能失敗，而導致server沒有token
//        getTokenSendServer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleRecycleView();
        chatRoomMessages = getChatRoomMessage();
//        getMembersToken();
        showChatRoomMessage(chatRoomMessages);
        handlebtSend();
    }

    private void findViews(View view) {
        memberImg = view.findViewById(R.id.chatRoomMemberImg);
        memberName = view.findViewById(R.id.chat_memberName);
        edMessage = view.findViewById(R.id.edit_message);
        btSend = view.findViewById(R.id.bt_message_send);
        rvChatMessage = view.findViewById(R.id.rv_chatMessage);
    }

    private void handleRecycleView() {
        rvChatMessage.setLayoutManager(new LinearLayoutManager(activity));
    }

    private  List<ChatRoomMessage> getChatRoomMessage() {
        List<ChatRoomMessage> chatRoomMessages = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
//            jsonObject.addProperty("MEMBER_ID", memberId);
            jsonObject.addProperty("chatRoomId", chatRoom.getChatroomId());
//            jsonObject.addProperty("MEMBER_ID", memberId);

            JsonObject jsonIn = new Gson().fromJson(RemoteAccess.getJsonData(url, jsonObject.toString()), JsonObject.class);
            Type listType = new TypeToken<List<ChatRoomMessage>>() {}.getType();

            //解析後端傳回資料
            chatRoomMessages = new Gson().fromJson(jsonIn.get("messageList").getAsString(), listType);

        }else {
            Toast.makeText(activity, "no network connection available", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "chatRoomMessages : " + chatRoomMessages, Toast.LENGTH_SHORT).show();
        return chatRoomMessages;

    }



    private void handlebtSend() {
        btSend.setOnClickListener(v -> {
            String chatMSG = edMessage.getText().toString().trim();
            if (chatMSG.length() <= 0) {
                Toast.makeText(activity, "Message is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                if (memberId.equals(chatRoom.getMemberId1())) {
                    ChatRoomMessage chatRoomMessage = new ChatRoomMessage(0, chatRoom.getChatroomId(), memberId, chatMSG);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "messageInsert");
                    jsonObject.addProperty("chatMessage", chatMSG);
                    jsonObject.addProperty("receivedMemberId", chatRoom.getMemberId2());
                    jsonObject.addProperty("MemberId", memberId);
                    jsonObject.addProperty("chatRoomMessage", new Gson().toJson(chatRoomMessage));

                    int count;
                    //執行緒池物件
                    String result = RemoteAccess.getJsonData(url, jsonObject.toString());
                    //新增筆數
                    count = Integer.parseInt(result);
                    //筆數為0
                    if (count == 0) {
                        Toast.makeText(activity, "新增失敗", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "新增成功", Toast.LENGTH_SHORT).show();

                        ChatRoomMessageAdapter chatRoomMessageAdapter = (ChatRoomMessageAdapter) rvChatMessage.getAdapter();

                        chatRoomMessageAdapter.notifyDataSetChanged();
                    }

                    } else if (memberId.equals(chatRoom.getMemberId2())) {
                    ChatRoomMessage chatRoomMessage = new ChatRoomMessage(0, chatRoom.getChatroomId(), memberId, chatMSG);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "messageInsert");
                    jsonObject.addProperty("chatMessage", chatMSG);
                    jsonObject.addProperty("receivedMemberId", chatRoom.getMemberId1());
                    jsonObject.addProperty("MemberId", memberId);
                    jsonObject.addProperty("chatRoomMessage", new Gson().toJson(chatRoomMessage));

                    int count;
                    //執行緒池物件
                    String result = RemoteAccess.getJsonData(url, jsonObject.toString());
                    //新增筆數
                    count = Integer.parseInt(result);
                    //筆數為0
                    if (count == 0) {
                        Toast.makeText(activity, "新增失敗", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "新增成功", Toast.LENGTH_SHORT).show();

                        ChatRoomMessageAdapter chatRoomMessageAdapter = (ChatRoomMessageAdapter) rvChatMessage.getAdapter();

                        chatRoomMessageAdapter.notifyDataSetChanged();
                    }
                }

            }

        });
    }


    // 下載Firebase storage的照片並顯示在ImageView上
    private void downloadImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024;
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ?
                                "Image download Failed" + ": " + path : task.getException().getMessage() + ": " + path;
                        imageView.setImageResource(R.drawable.no_image);
                        Log.e(TAG, message);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }



//    // 取得registration token後傳送至server
//    private void getTokenSendServer() {
//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                if (task.getResult() != null) {
//                    String token = task.getResult();
//                    RemoteAccess.sendTokenToServer(token, activity);
//                }
//            }
//        });
//    }


    private  void showChatRoomMessage(List<ChatRoomMessage> chatRoomMessages) {
        if (chatRoomMessages == null || chatRoomMessages.isEmpty()) {
            Toast.makeText(activity, "沒有訊息", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        ChatRoomMessageAdapter chatRoomMessageAdapter = (ChatRoomMessageAdapter) rvChatMessage.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (chatRoomMessageAdapter == null) {
            chatRoomMessageAdapter = new ChatRoomMessageAdapter(activity, chatRoomMessages);
            rvChatMessage.setAdapter(chatRoomMessageAdapter);
        } else {
            //更新Adapter資料,重刷
            chatRoomMessageAdapter.setAdapter(chatRoomMessages);
            //重新執行RecyclerView 三方法
            chatRoomMessageAdapter.notifyDataSetChanged();
        }
    }

    public class ChatRoomMessageAdapter extends RecyclerView.Adapter<ChatRoomMessageAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<ChatRoomMessage> chatRoomMessages;


        public ChatRoomMessageAdapter(Context context, List<ChatRoomMessage> chatRoomMessages) {
            layoutInflater = LayoutInflater.from(context);
            this.chatRoomMessages = chatRoomMessages;
        }

        public void setAdapter(List<ChatRoomMessage> chatRoomMessages) {
            this.chatRoomMessages = chatRoomMessages;
        }

        @Override
        public int getItemCount() {
            return chatRoomMessages == null ? 0 : chatRoomMessages.size();
        }

        @NonNull
        @Override
        public ChatRoomMessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.chat_msg_content_itemview, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatRoomMessageAdapter.MyViewHolder holder, int position) {
            ChatRoomMessage chatRoomMessage = chatRoomMessages.get(position);
//            Member member2 = members.get(position);
//            memberToken = member.getToken();
            if (!memberId.equals(chatRoom.getMemberId1())) {
                holder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
//            holder.chatRoom_message_ReadStatus_self.setText(chatRoomMessage.getRead().toString());
                holder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
//                holder.otherMessage.setVisibility(View.GONE);

            } else {
//                downloadImage(holder.chatRoomMemberImg, member2.getHeadshot());
                holder.chatRoom_message_context.setText(chatRoomMessage.getMsgChat());
                holder.chatRoom_message_CreatTime.setText(chatRoomMessage.getCreateTime().toString());
//                holder.selfMessage.setVisibility(View.GONE);

            }

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            //            public CircularImageView chatRoomMemberImg;
            public ImageView chatRoomMemberImg;
            public TextView chatRoom_message_context, chatRoom_message_CreatTime, chatRoom_message_context_self, chatRoom_message_CreatTime_self, chatRoom_message_ReadStatus_self;
            public LinearLayout otherMessage;
            public LinearLayout selfMessage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                chatRoom_message_context_self = itemView.findViewById(R.id.chatRoom_message_context_self);
                chatRoom_message_CreatTime_self = itemView.findViewById(R.id.chatRoom_message_CreatTime_self);
                chatRoom_message_ReadStatus_self = itemView.findViewById(R.id.chatRoom_message_ReadStatus_self);
                chatRoomMemberImg = itemView.findViewById(R.id.chatRoomMemberImg);
                chatRoom_message_context = itemView.findViewById(R.id.chatRoom_message_context);
                chatRoom_message_CreatTime = itemView.findViewById(R.id.chatRoom_message_CreatTime);
                selfMessage = itemView.findViewById(R.id.self_message);
                otherMessage = itemView.findViewById(R.id.other_message);
            }
        }
    }

//    public class FCMService extends FirebaseMessagingService {
//        private static final String TAG = "TAG_FCMService";
//
//        @Override
//        // 當Android裝置在前景收到FCM時呼叫
//        public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//            // 取得notification資料，主要為title與body這2個保留字
//            RemoteMessage.Notification notification = remoteMessage.getNotification();
//            String title = "";
//            String body = "";
//            if (notification != null) {
//                title = notification.getTitle();
//                body = notification.getBody();
//                Message message = new Message();
//                // 取得自訂資料
////            Map<String, String> map = remoteMessage.getData();
////            String data = map.get("data");
////            Log.d(TAG, "onMessageReceived():\ntitle: " + title + ", body: " + body + ", data: " + data);
//
//                //主執行緒才能控制元件
//                MainActivity.handler.sendMessage(message);
////                onNewToken(memberToken);
//            }
//        }
//
//        @Override
//        // 當registration token更新時呼叫，應該將新的token傳送至server
//        public void onNewToken(@NonNull String token) {
//            RemoteAccess.sendTokenToServer(token, activity);
//        }
//    }
}