package idv.tfp10105.project_forfun.discussionboard.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import idv.tfp10105.project_forfun.common.bean.Comment;
import idv.tfp10105.project_forfun.common.bean.Post;


public class discussionDetailFragment extends Fragment {
    private static final String TAG = "DetailFragment";
    private Activity activity;
    private TextView detailTitle, detailContext, detailMemberName, detailTime;
    private ImageView detailImageView;
    private Post post;
    private ImageButton detailBtMore, detailBtSent;
    private CircularImageView detailBtMemberHead;
    private RecyclerView rvDetail;
    private EditText detail_et_comment;
    private String imagePath;
    private Bundle bundle;
    private FirebaseStorage storage;
    private byte[] image;
    private List<Comment> comments;
    private List<Post> posts;
    private String url = Common.URL;
    private String name, headshot;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        //取bundle資料
        post = (Post) (getArguments() != null ? getArguments().getSerializable("post") : null);
        name = getArguments() != null ? getArguments().getString("name") : null;
        headshot = getArguments() != null ? getArguments().getString("headshot") : null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_detail, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        showPost();
        handleBtMore();
        handleRecyclerView();
        comments = getComments();
        showComment(comments);
        handleBtSent();
    }



    private void findViews(View view) {

        detailTitle = view.findViewById(R.id.detail_title_text);
        detailContext = view.findViewById(R.id.detail_context_text);
        detailImageView = view.findViewById(R.id.detail_imageView);
        detailMemberName = view.findViewById(R.id.detail_memberName_text);
        detailBtMore = view.findViewById(R.id.detail_bt_more);
        detailTime = view.findViewById(R.id.detail_time_text);
        detailBtMemberHead = view.findViewById(R.id.detail_bt_memberHead);
        rvDetail = view.findViewById(R.id.detail_recyclerView);
        detail_et_comment = view.findViewById(R.id.detail_et_comment);
        detailBtSent = view.findViewById(R.id.detail_bt_sent);

    }



    //顯示貼文內容
    private void showPost() {
        downloadImage(detailBtMemberHead,headshot);
        if (imagePath != "") {
            showImage(post.getPostImg());
            Log.d("post","post: " + post.toString());
        } else {
            detailImageView.setImageResource(R.drawable.no_image);
        }
        detailTitle.setText(post.getPostTitle());
        detailContext.setText(post.getPostContext());
        detailTime.setText(post.getCreateTime().toString());
        detailMemberName.setText(name);
    }

    private void handleRecyclerView() {
        rvDetail.setLayoutManager(new LinearLayoutManager(activity));
    }

    //連線資料庫取資料
    private List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "CommentController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("postId",post.getPostId());
            jsonObject.addProperty("action", "getAll");
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<Comment>>() {
            }.getType();

            //解析後端傳回資料
            comments = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "no network connection available", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "comments : " + comments, Toast.LENGTH_SHORT).show();
        return comments;
    }


    private void showComment(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            Toast.makeText(activity, "尚未有留言", Toast.LENGTH_SHORT).show();
        }
        //取得Adapter
        CommentAdapter commentAdapter = (CommentAdapter) rvDetail.getAdapter();
        // 如果spotAdapter不存在就建立新的，否則續用舊有的
        if (commentAdapter == null) {
            rvDetail.setAdapter(new CommentAdapter(activity,comments));
        } else {
            //更新Adapter資料,重刷
            commentAdapter.setAdapter(comments);

            //重新執行RecyclerView 三方法
            commentAdapter.notifyDataSetChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleBtMore() {
        detailBtMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                //新增
                if (itemId == R.id.update) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post", post);
                    Navigation.findNavController(v).navigate(R.id.discussionUpdateFragment, bundle);
                    //刪除
                } else if (itemId == R.id.delete) {
                    //實例化AlertDialog.Builder物件
                    new AlertDialog.Builder(activity)
                            .setTitle("是否刪除貼文")
                            // 設定確定按鈕-顯示文字及點擊監聽器
                            .setPositiveButton("確定", (dialog, which) -> {
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
                                        posts = getPosts();
                                        posts.remove(post);

                                        //刪除當前Fragment
//                                        FragmentManager fm = activity.getFragmentManager();
//                                        android.app.Fragment fragment = fm.findFragmentById(R.id.discussionDetailFragment);
//                                        FragmentTransaction ft = fm.beginTransaction();
//                                        ft.remove(fragment);

                                        // 外面post也必須移除選取的spot
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
                                }
                            })
                            // 設定否定按鈕-顯示文字及點擊監聽器
                            .setNegativeButton("取消", (dialog, which) ->
                            {
                                dialog.cancel(); })

                            // 設定是否可點擊對話框以外之處離開對話框
                            .setCancelable(false)
                            // 顯示對話框
                            .show();
                } else if (itemId == R.id.report) {
                            Navigation.findNavController(v).navigate(R.id.reportFragment);
                } else {
                    Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popupMenu.show();
        });
    }

    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<Comment> comments;

        public CommentAdapter(Context context, List<Comment> comments) {
            layoutInflater = LayoutInflater.from(context);
            this.comments = comments;
        }

        public void setAdapter(List<Comment> comments) {
            this.comments = comments;
        }


        private class MyViewHolder extends RecyclerView.ViewHolder {
            ImageButton comment_bt_report, comment_bt_more;
            TextView comment_memberName, comment_text;
            CircularImageView comment_bt_membetHead;

            public MyViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                comment_bt_membetHead = itemView.findViewById(R.id.comment_bt_membetHead);
                comment_memberName = itemView.findViewById(R.id.comment_memberName);
                comment_text = itemView.findViewById(R.id.comment_text);
                comment_bt_report = itemView.findViewById(R.id.comment_bt_report);
                comment_bt_more = itemView.findViewById(R.id.comment_bt_more);
            }
        }

        @Override
        public int getItemCount() {
            return comments == null ? 0 : comments.size();
        }


        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.comment_itemview,null, false);
            return new MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull @NotNull discussionDetailFragment.CommentAdapter.MyViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.comment_text.setText(comment.getCommentMsg());
            holder.comment_memberName.setText(name);
            downloadImage(holder.comment_bt_membetHead, headshot);
            holder.comment_bt_report.setOnClickListener(v -> {
                    Navigation.findNavController(v).navigate(R.id.reportFragment);
            });

            holder.comment_bt_more.setOnClickListener(v -> {
                PopupMenu popupComment = new PopupMenu(activity, v, Gravity.END);
                popupComment.inflate(R.menu.comment_popup_menu);
                popupComment.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    //新增
                    if (itemId == R.id.updateComment) {
                        final EditText et = new EditText(activity);
                        new AlertDialog.Builder(activity).setTitle("請更新留言")
                                .setView(et)
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //按下確定键後的事件
                                        if (RemoteAccess.networkCheck(activity)){
                                            String msg = et.getText().toString().trim();
                                            Comment comment = comments.get(position);
                                            comment.setCommentMsg(msg);
                                            int commentId = comment.getCommentId();
                                            url = Common.URL + "CommentController";
//                                            Comment comment = new Comment(commentId,1,post.getPostId(),msg);
                                            JsonObject jsonInsert = new JsonObject();
                                            jsonInsert.addProperty("action","commentUpdate");
                                            jsonInsert.addProperty("commentId", commentId);
                                            jsonInsert.addProperty("comment", new Gson().toJson(comment));
                                            int count;
                                            //執行緒池物件
                                            String result = RemoteAccess.getJsonData(url, jsonInsert.toString());
                                            //新增筆數
                                            count = Integer.parseInt(result);
                                            //筆數為0
                                            if (count == 0) {
                                                Toast.makeText(activity, "修改失敗", Toast.LENGTH_SHORT).show();
                                            } else {
                                                CommentAdapter.this.notifyDataSetChanged();
                                                Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(activity, "沒有網路連線", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).setNegativeButton("取消",null).show();
                    } else if (itemId == R.id.deleteComment) {
                        if (RemoteAccess.networkCheck(activity)) {
                            url = Common.URL + "CommentController";
                            Log.d(TAG,"URL: " + url);
                            JsonObject jsonDelete = new JsonObject();
                            jsonDelete.addProperty("action", "commentDelete");
                            jsonDelete.addProperty("commentId", comment.getCommentId());
                            int count;
                            String result = RemoteAccess.getJsonData(url, jsonDelete.toString());
                            count = Integer.parseInt(result);
                            if (count == 0) {
                                Toast.makeText(activity, "刪除失敗", Toast.LENGTH_SHORT).show();
                            } else {
                                comments.remove(comment);
                                CommentAdapter.this.notifyDataSetChanged();
                            }
                        }
                    }
                    return true;
                });
                popupComment.show();
            });
        }
    }

    private void handleBtSent() {
        detailBtSent.setOnClickListener(v -> {
            String commentMgs = detail_et_comment.getText().toString().trim();
            if (commentMgs.length() <= 0) {
                Toast.makeText(activity, "Comment is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            if (RemoteAccess.networkCheck(activity)) {
                url = Common.URL + "CommentController";
                Comment comment = new Comment(0,1,post.getPostId(),commentMgs);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action","commentInsert");
                jsonObject.addProperty("comment", new Gson().toJson(comment));
                int count;
                //執行緒池物件
                String result = RemoteAccess.getJsonData(url,jsonObject.toString());
                //新增筆數
                count = Integer.parseInt(result);
                //筆數為0
                if (count == 0) {
                    Toast.makeText(activity, "新增失敗", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "新增成功", Toast.LENGTH_SHORT).show();

                    CommentAdapter commentAdapter = (CommentAdapter) rvDetail.getAdapter();

                    commentAdapter.notifyDataSetChanged();


                }
            }
        });
    }

    //連線資料庫取資料
    private List<Post> getPosts() {
        List<Post> posts = null;
        if (RemoteAccess.networkCheck(activity)) {
            url = Common.URL + "DiscussionBoardController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<Post>>() {
            }.getType();

            //解析後端傳回資料
            posts = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "no network connection available", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "posts : " + posts, Toast.LENGTH_SHORT).show();
        return posts;
    }


    private void showImage(final String imagePath) {
        final int ONE_MEGABYTE = 1024 * 1024;
        StorageReference imageRef = storage.getReference().child(imagePath);
        //最多能暫存記憶體的量
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        image = task.getResult();
                        //轉bitmap呈現前端
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        detailImageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ? "Download fail" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        detailImageView.setImageResource(R.drawable.no_image);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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
}

