package cn.xmu.article.service;

import cn.xmu.article.model.po.ArticlePo;
import cn.xmu.pojo.vo.ArticleDetailVO;
import cn.xmu.utils.PagedGridResult;

import java.util.List;

public interface ArticlePortalService {

    /**
     * 首页查询文章列表
     */
    public PagedGridResult queryIndexArticleList(String keyword,
                                                 Integer category,
                                                 Integer page,
                                                 Integer pageSize);
    /**
     * 首页查询热闻列表
     */
    public List<ArticlePo> queryHotList();

    /**
     * 查询作家发布的所有文章列表
     */
    public PagedGridResult queryArticleListOfWriter(String writerId,
                                                    Integer page,
                                                    Integer pageSize);

    /**
     * 作家页面查询近期佳文
     */
    public PagedGridResult queryGoodArticleListOfWriter(String writerId);

//    /**
//     * 查询文章详情
//     */
    public ArticleDetailVO queryDetail(String articleId);
}

