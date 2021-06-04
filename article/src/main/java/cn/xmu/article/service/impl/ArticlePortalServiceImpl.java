package cn.xmu.article.service.impl;

import cn.xmu.api.BaseService;
import cn.xmu.article.mapper.ArticlePoMapper;
import cn.xmu.article.model.po.ArticlePo;
import cn.xmu.article.model.po.ArticlePoExample;
import cn.xmu.article.service.ArticlePortalService;
import cn.xmu.enums.ArticleReviewStatus;
import cn.xmu.enums.YesOrNo;
import cn.xmu.pojo.Article;
import cn.xmu.pojo.vo.ArticleDetailVO;
import cn.xmu.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class  ArticlePortalServiceImpl extends BaseService implements ArticlePortalService {

    @Autowired
    private ArticlePoMapper articlePoMapper;

    @Override
    public PagedGridResult queryIndexArticleList(String keyword,
                                                 Integer category,
                                                 Integer page,
                                                 Integer pageSize) {


        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();

        //按发布时间排序
        articlePoExample.setOrderByClause("'publishTime' DESC");

//        Example articleExample = new Example(Article.class);
//        articleExample.orderBy("publishTime").desc();
//        Example.Criteria criteria = articleExample.createCriteria();

        /**
         * 查询首页文章的自带隐性查询条件：
         * isAppoint=即使发布，表示文章已经直接发布的，或者定时任务到点发布的
         * isDelete=未删除，表示文章只能够显示未删除
         * articleStatus=审核通过，表示只有文章经过机审/人工审核之后才能展示
         */
        criteria.andIsAppointEqualTo(YesOrNo.NO.type);
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        criteria.andArticleStatusEqualTo(ArticleReviewStatus.SUCCESS.type);


        if (StringUtils.isNotBlank(keyword)) {
            criteria.andTitleLike(keyword);
            //criteria.andLike("title", "%" + keyword + "%");
        }
        if (category != null) {
            criteria.andCategoryIdEqualTo(category);
           // criteria.andEqualTo("categoryId", category);
        }

        PageHelper.startPage(page, pageSize);
        List<ArticlePo>articlePos = articlePoMapper.selectByExample(articlePoExample);
        return setterPagedGrid(articlePos, page);
    }



    @Override
    public List<ArticlePo> queryHotList() {

        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();
      //  criteria.andCategoryIdEqualTo(category);
        //按发布时间排序
        articlePoExample.setOrderByClause("'publishTime' DESC");

        criteria.andIsAppointEqualTo(YesOrNo.NO.type);
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        criteria.andArticleStatusEqualTo(ArticleReviewStatus.SUCCESS.type);

        PageHelper.startPage(1, 5);
        List<ArticlePo> list = articlePoMapper.selectByExample(articlePoExample);

//        Example articleExample = new Example(Article.class);
//        Example.Criteria criteria = setDefualArticleExample(articleExample);

    //    PageHelper.startPage(1, 5);
      //  List<Article> list  = articleMapper.selectByExample(articleExample);

        return list;
    }

    //默认的隐形条件
    private ArticlePoExample.Criteria setDefualArticleExample(ArticlePoExample articlePoExample) {

        articlePoExample.setOrderByClause("'publishTime' DESC");
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();

        /**
         * 查询首页文章的自带隐性查询条件：
         * isAppoint=即使发布，表示文章已经直接发布的，或者定时任务到点发布的
         * isDelete=未删除，表示文章只能够显示未删除
         * articleStatus=审核通过，表示只有文章经过机审/人工审核之后才能展示
         */
        criteria.andIsAppointEqualTo(YesOrNo.NO.type);
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        criteria.andArticleStatusEqualTo(ArticleReviewStatus.SUCCESS.type);

        //  articleExample.orderBy("publishTime").desc();
        //Example.Criteria criteria = articleExample.createCriteria();

//        /**
//         * 查询首页文章的自带隐性查询条件：
//         * isAppoint=即使发布，表示文章已经直接发布的，或者定时任务到点发布的
//         * isDelete=未删除，表示文章只能够显示未删除
//         * articleStatus=审核通过，表示只有文章经过机审/人工审核之后才能展示
//         */
//        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
//        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
//        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        return criteria;
    }

    @Override
    public PagedGridResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {


        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();

        criteria.andIsAppointEqualTo(YesOrNo.NO.type);
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        criteria.andArticleStatusEqualTo(ArticleReviewStatus.SUCCESS.type);

        criteria.andPublishUserIdEqualTo(writerId);

//        Example articleExample = new Example(Article.class);
//
//        Example.Criteria criteria = setDefualArticleExample(articleExample);
//        //作家的用户id
//        criteria.andEqualTo("publishUserId", writerId);

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);
        List<ArticlePo>list = articlePoMapper.selectByExample(articlePoExample);
    //        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryGoodArticleListOfWriter(String writerId) {

        ArticlePoExample articlePoExample = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = articlePoExample.createCriteria();

        criteria.andIsAppointEqualTo(YesOrNo.NO.type);
        criteria.andIsDeleteEqualTo(YesOrNo.NO.type);
        criteria.andArticleStatusEqualTo(ArticleReviewStatus.SUCCESS.type);

        criteria.andPublishUserIdEqualTo(writerId);
//
//        Example articleExample = new Example(Article.class);
//        articleExample.orderBy("publishTime").desc();
//
//        Example.Criteria criteria = setDefualArticleExample(articleExample);
//        criteria.andEqualTo("publishUserId", writerId);

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(1, 5);
        List<ArticlePo>list = articlePoMapper.selectByExample(articlePoExample);

        //  List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, 1);
    }

    @Override
    public ArticleDetailVO queryDetail(String articleId) {

        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(YesOrNo.NO.type);
        article.setIsDelete(YesOrNo.NO.type);
        article.setArticleStatus(ArticleReviewStatus.SUCCESS.type);

        ArticlePo result = articlePoMapper.selectByPrimaryKey(Long.parseLong(articleId));

        ArticleDetailVO detailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(result, detailVO);

        detailVO.setCover(result.getArticleCover());

        return detailVO;
    }
}
