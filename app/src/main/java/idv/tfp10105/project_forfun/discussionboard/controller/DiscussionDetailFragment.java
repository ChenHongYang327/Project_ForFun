package idv.tfp10105.project_forfun.discussionboard.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import idv.tfp10105.project_forfun.common.KeyboardUtils;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.TimeUtil;
import idv.tfp10105.project_forfun.common.bean.Comment;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.Post;
import idv.tfp10105.project_forfun.discussionboard.ItemDecoration;


public class DiscussionDetailFragment extends Fragment {
    private static final String TAG = "DetailFragment";
    private Activity activity;
    private TextView detailTitle, detailContext, detailMemberName, detailTime;
    private ImageView detailImageView;
    private Post post;
    private ImageButton detailBtMore, detailBtSent;
    private CircularImageView detailBtMemberHead;
    private RecyclerView rvDetail;
    private EditText detail_et_comment;
    private Bundle bundle;
    private FirebaseStorage storage;
    private byte[] image;
    private List<Comment> comments;
    private List<Post> posts;
    private String url = Common.URL;
    private String name, headshot, boardId;
    private String imagePath = "Project_ForFun/Discussion_insert/no_image.jpg";
    private List<Member> members;
    private Integer memberId;
    private SharedPreferences sharedPreferences;
    private CommentAdapter commentAdapter;
    private Member member;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        storage = FirebaseStorage.getInstance();
        //???bundle??????
        post = (Post) (getArguments() != null ? getArguments().getSerializable("post") : null);
        name = getArguments() != null ? getArguments().getString("name") : null;
        headshot = getArguments() != null ? getArguments().getString("headshot") : null;
        boardId = getArguments() != null ? getArguments().getString("boardId") : null;

        sharedPreferences = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        memberId = sharedPreferences.getInt("memberId", -1);
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
        showComment(comments, members);
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


    //??????????????????
    private void showPost() {
        if (imagePath != "") {
            showImage(post.getPostImg());
            Log.d("post", "post: " + post.toString());
        } else {
            detailImageView.setImageResource(R.drawable.no_image);
        }
        detailTitle.setText(post.getPostTitle());
        detailContext.setText(post.getPostContext());
        detailTime.setText(TimeUtil.getChatTimeStr(post.getCreateTime().getTime()));
        downloadImage(detailBtMemberHead, headshot);
        detailMemberName.setText(name);

        detailBtMemberHead.setOnClickListener(v -> {
            Member memberPersonal = getMemberByOwnerId(post.getPosterId());
            Bundle bundle = new Bundle();
            bundle.putSerializable("SelectUser", memberPersonal);
            Navigation.findNavController(v).navigate(R.id.personalSnapshotFragment, bundle);

        });
    }

    private void handleRecyclerView() {
        rvDetail.setLayoutManager(new LinearLayoutManager(activity));
        rvDetail.addItemDecoration(new ItemDecoration(10, activity));
    }

    //????????????????????????
    private List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "CommentController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("postId", post.getPostId());
            jsonObject.addProperty("action", "getAll");
            //???????????????
            jsonObject.addProperty("reqMemberId", memberId);
            //------
            JsonObject jsonIn = new Gson().fromJson(RemoteAccess.getJsonData(url, jsonObject.toString()), JsonObject.class);
            Type listType = new TypeToken<List<Comment>>() {
            }.getType();

