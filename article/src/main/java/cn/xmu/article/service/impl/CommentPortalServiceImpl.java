package cn.xmu.article.service.impl;

import cn.xmu.api.BaseService;
import cn.xmu.article.mapper.CommentPoMapper;
import cn.xmu.article.model.po.CommentPo;
import cn.xmu.article.model.po.CommentPoExample;
import cn.xmu.article.service.ArticlePortalService;
import cn.xmu.article.service.CommentPortalService;
import cn.xmu.pojo.vo.ArticleDetailVO;
import cn.xmu.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentPortalServiceImpl extends BaseService implements CommentPortalService {


    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private CommentPoMapper commentPoMapper;

//    @Autowired
//    private CommentsMapperCustom commentsMapperCustom;

    @Transactional
    @Override
    public void createComment(String articleId,
                              String fatherCommentId,
                              String content,
                              String userId,
                              String nickname,
                              String face) {

        ArticleDetailVO article
                 = articlePortalService.queryDetail(articleId);

        CommentPo comments = new CommentPo();

        comments.setWriterId(article.getPublishUserId());
        comments.setArticleTitle(article.getTitle());
        comments.setArticleCover(article.getCover());
        comments.setArticleId(articleId);

        comments.setFatherId(fatherCommentId);
        comments.setCommentUserId(userId);
        comments.setCommentUserNickname(nickname);

        comments.setCommentUserFace(face);
        LocalDateTime now=LocalDateTime.now();
        comments.setContent(content);
        comments.setCreateTime (now);

//        comments.setId(null);
        commentPoMapper.insert(comments);
//        comments.setId(null);


        // 评论数累加
        redis.increment(REDIS_ARTICLE_COMMENT_COUNTS + ":" + articleId, 1);
    }

    @Override
    public PagedGridResult queryArticleComments(String articleId,
                                                Integer page,
                                                Integer pageSize) {

        Map<String,  Object> map = new HashMap<>();
        map.put("articleId", articleId);

        CommentPoExample commentPoExample=new CommentPoExample();
        CommentPoExample.Criteria criteria = commentPoExample.createCriteria();
        criteria.andArticleIdEqualTo(articleId);

        PageHelper.startPage(page, pageSize);
        List<CommentPo> list = commentPoMapper.selectByExample(commentPoExample);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize) {

        CommentPoExample commentPoExample = new CommentPoExample();
        CommentPoExample.Criteria criteria = commentPoExample.createCriteria();
        criteria.andWriterIdEqualTo(writerId);

        PageHelper.startPage(page, pageSize);
        List<CommentPo> list = commentPoMapper.selectByExample(commentPoExample);
        return setterPagedGrid(list, page);
    }

    @Override
    public void deleteComment(String writerId, String commentId) {

         commentPoMapper.deleteByPrimaryKey(Long.parseLong(commentId));
    }
}
