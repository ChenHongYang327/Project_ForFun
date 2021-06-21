package idv.tfp10105.project_forfun.discussionboard.disboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.commend.Commend;
import idv.tfp10105.project_forfun.commend.RemoteAccess;
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;
import idv.tfp10105.project_forfun.commend.Post;
import idv.tfp10105.project_forfun.orderconfirm.ocf.Book;

public class discussionBoard_RentSeekingFragment extends Fragment {
    private final static  String TAG = "TAG_RentSeekingFragment";
    private RecyclerView rv_seeking;
    private Activity activity;
    private ExecutorService executor;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseStorage storage;
    //外部列表 第一頁 列表
//    private List<Post> posts;
    // 假資料
    private List<Book> books;
    private SearchView searchView;
    private FloatingActionButton bt_Add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        //需要開啟多個執行緒取得各景點圖片，使用執行緒池功能
        int numProcs = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + numProcs);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        //執行緒池管理物件·
        executor = Executors.newFixedThreadPool(numProcs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion_board_rent_seeking, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleRecyclerView();
//        posts = getPosts();
//        showPosts(posts);

        //fake
        books = getBookList();
        showBooks(books);

        handleBtAdd();
        handleSearchView();
        handleSwipeRefresh();
    }

    private void findViews(View view) {
        searchView = view.findViewById(R.id.searchView_dis);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_seek);
        rv_seeking = view.findViewById(R.id.rv_seeking);
        bt_Add = view.findViewById(R.id.dis_bt_Add);
    }

    private void handleSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //開始動畫
            swipeRefreshLayout.setRefreshing(true);
            //重新載入recycleView
//            showPosts(posts);

            //fake
            showBooks(books);
            //結束動畫
            swipeRefreshLayout.setRefreshing(false);
        });
    }


    //fake data
    private void handleSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                //如果輸入條件為空字串,就顯示原始資料,否則就顯示收尋後結果
                if (newText.isEmpty()) {
                    showBooks(books);
                }else {
                    List<Book> searchArticles = new ArrayList<>();
                    //搜尋原始資料內有無包含關鍵字（不區分大小寫）
                    for (Book book : books) {
                        if (book.getTitle().toUpperCase().contains(newText.toUpperCase())) {
                            searchArticles.add(book);
                        }
                    }
                    showBooks(searchArticles);
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }





//    private void handleSearchView() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //如果輸入條件為空字串,就顯示原始資料,否則就顯示收尋後結果
//                if (newText.isEmpty()) {
//                    showPosts(posts);
//                }else {
//                    List<Post> searchArticles = new ArrayList<>();
//                    //搜尋原始資料內有無包含關鍵字（不區分大小寫）
//                    for (Post post : posts) {
//                        if (post.getPostTitle().toUpperCase().contains(newText.toUpperCase())) {
//                            searchArticles.add(post);
//                        }
//                    }
//                    showPosts(searchArticles);
//                }
//                return true;
//            }
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//        });
//    }


    private void handleBtAdd() {
        //跳轉至新增頁面
        bt_Add.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_discussionBoard_RentSeekingFragment_to_discussionInsertFragment));
    }

    private void handleRecyclerView() {

        rv_seeking.setLayoutManager(new LinearLayoutManager(activity));
        rv_seeking.addItemDecoration(new ItemDecoration(30,activity));//30代表30dp
    }


    //連線資料庫取資料

    private List<Post> getPosts() {
        List<Post> posts = null;
        if (RemoteAccess.networkCheck(activity)) {
            String url = Commend.URL + "";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<Post>>() {
            }.getType();
            posts = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "no network connection available", Toast.LENGTH_SHORT).show();
        }
        return posts;
    }

    // fake data
    private void showBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            Toast.makeText(activity, "no posts found", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        SeekAdapter seekAdapter = (SeekAdapter) rv_seeking.getAdapter();

        if (seekAdapter == null) {
            rv_seeking.setAdapter(new SeekAdapter(activity, getBookList()));
        }else {
            //更新Adapter資料,重刷
            seekAdapter.setAdapter(books);

            seekAdapter.setAdapter(books);

            //重新執行RecyclerView 三方法
            seekAdapter.notifyDataSetChanged();
        }
    }




