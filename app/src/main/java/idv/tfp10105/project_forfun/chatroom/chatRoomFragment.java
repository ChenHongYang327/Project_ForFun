package idv.tfp10105.project_forfun.chatroom;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;

public class chatRoomFragment extends Fragment {
    private Activity activity;
    private RecyclerView rv_chat;
    private List<ChatRoom> chatRooms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv_chat =  view.findViewById(R.id.rv_chat);
        handleRecyclerView();
        chatRooms = getChatRooms();
        showChatRooms(chatRooms);
    }




    private void handleRecyclerView() {
        rv_chat.setLayoutManager(new LinearLayoutManager(activity));
        rv_chat.setAdapter(new ChatRoomAdapter(activity,chatRooms));
        rv_chat.addItemDecoration(new ItemDecoration(20, activity));
    }

    // 抓資料
    private void showChatRooms(List<ChatRoom> chatRooms) {
        if (chatRooms == null || chatRooms.isEmpty()) {
            Toast.makeText(activity, "沒有聊天記錄", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        ChatRoomAdapter chatRoomAdapter = (ChatRoomAdapter) rv_chat.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (chatRoomAdapter == null) {
            rv_chat.setAdapter(new ChatRoomAdapter(activity,chatRooms));
        } else {
            //更新Adapter資料,重刷
            chatRoomAdapter.setAdapter(chatRooms);

            //重新執行RecyclerView 三方法
            chatRoomAdapter.notifyDataSetChanged();
        }
    }



    private List<ChatRoom> getChatRooms() {
        List<ChatRoom> chatRooms = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "ChatRoomController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","getAll");
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<ChatRoom>>() {
            }.getType();

            //解析後端傳回資料
            chatRooms = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "chatRooms : " + chatRooms, Toast.LENGTH_SHORT).show();
        return chatRooms;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<ChatRoom> chatRooms;

        public ChatRoomAdapter(Context context, List<ChatRoom> chatRooms) {
            layoutInflater = LayoutInflater.from(context);
            this.chatRooms = chatRooms;
        }

        //重刷RecyclerView畫面
        public void setAdapter(List<ChatRoom> chatRooms) {
            this.chatRooms = chatRooms;
        }


        @Override
        public int getItemCount() {
            return chatRooms == null ? 0 : chatRooms.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemview = layoutInflater.inflate(R.layout.chat_room_itemview, parent, false);
            return new MyViewHolder(itemview);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ChatRoom chatRoom = chatRooms.get(position);
            holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
            holder.chatRoomCreatTime.setText(chatRoom.getCreateTime().toString());
            holder.chatRoom_memberName.setId(chatRoom.getMemberId1());
            holder.chatRoomReadStatus.setText("未讀");

            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("chatRooms", chatRoom);
                Navigation.findNavController(v).navigate(R.id.action_chatRoomFragment_to_chatMessageFragment, bundle);
            });


        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            CircularImageView chatRoomMemberImg;
            TextView chatRoom_memberName, chatRoomCreatTime, chatRoomReadStatus;

                public MyViewHolder(@NonNull View itemView) {
                    super(itemView);
                    chatRoomMemberImg = itemView.findViewById(R.id.chatRoomMemberImg);
                    chatRoom_memberName = itemView.findViewById(R.id.chatRoom_memberName);
                    chatRoomCreatTime = itemView.findViewById(R.id.chatRoomCreatTime);
                    chatRoomReadStatus = itemView.findViewById(R.id.chatRoomReadStatus);


                }
            }
    }
}