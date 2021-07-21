package idv.tfp10105.project_forfun.chatroom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.ChatRoom;
import idv.tfp10105.project_forfun.common.bean.ChatRoomMessage;

public class chatMessageFragment extends Fragment {
    private CircularImageView memberImg;
    private TextView memberName;
    private EditText edMessage;
    private ImageButton btSend;
    private Activity activity;
    private RecyclerView rvChatMessage;
    private List<ChatRoomMessage> chatRoomMessages;
    private ChatRoom chatRoom;
    private SharedPreferences sharedPreferences;
    private Integer memberId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        //取bundle資料
        chatRoom = (ChatRoom) (getArguments() != null ? getArguments().getSerializable("chatRooms") : null);
        //取偏好設定檔
        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        memberId = sharedPreferences.getInt("memberId", -1);
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

    private List<ChatRoomMessage> getChatRoomMessage() {
        List<ChatRoomMessage> chatRoomMessages = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "MessageController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            jsonObject.addProperty("MEMBER_ID", memberId);
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<ChatRoomMessage>>() {
            }.getType();

            //解析後端傳回資料
            chatRoomMessages = new Gson().fromJson(jsonIn, listType);
        } else {
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
                String url = Common.URL + "MessageController";
                ChatRoomMessage chatRoomMessage = new ChatRoomMessage(0, chatRoom.getChatroomId(), memberId, chatMSG);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "messageInsert");
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

        });
    }


    private void showChatRoomMessage(List<ChatRoomMessage> chatRoomMessages) {
        if (chatRoomMessages == null || chatRoomMessages.isEmpty()) {
            Toast.makeText(activity, "沒有訊息", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        ChatRoomMessageAdapter chatRoomMessageAdapter = (ChatRoomMessageAdapter) rvChatMessage.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (chatRoomMessageAdapter == null) {
            rvChatMessage.setAdapter(new ChatRoomMessageAdapter(activity, chatRoomMessages));
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
        private final int MESSAGE_IN_VIEW_TYPE = 1;
        private final int MESSAGE_OUT_VIEW_TYPE = 2;

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
            if (memberId.equals(chatRoom.getMemberId1())) {
                holder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
//            holder.chatRoom_message_ReadStatus_self.setText(chatRoomMessage.getRead().toString());
                holder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
//                holder.otherMessage.setVisibility(View.GONE);

            } else {
                holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
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

//    public class ChatRoomMessageAdapter extends RecyclerView.Adapter<ChatRoomMessageAdapter.MyViewHolder> {
//        private final LayoutInflater layoutInflater;
//        private List<ChatRoomMessage> chatRoomMessages;
//        private final int MESSAGE_IN_VIEW_TYPE = 1;
//        private final int MESSAGE_OUT_VIEW_TYPE = 2;
//
//
//        public ChatRoomMessageAdapter(Context context, List<ChatRoomMessage> chatRoomMessages) {
//            layoutInflater = LayoutInflater.from(context);
//            this.chatRoomMessages = chatRoomMessages;
//        }
//
//        public void setAdapter(List<ChatRoomMessage> chatRoomMessages) {
//            this.chatRoomMessages = chatRoomMessages;
//        }
//
//        public class MyViewHolder extends RecyclerView.ViewHolder {
//            public CircularImageView chatRoomMemberImg;
//            public TextView chatRoom_message_context, chatRoom_message_CreatTime, chatRoom_message_context_self, chatRoom_message_CreatTime_self, chatRoom_message_ReadStatus_self;
//            public LinearLayout otherMessage;
//            public LinearLayout selfMessage;
//
//            public MyViewHolder(@NonNull View itemView) {
//                super(itemView);
//                chatRoom_message_context_self = itemView.findViewById(R.id.chatRoom_message_context_self);
//                chatRoom_message_CreatTime_self = itemView.findViewById(R.id.chatRoom_message_CreatTime_self);
//                chatRoom_message_ReadStatus_self = itemView.findViewById(R.id.chatRoom_message_ReadStatus_self);
//                chatRoomMemberImg = itemView.findViewById(R.id.chatRoomMemberImg);
//                chatRoom_message_context = itemView.findViewById(R.id.chatRoom_message_context);
//                chatRoom_message_CreatTime = itemView.findViewById(R.id.chatRoom_message_CreatTime);
//                selfMessage = itemView.findViewById(R.id.self_message);
//                otherMessage = itemView.findViewById(R.id.other_message);
//
//            }
//        }
//
////        @Override
////        public int getItemViewType(int position) {
////            ChatRoomMessage chatRoomMessage = chatRoomMessages.get(position);
////            try {
////                if(chatRoomMessage.getBoolean("isSent")){
////                    if(chatRoomMessage.has("message")){
////                        return TYPE_MESSAGE_SENT;
////                    }
////                }else{
////                    if(chatRoomMessage.has("message")){
////                        return TYPE_MESSAGE_RECEIVED;
////                    }
////                }
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////            return -1;
////        }
//
//        @Override
//        public int getItemCount() {
//            return chatRoomMessages == null ? 0 : chatRoomMessages.size();
//        }
//
//        @NonNull
//        @Override
//        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View itemView = layoutInflater.inflate(R.layout.chat_msg_content, parent, false);
//            return new MyViewHolder(itemView);
//        }
////            View view = null;
////            if(viewType == MESSAGE_IN_VIEW_TYPE){
////                view = LayoutInflater.from(parent.getContext())
////                        .inflate(R.layout.received_chat_message_itemview, parent, false);
////            }
////            else{
////                view = LayoutInflater.from(parent.getContext())
////                        .inflate(R.layout.send_chat_message_itemview, parent, false);
////            }
//////            return new MessageHolder(view);
////            return null;
////        }
//
//        @Override
//        public void onBindViewHolder(@NonNull chatMessageFragment.ChatRoomMessageAdapter.MyViewHolder holder, int position) {
//            ChatRoomMessage chatRoomMessage = chatRoomMessages.get(position);
//            if (memberId.equals(chatRoomMessage.getMemberId())) {
//                holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
//                holder.chatRoom_message_context.setText(chatRoomMessage.getMsgChat());
//                holder.chatRoom_message_CreatTime.setText(chatRoomMessage.getCreateTime().toString());
////                holder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
////                holder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
//                holder.chatRoomMemberImg.setVisibility(View.VISIBLE);
//                holder.chatRoom_message_context.setVisibility(View.VISIBLE);
//                holder.chatRoom_message_CreatTime.setVisibility(View.VISIBLE);
//                holder.chatRoom_message_context_self.setVisibility(View.GONE);
//                holder.chatRoom_message_ReadStatus_self.setVisibility(View.GONE);
//            } else if (memberId.equals(chatRoom.getMemberId2())) {
////                holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
////                holder.chatRoom_message_context.setText(chatRoomMessage.getMsgChat());
////                holder.chatRoom_message_CreatTime.setText(chatRoomMessage.getCreateTime().toString());
//                holder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
//                holder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
//                holder.chatRoomMemberImg.setVisibility(View.GONE);
//                holder.chatRoom_message_context.setVisibility(View.GONE);
//                holder.chatRoom_message_CreatTime.setVisibility(View.GONE);
//                holder.chatRoom_message_context_self.setVisibility(View.VISIBLE);
//                holder.chatRoom_message_ReadStatus_self.setVisibility(View.VISIBLE);
//            }
//
//
//        }
//
//    }
}