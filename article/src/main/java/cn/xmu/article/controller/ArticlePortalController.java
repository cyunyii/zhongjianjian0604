package cn.xmu.article.controller;

import cn.xmu.api.BaseController;
import cn.xmu.api.article.ArticlePortalControllerApi;
import cn.xmu.article.model.po.ArticlePo;
import cn.xmu.article.service.ArticlePortalService;
import cn.xmu.enums.JsonUtils;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.pojo.vo.AppUserVO;
import cn.xmu.pojo.vo.ArticleDetailVO;
import cn.xmu.pojo.vo.IndexArticleVO;
import cn.xmu.utils.IPUtil;
import cn.xmu.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {

    final static Logger logger = LoggerFactory.getLogger(ArticlePortalController.class);

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public GraceJSONResult list(String keyword,
                                Integer category,
                                Integer page,
                                Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult
                = articlePortalService.queryIndexArticleList(keyword,
                category,
                page,
                pageSize);

//        if(gridResult.getTotal()==0)
//           return GraceJSONResult.ok(gridResult);
//        else
        //远程服务调用
          gridResult = rebuildArticleGrid(gridResult);

        return GraceJSONResult.ok(gridResult);
    }

    private PagedGridResult rebuildArticleGrid(PagedGridResult gridResult) {
        // START

        List<ArticlePo> list = (List<ArticlePo>)gridResult.getRows();

        // 1. 构建发布者id列表
        Set<String> idSet = new HashSet<>();
        List<String> idList = new ArrayList<>();

        for (ArticlePo a : list) {

//            System.out.println(a.getPublishUserId());
            // 1.1 构建发布者的set
            idSet.add(a.getPublishUserId());
            // 1.2 构建文章id的list
            idList.add(REDIS_ARTICLE_READ_COUNTS + ":" + a.getId());
        }
        System.out.println(idSet.toString());

        // 发起redis的mget批量查询api，获得对应的值
        List<String> readCountsRedisList = redis.mget(idList);

        // 2. 发起远程调用（restTemplate），请求用户服务获得用户（idSet 发布者）的列表
        String userServerUrlExecute
                = "http://localhost:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        List<AppUserVO> publisherList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            publisherList = JsonUtils.jsonToList(userJson, AppUserVO.class);
        }


        // 3. 拼接两个list，重组文章列表
        List<IndexArticleVO> indexArticleList = new ArrayList<>();

        for (int i = 0 ; i < list.size() ; i ++) {
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            ArticlePo a = list.get(i);
            BeanUtils.copyProperties(a, indexArticleVO);

            // 3.1 从publisherList中获得发布者的基本信息
            AppUserVO publisher  = getUserIfPublisher(a.getPublishUserId(), publisherList);
            indexArticleVO.setPublisherVO(publisher);

            // 3.2 重新组装设置文章列表中的阅读量
            String redisCountsStr = readCountsRedisList.get(i);
            int readCounts = 0;
            if (StringUtils.isNotBlank(redisCountsStr)) {
                readCounts = Integer.valueOf(redisCountsStr);
            }
            indexArticleVO.setReadCounts(readCounts);

            indexArticleList.add(indexArticleVO);
        }


        gridResult.setRows(indexArticleList);
// END
        return gridResult;
    }

   private AppUserVO getUserIfPublisher(String publisherId,
                                         List<AppUserVO> publisherList) {
        for (AppUserVO user : publisherList) {
            if (String.valueOf(user.getId()).equalsIgnoreCase(publisherId)) {
                return user;
            }
        }
//单独write
//    // 注入服务发现，可以获得已经注册的服务相关信息
//    @Autowired
//    private DiscoveryClient discoveryClient;
//
//    @Autowired
//    private UserControllerApi userControllerApi;
//
//    // 发起远程调用，获得用户的基本信息
//    private List<AppUserVO> getPublisherList(Set idSet) {
//    }

    @Override
    public GraceJSONResult hotList() {
        return GraceJSONResult.ok(articlePortalService.queryHotList());
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {

        System.out.println("writerId=" + writerId);

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articlePortalService.queryArticleListOfWriter(writerId, page, pageSize);
        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {
        PagedGridResult gridResult = articlePortalService.queryGoodArticleListOfWriter(writerId);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult detail(String articleId) {
        ArticleDetailVO detailVO = articlePortalService.queryDetail(articleId);

        Set<String> idSet = new HashSet();
        idSet.add(detailVO.getPublishUserId());

        //  发起远程调用（restTemplate），请求用户服务获得用户（idSet 发布者）的列表
        String userServerUrlExecute
                = "http://localhost:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        List<AppUserVO> publisherList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            publisherList = JsonUtils.jsonToList(userJson, AppUserVO.class);
        }

        if (!publisherList.isEmpty()) {
            detailVO.setPublishUserName(publisherList.get(0).getNickname());
        }

        detailVO.setReadCounts(
                getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId));

        return GraceJSONResult.ok(detailVO);
    }

    @Override
    public Integer readCounts(String articleId) {
        return getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {

        //获得用户ip
        String userIp = IPUtil.getRequestIp(request);
        // 设置针对当前用户ip的永久存在的key，存入到redis，表示该ip的用户已经阅读过了，无法累加阅读量
        redis.setnx(REDIS_ALREADY_READ + ":" +  articleId + ":" + userIp, userIp);

        redis.increment(REDIS_ARTICLE_READ_COUNTS + ":" + articleId, 1);
        return GraceJSONResult.ok();
    }
}
