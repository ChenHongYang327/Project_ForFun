package idv.tfp10105.project_forfun.commend;

import java.io.Serializable;
import java.util.Date;

public class Post implements Serializable {

    private Integer postId;
    private Integer boardId;
    private Integer posterId;
    private String postTitle;
    private String postImg;
    private String posterImg;
    private String context;
    private Date createTime;
    private Date updateTime;
    private Date deleteTime;

    public Post() {
    }

    public Post(Integer postId, Integer boardId, Integer posterId, String postTitle, String postImg,
                String posterImg, String context, Date createTime, Date updateTime, Date deleteTime)
    {
        this.postId = postId;
        this.boardId = boardId;
        this.posterId = posterId;
        this.postTitle = postTitle;
        this.postImg = postImg;
        this.posterImg = posterImg;
        this.context = context;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleteTime = deleteTime;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public Integer getPosterId() {
        return posterId;
    }

    public void setPosterId(Integer posterId) {
        this.posterId = posterId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
    }

    public String getPosterImg() {
        return posterImg;
    }

    public void setPosterImg(String posterImg) {
        this.posterImg = posterImg;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }
}