            //????????????????????????
            comments = new Gson().fromJson(jsonIn.get("commentList").getAsString(), listType);
        } else {
            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(activity, "comments : " + comments, Toast.LENGTH_SHORT).show();
        return comments;
    }

    // ??????????????????
    private List<Member> getMembers() {

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "CommentController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("postId", post.getPostId());
            jsonObject.addProperty("action", "getAll");
            //???????????????
            jsonObject.addProperty("reqMemberId", memberId);
            JsonObject jsonIn = new Gson().fromJson(RemoteAccess.getJsonData(url, jsonObject.toString()), JsonObject.class);
            Type listMember = new TypeToken<List<Member>>() {
            }.getType();

            //????????????????????????
            members = new Gson().fromJson(jsonIn.get("memberList").getAsString(), listMember);

        } else {
            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(activity, "members : " + members, Toast.LENGTH_SHORT).show();

        return members;
    }


    private void showComment(List<Comment> comments, List<Member> members) {
        if (comments == null || comments.isEmpty()) {
//            Toast.makeText(activity, "???????????????", Toast.LENGTH_SHORT).show();
        }
        //??????Adapter
        commentAdapter = new CommentAdapter(activity, comments, getMembers());
        rvDetail.setAdapter(commentAdapter);
//         commentAdapter = (CommentAdapter) rvDetail.getAdapter();
//        // ??????spotAdapter????????????????????????????????????????????????
//        if (commentAdapter == null) {
//            rvDetail.setAdapter(new CommentAdapter(activity,comments, getMembers()));
//        } else {
//            //??????Adapter??????,??????
//            commentAdapter.setAdapter(comments, members);
//
//            //????????????RecyclerView ?????????
//            commentAdapter.notifyDataSetChanged();

    }

    private Member getMemberByOwnerId(int ownerId) {
        Member membershot = null;

        if (RemoteAccess.networkCheck(activity)) {
            String url = Common.URL + "/memberCenterPersonalInformation";
            JsonObject request = new JsonObject();
            request.addProperty("action", "getMember");
            request.addProperty("member_id", ownerId);

            String jsonResule = RemoteAccess.getJsonData(url, new Gson().toJson(request));

            membershot = new Gson().fromJson(jsonResule, Member.class);
        }

        return membershot;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleBtMore() {
        detailBtMore.setOnClickListener(v -> {
            // ??????????????????
            int role = sharedPreferences.getInt("role", -1);
            if (role == 3) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("????????????");
                dialog.setMessage("?????????????????????");
                dialog.setPositiveButton("??????", null);
                Window window = dialog.show().getWindow();
                // ??????????????????
                Button btnOK = window.findViewById(android.R.id.button1);
                btnOK.setTextColor(getResources().getColor(R.color.black));

                return;
            } else {

                PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                popupMenu.inflate(R.menu.popup_menu);

                if (memberId == post.getPosterId()) {

                    Log.d("posterId", ":" + post.getPosterId());
                    Log.d("memberId", ":" + memberId);

                    popupMenu.getMenu().getItem(0).setVisible(true);
                    popupMenu.getMenu().getItem(1).setVisible(true);
                    popupMenu.getMenu().getItem(2).setVisible(false);
                } else {
                    popupMenu.getMenu().getItem(0).setVisible(false);
                    popupMenu.getMenu().getItem(1).setVisible(false);
                    popupMenu.getMenu().getItem(2).setVisible(true);
                }


                popupMenu.setOnMenuItemClickListener(item -> {

                    int itemId = item.getItemId();
                    //??????
                    if (itemId == R.id.update) {
                        Bundle bundle2 = new Bundle();
                        bundle2.putString("name", name);
                        bundle2.putString("headshot", headshot);
                        bundle2.putSerializable("post", post);
                        Navigation.findNavController(v).navigate(R.id.discussionUpdateFragment, bundle2);
                        //??????
                    } else if (itemId == R.id.delete) {
                        //?????????AlertDialog.Builder??????
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

                        alertDialog.setTitle("??????????????????");
                        // ??????????????????-??????????????????????????????
                        alertDialog.setPositiveButton("??????", (dialog, which) -> {

                            if (RemoteAccess.networkCheck(activity)) {
                                url = Common.URL + "DiscussionBoardController";
                                JsonObject jsonDelete = new JsonObject();
                                jsonDelete.addProperty("action", "postDelete");
                                jsonDelete.addProperty("postId", post.getPostId());
                                int count;
                                String result = RemoteAccess.getJsonData(url, jsonDelete.toString());
                                count = Integer.parseInt(result);
                                if (count == 0) {
                                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();

                                    switch (boardId) {
                                        case "?????????":
                                            Navigation.findNavController(v).popBackStack(R.id.discussionDetailFragment, true);
                                            Navigation.findNavController(v).navigate(R.id.discussionBoardFragment);

                                        case "????????????":
                                            Navigation.findNavController(v).popBackStack(R.id.discussionDetailFragment, true);
                                            Navigation.findNavController(v).navigate(R.id.discussionBoardFragment);
                                        default:
                                            Navigation.findNavController(v).popBackStack(R.id.discussionDetailFragment, true);
                                            Navigation.findNavController(v).navigate(R.id.discussionBoardFragment);
                                    }


                                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        // ??????????????????-??????????????????????????????
                        alertDialog.setNegativeButton("??????", (dialog, which) ->
                        {
                            dialog.cancel();
                        });

                        // ?????????????????????????????????????????????????????????
                        alertDialog.setCancelable(false);
                        // ???????????????
                        Window window = alertDialog.show().getWindow();
                        //?????????????????????
                        Button btSure = window.findViewById(android.R.id.button1);
                        Button btCancel = window.findViewById(android.R.id.button2);
                        btSure.setTextColor(getResources().getColor(R.color.black));
                        btCancel.setTextColor(getResources().getColor(R.color.black));
                    } else if (itemId == R.id.report) {
                        Bundle bundle = new Bundle();
                        // ?????????
                        bundle.putInt("WHISTLEBLOWER_ID", memberId);
                        //????????????
                        bundle.putInt("REPORTED_ID", post.getPosterId());
                        //????????????
                        bundle.putInt("POST_ID", post.getPostId());
                        //????????????
                        bundle.putInt("ITEM", 0);

                        Navigation.findNavController(v).navigate(R.id.reportFragment, bundle);
                    } else {
                        Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });

                popupMenu.show();


            }
        });
    }

    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<Comment> comments;
        private List<Member> members;

        public CommentAdapter(Context context, List<Comment> comments, List<Member> members) {
            layoutInflater = LayoutInflater.from(context);
            this.comments = comments;
            this.members = members;
        }

        public void setAdapter(List<Comment> comments, List<Member> members) {
            this.comments = comments;
            this.members = members;
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

        public void updateData(List<Comment> comments, List<Member> members) {
            this.comments = comments;
            this.members = members;
            CommentAdapter.this.notifyDataSetChanged();
        }


        @Override
        public int getItemCount() {
            return comments == null ? 0 : comments.size();
        }


        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.comment_itemview, null, false);
            return new MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull @NotNull DiscussionDetailFragment.CommentAdapter.MyViewHolder holder, int position) {
            final Comment comment = comments.get(position);
            Member member = members.get(position);
            holder.comment_text.setText(comment.getCommentMsg());
            holder.comment_memberName.setText(member.getNameL() + member.getNameF());
            downloadImage(holder.comment_bt_membetHead, member.getHeadshot());

            holder.comment_bt_membetHead.setOnClickListener(v -> {
                Member memberPersonal = getMemberByOwnerId(comment.getMemberId());
                Bundle bundle = new Bundle();
                bundle.putSerializable("SelectUser", memberPersonal);
                Navigation.findNavController(v).navigate(R.id.personalSnapshotFragment, bundle);

            });

            if (memberId == comment.getMemberId()) {

                holder.comment_bt_report.setEnabled(false);
            } else {
                holder.comment_bt_report.setOnClickListener(v -> {

                    int role = sharedPreferences.getInt("role", -1);
                    if (role == 3) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle("????????????");
                        dialog.setMessage("?????????????????????");
                        dialog.setPositiveButton("??????", null);

                        Window window = dialog.show().getWindow();
                        // ??????????????????
                        Button btnOK = window.findViewById(android.R.id.button1);
                        btnOK.setTextColor(getResources().getColor(R.color.black));

                        return;
                    } else {

                        Bundle bundle = new Bundle();
                        // ?????????
                        bundle.putInt("WHISTLEBLOWER_ID", memberId);
                        //????????????
                        bundle.putInt("REPORTED_ID", comment.getMemberId());
                        //????????????
                        bundle.putInt("CHATROOM_ID", comment.getCommentId());
                        //????????????
                        bundle.putInt("ITEM", 1);

                        Navigation.findNavController(v).navigate(R.id.reportFragment, bundle);

                    }

                });

            }



            holder.comment_bt_more.setOnClickListener(v -> {

                // ??????????????????
                int role = sharedPreferences.getInt("role", -1);
                if (role == 3) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                    dialog.setTitle("????????????");
                    dialog.setMessage("?????????????????????");
                    dialog.setPositiveButton("??????", null);

                    Window window = dialog.show().getWindow();
                    // ??????????????????
                    Button btnOK = window.findViewById(android.R.id.button1);
                    btnOK.setTextColor(getResources().getColor(R.color.black));

                    return;
                } else {
                    PopupMenu popupComment = new PopupMenu(activity, v, Gravity.END);
                    popupComment.inflate(R.menu.comment_popup_menu);

                    if (memberId == comment.getMemberId()) {

                        Log.d("posterId", ":" + post.getPosterId());
                        Log.d("memberId", ":" + memberId);

                        popupComment.getMenu().getItem(0).setVisible(true);
                        popupComment.getMenu().getItem(1).setVisible(true);

                    } else {
                        popupComment.getMenu().getItem(0).setVisible(false);
                        popupComment.getMenu().getItem(1).setVisible(false);
                       ;
                    }

                    popupComment.setOnMenuItemClickListener(item -> {
                        int itemId = item.getItemId();
                        //??????
                        if (itemId == R.id.updateComment) {
                            final EditText et = new EditText(activity);
                            et.setText(comment.getCommentMsg());
                            AlertDialog.Builder alert = new AlertDialog.Builder(activity).setTitle("???????????????");
                            alert.setView(et);
                            alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //???????????????????????????
                                    if (RemoteAccess.networkCheck(activity)) {
                                        String msg = et.getText().toString().trim();
                                        Comment comment = comments.get(position);
                                        comment.setCommentMsg(msg);
                                        int commentId = comment.getCommentId();
                                        url = Common.URL + "CommentController";
//                                            Comment comment = new Comment(commentId,1,post.getPostId(),msg);
                                        JsonObject jsonInsert = new JsonObject();
                                        jsonInsert.addProperty("action", "commentUpdate");
                                        jsonInsert.addProperty("commentId", commentId);
                                        jsonInsert.addProperty("comment", new Gson().toJson(comment));
                                        int count;
                                        //??????????????????
                                        String result = RemoteAccess.getJsonData(url, jsonInsert.toString());
                                        //????????????
                                        count = Integer.parseInt(result);
                                        //?????????0
                                        if (count == 0) {
                                            Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                                        } else {
                                            CommentAdapter.this.notifyDataSetChanged();
                                            Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alert.setNegativeButton("??????", null);
                            // ???????????????
                            Window window = alert.show().getWindow();
                            //?????????????????????
                            Button btSure = window.findViewById(android.R.id.button1);
                            Button btCancel = window.findViewById(android.R.id.button2);
                            btSure.setTextColor(getResources().getColor(R.color.black));
                            btCancel.setTextColor(getResources().getColor(R.color.black));

                        } else if (itemId == R.id.deleteComment) {
                            if (RemoteAccess.networkCheck(activity)) {
                                url = Common.URL + "CommentController";
                                Log.d(TAG, "URL: " + url);
                                JsonObject jsonDelete = new JsonObject();
                                jsonDelete.addProperty("action", "commentDelete");
                                jsonDelete.addProperty("commentId", comment.getCommentId());
                                int count;
                                String result = RemoteAccess.getJsonData(url, jsonDelete.toString());
                                count = Integer.parseInt(result);
                                if (count == 0) {
                                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                                } else {
                                    comments.remove(comment);
                                    CommentAdapter.this.notifyDataSetChanged();
                                }
                            }
                        }
                        return true;
                    });
                    popupComment.show();
                }


            });
        }
    }

    private void handleBtSent() {

        detailBtSent.setOnClickListener(v -> {
            // ??????????????????
            int role = sharedPreferences.getInt("role", -1);
            if (role == 3) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("????????????");
                dialog.setMessage("?????????????????????");
                dialog.setPositiveButton("??????", null);

                Window window = dialog.show().getWindow();
                // ??????????????????
                Button btnOK = window.findViewById(android.R.id.button1);
                btnOK.setTextColor(getResources().getColor(R.color.black));
                return;

            } else {
                String commentMgs = detail_et_comment.getText().toString().trim();
                detail_et_comment.setText("");
                KeyboardUtils.hideKeyboard(activity);
                if (commentMgs.length() <= 0) {
                    Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (RemoteAccess.networkCheck(activity)) {
                    url = Common.URL + "CommentController";
                    Comment comment = new Comment(0, memberId, post.getPostId(), commentMgs);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "commentInsert");
                    jsonObject.addProperty("comment", new Gson().toJson(comment));
                    int count;
                    //??????????????????
                    String result = RemoteAccess.getJsonData(url, jsonObject.toString());
                    //????????????
                    count = Integer.parseInt(result);
                    //?????????0
                    if (count == 0) {
//                        Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();

                        comments = getComments();
                        members = getMembers();
                        commentAdapter.updateData(comments, getMembers());


                    }
                }
            }


        });
    }

    //????????????????????????
    private List<Post> getPosts() {
        List<Post> posts = null;
        if (RemoteAccess.networkCheck(activity)) {
            url = Common.URL + "DiscussionBoardController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            //???????????????
            jsonObject.addProperty("reqMemberId", memberId);
            String jsonIn = RemoteAccess.getJsonData(url, jsonObject.toString());
            Type listType = new TypeToken<List<Post>>() {
            }.getType();

            //????????????????????????
            posts = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, "??????????????????", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(activity, "posts : " + posts, Toast.LENGTH_SHORT).show();
        return posts;
    }


    private void showImage(final String imagePath) {
        final int ONE_MEGABYTE = 1024 * 1024 * 10;
        StorageReference imageRef = storage.getReference().child(imagePath);
        //??????????????????????????????
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        image = task.getResult();
                        //???bitmap????????????
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        detailImageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ? "????????????" : task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        detailImageView.setImageResource(R.drawable.no_image);
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ??????Firebase storage?????????????????????ImageView???
    private void downloadImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024 * 10;
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ?
                                "????????????" + ": " + path : task.getException().getMessage() + ": " + path;
                        imageView.setImageResource(R.drawable.no_image);
                        Log.e(TAG, message);
//                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

