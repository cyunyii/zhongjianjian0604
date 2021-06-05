package cn.xmu.article.controller;


import cn.xmu.api.BaseController;
import cn.xmu.api.article.CommentControllerApi;
import cn.xmu.article.service.CommentPortalService;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.pojo.bo.CommentReplyBO;
import cn.xmu.utils.PagedGridResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class CommentController extends BaseController implements CommentControllerApi {

    final static Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentPortalService commentPortalService;

    //@Valid未登录测试前去掉
    @Override
    public GraceJSONResult createArticle(@Valid CommentReplyBO commentReplyBO){
//                                         BindingResult result) {
        //                                         BindingResult result) {
//        // 0. 判断BindingResult是否保存错误的验证信息，如果有，则直接return
//        if (result.hasErrors()) {
//            Map<String, String> errorMap = getErrors(result);
//            return GraceJSONResult.errorMap(errorMap);
//        }

        // 1. 根据留言用户的id查询他的昵称，用于存入到数据表进行字段的冗余处理，从而避免多表关联查询的性能影响
        String userId = String.valueOf(commentReplyBO.getCommentUserId());

        // 2. 发起restTemplate调用用户服务，获得用户侧昵称
        Set<String> idSet = new HashSet<>();
        idSet.add(userId);
        String nickname = getBasicUserList(idSet).get(0).getNickname();
        //用户头像
        String face = getBasicUserList(idSet).get(0).getFace();

        // 3. 保存用户评论的信息到数据库
        commentPortalService.createComment(String.valueOf(commentReplyBO.getArticleId()),
                String.valueOf(commentReplyBO.getFatherId()),
                commentReplyBO.getContent(),
                userId,
                nickname,
                face);

        return GraceJSONResult.ok();
    }



    @Override
    public GraceJSONResult commentCounts(Long articleId) {

        Integer counts =
                getCountsFromRedis(REDIS_ARTICLE_COMMENT_COUNTS + ":" + String.valueOf(articleId));

        return GraceJSONResult.ok(counts);
    }

    @Override
    public GraceJSONResult list(String articleId,
                                Integer page,
                                Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryArticleComments(articleId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryWriterCommentsMng(String.valueOf(writerId), page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult delete(Long writerId, Long commentId) {
        commentPortalService.deleteComment(String.valueOf(writerId), String.valueOf(commentId));
        return GraceJSONResult.ok();
    }
}
