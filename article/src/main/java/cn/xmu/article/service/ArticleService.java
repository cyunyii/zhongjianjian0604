package cn.xmu.article.service;


import cn.xmu.pojo.Category;
import cn.xmu.pojo.bo.NewArticleBO;
import cn.xmu.utils.PagedGridResult;

import java.time.LocalDateTime;

public interface ArticleService {

    /**
     * 发布文章
     */
    public void createArticle(NewArticleBO newArticleBO, Category category);

//    /**
//     * 更新定时发布为即时发布
//     */
//    public void updateAppointToPublish();
//
//    /**
//     * 用户中心 - 查询我的文章列表
//     */
    public PagedGridResult queryMyArticleList(String userId,
                                              String keyword,
                                              Integer status,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate,
                                              Integer page,
                                              Integer pageSize);

//    /**
//     * 更改文章的状态
//     */
    public void updateArticleStatus(String articleId, Integer pendingStatus);
//
    /**
     * 管理员查询文章列表
     */
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize);

//    /**
//     * 删除文章
//     */
    public void deleteArticle(String userId, String articleId);

//    /**
//     * 撤回文章
//     */
    public void withdrawArticle(String userId, String articleId);
}
