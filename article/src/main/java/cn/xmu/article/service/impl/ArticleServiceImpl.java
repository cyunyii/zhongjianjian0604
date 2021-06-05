package cn.xmu.article.service.impl;

import cn.xmu.api.BaseService;
import cn.xmu.article.mapper.ArticlePoMapper;
import cn.xmu.article.model.po.ArticlePo;
import cn.xmu.article.model.po.ArticlePoExample;
import cn.xmu.article.service.ArticleService;
import cn.xmu.enums.ArticleAppointType;
import cn.xmu.enums.ArticleReviewStatus;
import cn.xmu.enums.YesOrNo;
import cn.xmu.exception.GraceException;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.Category;
import cn.xmu.pojo.bo.NewArticleBO;
import cn.xmu.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private ArticlePoMapper articlePoMapper;

//    @Autowired
//    private Sid sid;

    public static LocalDateTime date2LocalDateTime(Date date) {
        if(null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /*
    insert的时候出错
     */
    @Transactional
    @Override
    public void createArticle(NewArticleBO newArticleBO, Category category) {

//        String articleId = sid.nextShort();

        ArticlePo article = new ArticlePo();
        BeanUtils.copyProperties(newArticleBO, article);

//        article.setId(Long.parseLong(articleId));
        article.setCategoryId(category.getId());
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setCommentCounts(0);
        article.setReadCounts(0);



        article.setIsDelete(YesOrNo.NO.type);
        article.setCreateTime(date2LocalDateTime(new Date()));
        article.setUpdateTime(date2LocalDateTime(new Date()));

        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {
            article.setPublishTime(date2LocalDateTime(newArticleBO.getPublishTime()));
        } else if (article.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
            article.setPublishTime(date2LocalDateTime(new Date()));
        }

        int res = articlePoMapper.insert(article);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }

        /**
         * FIXME: 我们只检测正常的词汇，非正常词汇大家课后去检测
         */
//        // 通过阿里智能AI实现对文章文本的自动检测（自动审核）
////        String reviewTextResult = aliTextReviewUtils.reviewTextContent(newArticleBO.getContent());
//        String reviewTextResult = ArticleReviewLevel.REVIEW.type;
//
//        if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.PASS.type)) {
//            // 修改当前的文章，状态标记为审核通过
//            this.updateArticleStatus(articleId, ArticleReviewStatus.SUCCESS.type);
//        } else if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.REVIEW.type)) {
//            // 修改当前的文章，状态标记为需要人工审核
//            this.updateArticleStatus(articleId, ArticleReviewStatus.WAITING_MANUAL.type);
//        } else if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.BLOCK.type)) {
//            // 修改当前的文章，状态标记为审核未通过
//            this.updateArticleStatus(articleId, ArticleReviewStatus.FAILED.type);
//        }
//
    }

    @Override
    public PagedGridResult queryMyArticleList(String userId,
                                              String keyword,
                                              Integer status,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate,
                                              Integer page,
                                              Integer pageSize) {

//        Example example = new Example(Article.class);
//        example.orderBy("createTime").desc();
//        Example.Criteria criteria = example.createCriteria();
//
//        criteria.andEqualTo("publishUserId", userId);

        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria1=articlePoExample.createCriteria();
        criteria1.andPublishUserIdEqualTo(userId);



        if (StringUtils.isNotBlank(keyword)) {
//            criteria.andLike("title", "%" + keyword + "%");
            criteria1.andTitleLike(keyword);
        }

        /*
        valid是几？
         */
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
//            criteria.andEqualTo("articleStatus", status);
            criteria1.andArticleStatusEqualTo(status);
        }

        if (status != null && status == 12) {
            criteria1.andArticleStatusEqualTo(ArticleReviewStatus.REVIEWING.type);
//                    .orArticleStatusEqualTo(ArticleReviewStatus.WAITING_MANUAL.type);

        }

        criteria1.andIsDeleteEqualTo( YesOrNo.NO.type);

        if (startDate != null) {
            criteria1.andPublishTimeGreaterThanOrEqualTo(startDate);
        }
        if (endDate != null) {
            criteria1.andPublishTimeLessThanOrEqualTo(endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<ArticlePo> list = articlePoMapper.selectByExample(articlePoExample);
        return setterPagedGrid(list, page);
    }



    @Transactional
    @Override
    public void deleteArticle(String userId, String articleId) {
        articlePoMapper.deleteByPrimaryKey(Long.parseLong(articleId));

    }

    @Transactional
    @Override
    public void updateArticleStatus(String articleId, Integer pendingStatus) {
        ArticlePo articlePo=new ArticlePo();
        articlePo.setId(Long.parseLong(articleId));
        articlePo.setArticleStatus(pendingStatus);
        System.out.println(articleId);
        System.out.println(articlePo.getId());
        int result = articlePoMapper.updateByPrimaryKeySelective(articlePo);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_WITHDRAW_ERROR);
        }
    }
    @Transactional
    @Override
    public void withdrawArticle(String userId, String articleId) {

//        ArticlePoExample articlePoExample = makeExampleCriteria(userId, articleId);
//
//        Article pending = new Article();
//        pending.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);
        ArticlePo articlePo=new ArticlePo();
        articlePo.setId(Long.parseLong(articleId));
        articlePo.setPublishUserId(userId);
        articlePo.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);
        System.out.println(articleId);
        System.out.println(articlePo.getId());
        int result = articlePoMapper.updateByPrimaryKeySelective(articlePo);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_WITHDRAW_ERROR);
        }
    }

    private ArticlePoExample makeExampleCriteria(String userId, String articleId) {
        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();
        criteria.andPublishUserIdEqualTo( userId);
        criteria.andIdEqualTo(Long.parseLong(articleId));
        return articlePoExample;
    }

    @Override
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize) {

        // 审核中是机审和人审核的两个状态，所以需要单独判断??
        ArticlePoExample articlePoExample=new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();
        if(status != null && status == 12)
            criteria.andArticleStatusBetween(ArticleReviewStatus.REVIEWING.type,ArticleReviewStatus.WAITING_MANUAL.type);
        else if(status != null)
            criteria.andArticleStatusEqualTo(status);
        else{}
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        List<Integer> type=new ArrayList<Integer>();
        type.add(ArticleReviewStatus.REVIEWING.type);   //索引为0  //.add(e)
        type.add(ArticleReviewStatus.WAITING_MANUAL.type);    //索引为1
        if (status != null && status == 12) {
            criteria.andArticleStatusIn(type);
        }


        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);
        List<ArticlePo> list = articlePoMapper.selectByExample(articlePoExample);
        return setterPagedGrid(list, page);
    }

}
