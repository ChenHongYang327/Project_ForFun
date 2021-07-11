package idv.tfp10105.project_forfun.chatroom;

import android.app.Activity;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.ChatRoom;
import idv.tfp10105.project_forfun.common.bean.ChatRoomMessage;
import idv.tfp10105.project_forfun.common.bean.Comment;
import idv.tfp10105.project_forfun.discussionboard.controller.discussionDetailFragment;

public class chatMessageFragment extends Fragment {
    private CircularImageView memberImg;
    private TextView memberName;
    private EditText edMessage;
    private ImageButton btSend;
    private Activity activity;
    private RecyclerView rvChatMessage;
    private List<ChatRoomMessage> chatRoomMessages;
    private ChatRoom chatRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        //取bundle資料
        chatRoom = (ChatRoom) (getArguments() != null ? getArguments().getSerializable("chatRooms") : null);
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
        handleBtSend();
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
            jsonObject.addProperty("action","getAll");
            jsonObject.addProperty("MEMBER_ID", chatRoom.getMemberId1());
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<ChatRoomMessage>>() {}.getType();

            //解析後端傳回資料
            chatRoomMessages = new Gson().fromJson(jsonIn, listType);
        }else {
            Toast.makeText(activity, "no network connection available", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "chatRoomMessages : " + chatRoomMessages, Toast.LENGTH_SHORT).show();
        return chatRoomMessages;

    }

    private void handleBtSend() {
        btSend.setOnClickListener(v -> {
            String chatMSG = edMessage.getText().toString().trim();
            if (chatMSG.length() <= 0) {
                Toast.makeText(activity, "Message is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                String url = Common.URL + "MessageController";
                ChatRoomMessage chatRoomMessage = new ChatRoomMessage(0, 1, 1, chatMSG);
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

                    if (!chatRoomMessage.getMemberId().equals(chatRoom.getMemberId1())) {
                        ChatRoomMessageAdapter.MyViewHolder.otherMessage.setVisibility(View.VISIBLE);
                        ChatRoomMessageAdapter.MyViewHolder.selfMessage.setVisibility(View.GONE);
                        ChatRoomMessageAdapter.MyViewHolder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
                        ChatRoomMessageAdapter.MyViewHolder.chatRoom_message_context.setText(chatRoomMessage.getMsgChat());
                        ChatRoomMessageAdapter.MyViewHolder.chatRoom_message_CreatTime.setText(chatRoomMessage.getCreateTime().toString());

                    } else {
                        ChatRoomMessageAdapter.MyViewHolder.selfMessage.setVisibility(View.VISIBLE);
                        ChatRoomMessageAdapter.MyViewHolder.otherMessage.setVisibility(View.GONE);
                        ChatRoomMessageAdapter.MyViewHolder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
                        ChatRoomMessageAdapter.MyViewHolder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
                    }

                    Toast.makeText(activity, "新增成功", Toast.LENGTH_SHORT).show();

                    chatRoomFragment.ChatRoomAdapter chatRoomAdapter = (chatRoomFragment.ChatRoomAdapter) rvChatMessage.getAdapter();

                    chatRoomAdapter.notifyDataSetChanged();
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



    public static class ChatRoomMessageAdapter extends RecyclerView.Adapter<ChatRoomMessageAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<ChatRoomMessage> chatRoomMessages;

        public ChatRoomMessageAdapter(Context context ,List<ChatRoomMessage> chatRoomMessages) {
            layoutInflater = LayoutInflater.from(context);
            this.chatRoomMessages = chatRoomMessages;
        }

        public void setAdapter(List<ChatRoomMessage> chatRoomMessages) {
            this.chatRoomMessages = chatRoomMessages;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            public static CircularImageView chatRoomMemberImg;
            public static TextView chatRoom_message_context, chatRoom_message_CreatTime, chatRoom_message_context_self, chatRoom_message_CreatTime_self, chatRoom_message_ReadStatus_self;
            public static LinearLayout otherMessage;
            public static LinearLayout selfMessage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
               chatRoomMemberImg = itemView.findViewById(R.id.chatRoomMemberImg);
               chatRoom_message_context = itemView.findViewById(R.id.chatRoom_message_context);
               chatRoom_message_CreatTime = itemView.findViewById(R.id.chatRoom_message_CreatTime);
               chatRoom_message_context_self = itemView.findViewById(R.id.chatRoom_message_context_self);
               chatRoom_message_CreatTime_self = itemView.findViewById(R.id.chatRoom_message_CreatTime_self);
               chatRoom_message_ReadStatus_self = itemView.findViewById(R.id.chatRoom_message_ReadStatus_self);
               otherMessage = itemView.findViewById(R.id.other_message);
               selfMessage = itemView.findViewById(R.id.self_message);

            }
        }

        @Override
        public int getItemCount() {
            return chatRoomMessages == null ? 0 : chatRoomMessages.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.chat_member_itemview, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ChatRoomMessage chatRoomMessage = chatRoomMessages.get(position);
            holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
            holder.chatRoom_message_context.setText(chatRoomMessage.getMsgChat());
            holder.chatRoom_message_CreatTime.setText(chatRoomMessage.getCreateTime().toString());
            holder.chatRoom_message_context_self.setText(chatRoomMessage.getMsgChat());
            holder.chatRoom_message_CreatTime_self.setText(chatRoomMessage.getCreateTime().toString());
//            holder.chatRoom_message_ReadStatus_self.set(false);


        }
    }
}