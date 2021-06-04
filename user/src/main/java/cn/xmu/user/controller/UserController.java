package cn.xmu.user.controller;


import cn.xmu.api.BaseController;
import cn.xmu.api.user.UserControllerApi;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.bo.UpdateUserInfoBO;
import cn.xmu.pojo.vo.AppUserVO;
import cn.xmu.pojo.vo.UserAccountInfoVO;
import cn.xmu.user.model.po.AppUserPo;
import cn.xmu.user.service.UserService;
import cn.xmu.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserController extends BaseController implements UserControllerApi {

    final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;


    @Override
    public GraceJSONResult getUserInfo(Long userId) {
        // 0. 判断参数不能为空
        if (userId==null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. 根据userId查询用户的信息
        AppUserPo user = getUser(userId);

        // 2. 返回用户信息
        AppUserVO userVO = new AppUserVO();
        BeanUtils.copyProperties(user, userVO);

        // 3. 查询redis中用户的关注数和粉丝数，放入userVO到前端渲染
        userVO.setMyFansCounts(getCountsFromRedis(REDIS_WRITER_FANS_COUNTS + ":" + userId));
        userVO.setMyFollowCounts(getCountsFromRedis(REDIS_MY_FOLLOW_COUNTS + ":" + userId));

        return GraceJSONResult.ok(userVO);
    }

    @Override
    public GraceJSONResult getAccountInfo(Long userId) {
        // 0. 判断参数不能为空
//        if (result.hasErrors()) {
//            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
//        }
        if (userId==null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }
        // 1. 根据userId查询用户的信息
        AppUserPo user = getUser(userId);

        // 2. 返回用户信息
        UserAccountInfoVO accountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(user, accountInfoVO);

        return GraceJSONResult.ok(accountInfoVO);
    }

//    https://blog.csdn.net/weixin_46080554/article/details/108346470 解决时间序列化反序列化错误
    private AppUserPo getUser(Long userId) {
        // 查询判断redis中是否包含用户信息，如果包含，则查询后直接返回，就不去查询数据库了
        String userJson = redis.get(REDIS_USER_INFO + ":" + userId);
        AppUserPo user = null;
        if (StringUtils.isNotBlank(userJson)) {
            user = JsonUtils.jsonToPojo(userJson, AppUserPo.class);
        } else {
            user = userService.getUser(userId);
            // 由于用户信息不怎么会变动，对于一些千万级别的网站来说，这类信息不会直接去查询数据库
            // 那么完全可以依靠redis，直接把查询后的数据存入到redis中
            redis.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user));
        }
        return user;
    }

    @Override
    public GraceJSONResult updateUserInfo(@Valid UpdateUserInfoBO updateUserInfoBO, BindingResult result) {
        // 0. 校验BO
        if (result.hasErrors()) {
            Map<String, String> map = getErrors(result);
            return GraceJSONResult.errorMap(map);
        }

        // 1. 执行更新操作
        userService.updateUserInfo(updateUserInfoBO);
        return GraceJSONResult.ok();
    }


    @Value("${server.port}")
    private String myPort;

    //HystrixCommand？？的应用+++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //@HystrixCommand//(fallbackMethod = "queryByIdsFallback")
    @Override
    public GraceJSONResult queryByIds(String userIds) {

        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        List<AppUserVO> publisherList = new ArrayList<>();
        List<String> userIdList = cn.xmu.enums.JsonUtils.jsonToList(userIds, String.class);


        for (String userId : userIdList) {
            // 获得用户基本信息
            AppUserVO userVO = getBasicUserInfo(userId);
            // 添加到publisherList
            publisherList.add(userVO);
        }

        return GraceJSONResult.ok(publisherList);
    }

//    public GraceJSONResult queryByIdsFallback(String userIds) {
//
//        System.out.println("进入降级方法：queryByIdsFallback");
//
//        List<AppUserVO> publisherList = new ArrayList<>();
//        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);
//        for (String userId : userIdList) {
//            // 手动构建空对象，详情页所展示的用户信息可有可无
//            AppUserVO userVO = new AppUserVO();
//            publisherList.add(userVO);
//        }
//        return GraceJSONResult.ok(publisherList);
//    }


    private AppUserPo getUsers(Long userId) {
        AppUserPo user = null;

            user = userService.getUser(userId);

        return user;
    }

    private AppUserVO getBasicUserInfo(String userId) {
        // 1. 根据userId查询用户的信息
        AppUserPo user = getUsers(Long.valueOf(userId));

        // 2. 返回用户信息
        AppUserVO userVO = new AppUserVO();
//        BeanUtils.copyProperties(user, userVO);

        userVO.setId(String.valueOf(user.getId()));
        userVO.setActiveStatus(user.getActiveStatus());
        userVO.setFace(user.getFace());
        userVO.setNickname(user.getNickname());
        return userVO;
    }

}