//    private void showPosts(List<Post> posts) {
//        if (posts == null || posts.isEmpty()) {
//            Toast.makeText(activity, "no posts found", Toast.LENGTH_SHORT).show();
//        }
//            //取得Adapter
//            SeekAdapter seekAdapter = (SeekAdapter) rv_seeking.getAdapter();
//
//            if (seekAdapter == null) {
//                rv_seeking.setAdapter(new SeekAdapter(activity, posts));
//            }else {
//                //更新Adapter資料,重刷
//                seekAdapter.setAdapter(posts);
//
//
//                seekAdapter.setAdapter(posts);
//
//                //重新執行RecyclerView 三方法
//                seekAdapter.notifyDataSetChanged();
//        }
//    }


    private  class SeekAdapter extends RecyclerView.Adapter<SeekAdapter.MyViewHolder> {

        private final LayoutInflater layoutInflater;
        private final int imageSize;
        //內部列表（搜尋後）
        private List<Post> posts;
        //fake data
        private List<Book> books;


        // fake data
        public SeekAdapter(Context context, List<Book> books) {
            layoutInflater = LayoutInflater.from(context);
            this.books = books;
            //螢幕寬度除以四當圖片尺寸
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }


//        public SeekAdapter(Context context, List<Post> posts) {
//            layoutInflater = LayoutInflater.from(context);
//            this.posts = posts;
//            //螢幕寬度除以四當圖片尺寸
//            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
//        }

        //重刷RecyclerView畫面
//        public void setAdapter (List<Post> posts) {
//            this.posts = posts;
//        }

        // fake data
        public void setAdapter(List<Book> books) {
            this.books = books;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView disPostName, disPostTitle, disPostContext, disPostTime;
            ImageButton disPostBtMore, disPostMemberImg;
            ImageView disPostImg;

            public MyViewHolder(@NonNull @NotNull View itemView) {
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
//        public int getItemCount() {
//            return posts == null ? 0 : posts.size();
//        }

        // fake data
        public int getItemCount() {
            return books == null ? 0 : books.size();
        }


        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.discussion_itemview, null, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull discussionBoard_RentSeekingFragment.SeekAdapter.MyViewHolder holder, int position) {
            // fake data
            final Book book = books.get(position);
            holder.disPostTitle.setText(book.getTitle());
            holder.disPostName.setText(book.getImageId().toString());
            holder.disPostImg.setImageResource(R.drawable.post_houseimg_test);
            holder.disPostMemberImg.setImageResource(R.drawable.post_memberhead);
            //設定點擊事件
//            holder.disPostImg.setOnClickListener(v -> {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("book", book);
//                //轉至詳細頁面
//                Navigation.findNavController(v).navigate(R.id.action_discussionBoard_RentSeekingFragment_to_discussionDetailFragment,bundle);
        }

//    });


//            final Post post = posts.get(position);
//            holder.disPostTitle.setText(post.getPostTitle());
//            holder.disPostName.setText(post.getPostId());
//            holder.disPostContext.setText(post.getContext());
//            holder.disPostTime.setText(post.getCreateTime().toString());
//            //設定點擊事件
//            holder.disPostImg.setOnClickListener(v -> {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("post", post);
//                //轉至詳細頁面
//                Navigation.findNavController(v).navigate(R.id.action_discussionBoardFragment_to_discussionDetailFragment,bundle);
//            });
        }
//    }


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
                        Log.e(TAG, message);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Book> getBookList() {
        List<Book> bookList = new ArrayList<>();
        bookList.add(new Book(1, "A突破困境：資安開源工具應用"));
        bookList.add(new Book(2, "B大話 AWS 雲端架構：雲端應用架構圖解輕鬆學"));
        bookList.add(new Book(3, "C為你自己學 Git"));
        bookList.add(new Book(4, "D無瑕的程式碼－整潔的軟體設計與架構篇"));
        bookList.add(new Book(5, "E大話設計模式"));
        bookList.add(new Book(6, "F無瑕的程式碼－敏捷軟體開發技巧守則"));
        bookList.add(new Book(7, "GiOS App 程式開發實務攻略：快速精通 SwiftUI"));
        return bookList;
    }

}