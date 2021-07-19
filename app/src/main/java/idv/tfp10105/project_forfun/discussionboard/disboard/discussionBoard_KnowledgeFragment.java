package idv.tfp10105.project_forfun.discussionboard.disboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.Post;
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;

public class discussionBoard_KnowledgeFragment extends Fragment {
    private final static String TAG = "TAG_KnowledgeFragment";
    private RecyclerView rv_know;
    private Activity activity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseStorage storage;
    //外部列表 第一頁 列表
    private List<Post> posts;
    private SearchView searchView;
    private FloatingActionButton bt_Add;
    private SharedPreferences sharedPreferences;
    private String name, headshot;
    private String signin_name, signin_headshot;
    private int memberId;
    private List<Member> members;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences( "SharedPreferences", Context.MODE_PRIVATE);
        signin_name = sharedPreferences.getString("name","");
        signin_headshot = sharedPreferences.getString("headshot", "");
        memberId = sharedPreferences.getInt("memberId" , -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_board_knowledge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleRecyclerView();
        handleBtAdd();
        handleSearchView();
        handleSwipeRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        posts = getPosts();
        showPosts(posts, members);

    }



    private void findViews(View view) {
        searchView = view.findViewById(R.id.searchView_know);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_know);
        rv_know = view.findViewById(R.id.rv_know);
        bt_Add = view.findViewById(R.id.dis_bt_Add_know);
    }

    private void handleSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //開始動畫
            swipeRefreshLayout.setRefreshing(true);
            //重新載入recycleView
            showPosts(posts, members);
            //結束動畫
            swipeRefreshLayout.setRefreshing(false);

        });
    }

    private void handleSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                //如果輸入條件為空字串,就顯示原始資料,否則就顯示收尋後結果
                if (newText.isEmpty()) {
                    showPosts(posts, members);
                } else {
                    List<Post> searchPosts = new ArrayList<>();
                    //搜尋原始資料內有無包含關鍵字（不區分大小寫）
                    for (Post post : posts) {
                        if (post.getPostTitle().toUpperCase().contains(newText.toUpperCase())) {
                            searchPosts.add(post);
                        }
                    }
                    showPosts(searchPosts, members);
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }


    private void handleBtAdd() {
        //跳轉至新增頁面
        bt_Add.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_discussionBoardFragment_to_discussionInsertFragment));
    }

    private void handleRecyclerView() {

        rv_know.setLayoutManager(new LinearLayoutManager(activity));
        rv_know.addItemDecoration(new ItemDecoration(30, activity));//30代表30dp
    }



    private List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "DiscussionBoardController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("boardId","知識問答");
            jsonObject.addProperty("action", "getAll");

            JsonObject jsonIn = new Gson().fromJson(RemoteAccess.getJsonData(url, jsonObject.toString()),JsonObject.class);
            Type listPost = new TypeToken<List<Post>>() {}.getType();

            //解析後端傳回資料
            posts = new Gson().fromJson(jsonIn.get("postList").getAsString(),listPost);

        } else {
            Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "posts : " + posts, Toast.LENGTH_SHORT).show();
        return posts;
    }

    // 抓po文者資料
    private List<Member> getMembers() {

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "DiscussionBoardController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("boardId","需求單");
            jsonObject.addProperty("action", "getAll");
            JsonObject jsonIn = new Gson().fromJson(RemoteAccess.getJsonData(url, jsonObject.toString()),JsonObject.class);
            Type listMember = new TypeToken<List<Member>>() {}.getType();

            //解析後端傳回資料
            members = new Gson().fromJson(jsonIn.get("memberList").getAsString(),listMember);

        } else {
            Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "members : " + members, Toast.LENGTH_SHORT).show();

        return members;
    }

    private void showPosts(List<Post> posts, List<Member> members) {
        if (posts == null || posts.isEmpty()) {
            Toast.makeText(activity, "沒有貼文", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        KnowAdapter knowAdapter = (KnowAdapter) rv_know.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (knowAdapter == null) {
            rv_know.setAdapter(new KnowAdapter(activity, posts, getMembers()));
        } else {
            //更新Adapter資料,重刷
            knowAdapter.setAdapter(posts, members);

            //重新執行RecyclerView 三方法
            knowAdapter.notifyDataSetChanged();
        }
    }


    public class KnowAdapter extends RecyclerView.Adapter<KnowAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private final int imageSize;
        //內部列表（搜尋後）
        private List<Post> posts;
        private List<Member> members;



        public KnowAdapter(Context context, List<Post> posts, List<Member> members) {
            layoutInflater = LayoutInflater.from(context);
            this.posts = posts;
            this.members = members;
            //螢幕寬度除以四當圖片尺寸
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        //        重刷RecyclerView畫面
        public void setAdapter(List<Post> posts, List<Member> members) {
            this.posts = posts;
            this.members = members;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView disPostName, disPostTitle, disPostContext, disPostTime;
            ImageButton disPostBtMore;
            ImageView disPostImg;
            CircularImageView disPostMemberImg;

            MyViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                disPostTitle = itemView.findViewById(R.id.post_item_title_text);
                disPostName = itemView.findViewById(R.id.post_item_memberName_text);
                disPostContext = itemView.findViewById(R.id.post_item_context_text);
                disPostImg = itemView.findViewById(R.id.post_item_house_img);
                disPostBtMore = itemView.findViewById(R.id.post_item_bt_more);
                disPostMemberImg = itemView.findViewById(R.id.post_item_bt_memberhead);
                disPostTime = itemView.findViewById(R.id.post_item_time_text);
            }
        }

        @Override
        public int getItemCount() {
            return posts == null ? 0 : posts.size();
        }




        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.discussion_itemview, null, false);
            return new MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull @NotNull discussionBoard_KnowledgeFragment.KnowAdapter.MyViewHolder holder, int position) {

            final Post post = posts.get(position);
            Member member2 = members.get(position);

            holder.disPostTitle.setText(post.getPostTitle());
            //TODO
            holder.disPostName.setText(member2.getNameL() + member2.getNameF());
            holder.disPostContext.setText(post.getPostContext());
            holder.disPostTime.setText(post.getCreateTime().toString());
            showImage(holder.disPostMemberImg, member2.getHeadshot());

            String url = Common.URL + "DiscussionBoardController";
            int postId = post.getPostId();
            String imagePath = post.getPostImg();
            JsonObject jsonObject =  new JsonObject();
            jsonObject.addProperty("action","getImage");
            jsonObject.addProperty("imagePath",imagePath);
            jsonObject.addProperty("postId", postId);
            jsonObject.addProperty("imageSize", imageSize);
            String jsonImg = RemoteAccess.getJsonData(url, jsonObject.toString());

            //設定點擊事件
            holder.disPostImg.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("name", member2.getNameL() + member2.getNameF());
                bundle.putString("headshot", member2.getHeadshot());
                bundle.putSerializable("post", post);
                //轉至詳細頁面
                Navigation.findNavController(v).navigate(R.id.action_discussionBoardFragment_to_discussionDetailFragment, bundle);
            });
            if (jsonImg != null) {
                showImage(holder.disPostImg, imagePath);
            } else {
                holder.disPostImg.setImageResource(R.drawable.no_image);
            }
            holder.disPostBtMore.setOnClickListener(v -> {

                //選單
                PopupMenu popupMenu = new PopupMenu(activity,v, Gravity.END);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    //新增
                    if (itemId == R.id.update){
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("post", post);
                        bundle.putString("name", member2.getNameL() + member2.getNameF());
                        bundle.putString("headshot", member2.getHeadshot());
                        Navigation.findNavController(v).navigate(R.id.action_discussionBoardFragment_to_discussionUpdateFragment,bundle);
                        //刪除
                    } else if (itemId == R.id.delete) {
                        if (RemoteAccess.networkCheck(activity)){
                            JsonObject jsonDelete = new JsonObject();
                            jsonDelete.addProperty("action","postDelete");
                            jsonDelete.addProperty("postId",post.getPostId());
                            int count;
                            String result = RemoteAccess.getJsonData(url,jsonDelete.toString());
                            count = Integer.parseInt(result);
                            if (count == 0) {
                                Toast.makeText(activity, "刪除失敗", Toast.LENGTH_SHORT).show();
                            } else {
                                posts.remove(post);
                                KnowAdapter.this.notifyDataSetChanged();
                                // 外面spots也必須移除選取的spot
                                discussionBoard_KnowledgeFragment.this.posts.remove(post);
                                storage.getReference().child(post.getPostImg()).delete()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "照片已刪除");
                                            } else {
                                                String message = task.getException() == null ? "照片刪除失敗" + ": " + post.getPostImg() :
                                                        task.getException().getMessage() + ": " + post.getPostImg();
                                                Log.e(TAG, message);
                                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                Toast.makeText(activity, "刪除成功", Toast.LENGTH_SHORT).show();
                            }
                            //檢舉
                        }
                    }else if (itemId == R.id.report) {
                        //TODO
                        Navigation.findNavController(v).navigate(R.id.action_discussionBoardFragment_to_reportFragment);
                    } else {
                        Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                popupMenu.show();
            });
        }



        // 下載Firebase storage的照片並顯示在ImageView上
        private void showImage(final ImageView imageView, final String path) {
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