package cn.xmu.pojo.bo;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotBlank;

/**
 * 文章留言的BO
 */
public class CommentReplyBO {

    @NotBlank(message = "留言信息不完整")
    private String articleId;

    //评论回复了其他人的id
    @NotBlank(message = "留言信息不完整")
    private String fatherId;

    @NotBlank(message = "当前用户信息不正确，请尝试重新登录")
    private String commentUserId;

    @NotBlank(message = "留言内容不能为空")
    @Length(max = 50, message = "文章内容长度不能超过50")
    private String content;

    @Value("匿名用户")
    private String nickname;

    public String getNickname() {
        return nickname;
    }
    public String getCommentUserId() {
        return commentUserId;
    }

    public void setCommentUserId(String commentUserId) {
        this.commentUserId = commentUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFatherId() {
        return fatherId;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    @Override
    public String toString() {
        return "CommentReplyBO{" +
                "articleId='" + articleId + '\'' +
                ", fatherId='" + fatherId + '\'' +
                ", commentUserId='" + commentUserId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
