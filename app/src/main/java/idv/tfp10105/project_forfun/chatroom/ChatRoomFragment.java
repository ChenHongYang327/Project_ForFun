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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "ChatRoomFragment";
    private static Activity activity;
    public static RecyclerView rv_chat;
    public static List<ChatRoom> chatRooms;
    public static ChatRoom chatRoom;
    private SharedPreferences sharedPreferences, sharedPreferf;
    private int selectUserId;
    private String selectUserHeadShot, selectUserName, selectmember;
    private FirebaseStorage storage;
    private int memberId;
    private Member member;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        sharedPreferf = activity.getSharedPreferences("selectUser", Context.MODE_PRIVATE);
        selectUserId = sharedPreferf.getInt("selectUserId", -1);
        selectUserName = sharedPreferf.getString("selectUserName", "");
        selectUserHeadShot = sharedPreferf.getString("selectUserHeadShot", "");
        selectmember = sharedPreferf.getString("selectmember", "");
        member = new Gson().fromJson(selectmember, Member.class);





        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        memberId = sharedPreferences.getInt("memberId", -1);

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



    public List<ChatRoom> getChatRooms() {
        List<ChatRoom> chatRooms = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "ChatRoomController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","getAll");
            jsonObject.addProperty("receivedMemberId", memberId);
            jsonObject.addProperty("sendMemberId", selectUserId);
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<ChatRoom>>() {}.getType();

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
            chatRoom = chatRooms.get(position);
//            holder.chatRoomMemberImg.setImageResource(R.drawable.post_memberhead);
//            downloadImage(holder.chatRoomMemberImg,selectUserHeadShot);
            holder.chatRoomCreatTime.setText(chatRoom.getCreateTime().toString());
            holder.chatRoom_memberName.setText(selectUserName);
            holder.chatRoomReadStatus.setText("未讀");

            holder.itemView.setOnClickListener(v -> {
//                if (RemoteAccess.networkCheck(activity)) {
//                    String url = Common.URL + "MessageController";
//                    JsonObject jsonObject = new JsonObject();
//                    jsonObject.addProperty("action","updateRead");
//                    jsonObject.addProperty("chatRoomId", chatRoom.getChatroomId());
//                    int messageReadType;
//                    String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
//                    //解析後端傳回資料
//                    messageReadType = Integer.parseInt(jsonIn);
//                    if (messageReadType >= 0) {
//                        holder.chatRoomReadStatus.setText("已讀");
//                    } else {
//                        Toast.makeText(activity, "沒有留言", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(activity, "chatRooms : " + chatRooms, Toast.LENGTH_SHORT).show();
//
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("selectUser", chatRoom);
//                bundle.getInt("chatroomId",chatRoom.getChatroomId());
//                Navigation.findNavController(v).navigate(R.id.action_chatRoomFragment_to_chatMessageFragment, bundle);

                if (RemoteAccess.networkCheck(activity)) {
                    String url = Common.URL + "ChatRoomController";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "selectChatRoomId");
                    jsonObject.addProperty("receivedMemberId", selectUserId);
                    jsonObject.addProperty("sendMemberId", memberId);
                    int chatroomId;
                    String result = RemoteAccess.getJsonData(url,jsonObject.toString());
                    chatroomId = Integer.parseInt(result);
                    if (chatroomId == 0) {
                        Toast.makeText(activity, "已有聊天室", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(activity, "聊天室新建成功", Toast.LENGTH_SHORT).show();

                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectUser", member);
                    bundle.putInt("chatroomId", chatroomId);
                    Navigation.findNavController(v).navigate(R.id.chatMessageFragment,bundle);
                    sharedPreferf.edit().remove("selectUser");
                }


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
    }
}