package idv.tfp10105.project_forfun.discussionboard.disboard;

import android.app.Activity;
import android.content.Context;
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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Post;
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;

public class discussionBoard_RentHouseFragment extends Fragment {
    private final static String TAG = "TAG_RentSeekingFragment";
    private RecyclerView rv_rent;
    private Activity activity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseStorage storage;
    //外部列表 第一頁 列表
    private List<Post> posts;
    private SearchView searchView;
    private FloatingActionButton bt_Add;
    private Post post;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_board_rent_house, container, false);
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
        showPosts(posts);
    }

    private void findViews(View view) {
        searchView = view.findViewById(R.id.searchView_rent);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_rent);
        rv_rent = view.findViewById(R.id.rv_rent);
        bt_Add = view.findViewById(R.id.dis_bt_Add_rent);
    }

    private void handleSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //開始動畫
            swipeRefreshLayout.setRefreshing(true);
            //重新載入recycleView
            showPosts(posts);
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
                    showPosts(posts);
                } else {
                    List<Post> searchPosts = new ArrayList<>();
                    //搜尋原始資料內有無包含關鍵字（不區分大小寫）
                    for (Post post : posts) {
                        if (post.getPostTitle().toUpperCase().contains(newText.toUpperCase())) {
                            searchPosts.add(post);
                        }
                    }
                    showPosts(searchPosts);
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

        rv_rent.setLayoutManager(new LinearLayoutManager(activity));
        rv_rent.addItemDecoration(new ItemDecoration(30, activity));//30代表30dp
    }


    //連線資料庫取資料
    private List<Post> getPosts() {
        List<Post> posts = null;
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "DiscussionBoardController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("boardId", "租屋交流");
            jsonObject.addProperty("action", "getAll");
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<Post>>() {
            }.getType();

            //解析後端傳回資料
            posts = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "posts : " + posts, Toast.LENGTH_SHORT).show();
        return posts;
    }


    private void showPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            Toast.makeText(activity, "沒有貼文", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        RentAdapter rentAdapter = (RentAdapter) rv_rent.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (rentAdapter == null) {
            rv_rent.setAdapter(new RentAdapter(activity, posts));
        } else {
            //更新Adapter資料,重刷
            rentAdapter.setAdapter(posts);

            //重新執行RecyclerView 三方法
            rentAdapter.notifyDataSetChanged();
        }
    }


    private class RentAdapter extends RecyclerView.Adapter<RentAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private final int imageSize;
        //內部列表（搜尋後）
        private List<Post> posts;



        public RentAdapter(Context context, List<Post> posts) {
            layoutInflater = LayoutInflater.from(context);
            this.posts = posts;
            //螢幕寬度除以四當圖片尺寸
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        //        重刷RecyclerView畫面
        public void setAdapter(List<Post> posts) {
            this.posts = posts;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView disPostName, disPostTitle, disPostContext, disPostTime;
            ImageButton disPostBtMore, disPostMemberImg;
            ImageView disPostImg;

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
        public RentAdapter.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.discussion_itemview, null, false);
            return new MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull @NotNull RentAdapter.MyViewHolder holder, int position) {

            final Post post = posts.get(position);
            holder.disPostTitle.setText(post.getPostTitle());
            //TODO
            holder.disPostName.setText("6");
            holder.disPostContext.setText(post.getPostContext());
            holder.disPostTime.setText(post.getCreateTime().toString());

            String url = Common.URL + "DiscussionBoardController";
            int postId = post.getPostId();
            String imagePath = post.getPostImg();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("imagePath", imagePath);
            jsonObject.addProperty("postId", postId);
            jsonObject.addProperty("imageSize", imageSize);
            String jsonImg = RemoteAccess.getJsonData(url, jsonObject.toString());

            //設定點擊事件
            holder.disPostImg.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
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
                controller(v, post, url);
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void controller (View v, Post post, String url) {
            //選單
            PopupMenu popupMenu = new PopupMenu(activity,v, Gravity.END);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                //新增
                if (itemId == R.id.update){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post",post);
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
                            RentAdapter.this.notifyDataSetChanged();
                            // 外面spots也必須移除選取的spot
                            discussionBoard_RentHouseFragment.this.posts.remove(post);
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
                    } else if (itemId == R.id.report) {
                        //TODO
//                            Navigation.findNavController(v).navigate("路徑");
                    } else {
                        Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
            popupMenu.show();
            return;
        }


        // 下載Firebase storage的照片並顯示在ImageView上
        private void showImage(final ImageView imageView, final String path) {
            final int ONE_MEGABYTE = 1024 * 1024;
            StorageReference imageRef = storage.getReference().child(path);
            if (imageRef == null || path == null) {
                imageView.setImageResource(R.drawable.no_image);
            } else {
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
}