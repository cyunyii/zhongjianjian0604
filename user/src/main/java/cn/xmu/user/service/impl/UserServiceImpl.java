package cn.xmu.user.service.impl;

import cn.xmu.enums.UserStatus;
import cn.xmu.exception.GraceException;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.bo.UpdateUserInfoBO;
import cn.xmu.pojo.vo.PublisherVO;
import cn.xmu.user.controller.PassportController;
import cn.xmu.user.mapper.AppUserPoMapper;
import cn.xmu.user.model.po.AppUserPo;
import cn.xmu.user.model.po.AppUserPoExample;
import cn.xmu.user.service.UserService;
import cn.xmu.utils.DesensitizationUtil;
import cn.xmu.utils.JsonUtils;
import cn.xmu.utils.KeyGenerateUtil;
import cn.xmu.utils.RedisOperator;
import com.google.gson.internal.$Gson$Preconditions;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;



@Service
public class UserServiceImpl implements UserService {
    final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private AppUserPoMapper appUserPoMapper;

    @Autowired
    public RedisOperator redis;
    public static final String REDIS_USER_INFO = "redis_user_info";

    private static final String USER_FACE0 = "http://znbwc.cn:8000/picture/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";

    @Override
    public List<AppUserPo> queryEmailIsExist(String email) {
        AppUserPoExample example = new AppUserPoExample();
        AppUserPoExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);
        List<AppUserPo> appUserPos = null;
        try {
            appUserPos = appUserPoMapper.selectByExample(example);

            logger.error("appUserPos: "+appUserPos.size());
            if(appUserPos.isEmpty()) appUserPos = null;
        } catch (DataAccessException e) {
            StringBuilder message = new StringBuilder().append("getUserByName: ").append(e.getMessage());
            logger.error(message.toString());
        }
        return appUserPos;
    }

    @Override
    @Transactional
    public AppUserPo createUser(String mail) {
        AppUserPo appUserPo = new AppUserPo();

        appUserPo.setEmail(mail);
        appUserPo.setFace(USER_FACE0);

        appUserPo.setActiveStatus(UserStatus.INACTIVE.type);
        appUserPo.setCreatedTime(LocalDateTime.now());

        String nikename = DesensitizationUtil.commonDisplay(mail);
        int end = Math.min(16, nikename.length());
        nikename = nikename.substring(0, end);
        appUserPo.setNickname(nikename);

        appUserPo.setUpdatedTime(LocalDateTime.now());

        appUserPo.setTotalIncome(0);

        GraceJSONResult graceJSONResult = null;
        try {
            appUserPoMapper.insertSelective(appUserPo);
            graceJSONResult = new GraceJSONResult();
            graceJSONResult.setData(appUserPo);
        }catch (Exception e){
            appUserPo = null;
            logger.error(e.toString());
        }
        return appUserPo;
    }

    @Override
    public AppUserPo getUser(Long userId) {
        AppUserPo appUserPo = appUserPoMapper.selectByPrimaryKey(userId);
        return appUserPo;
    }

    @Override
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
        Long userId = updateUserInfoBO.getId();
        // 保证双写一致，先删除redis中的数据，后更新数据库
        redis.del(REDIS_USER_INFO + ":" + userId);

        AppUserPo userInfo = new AppUserPo();
        BeanUtils.copyProperties(updateUserInfoBO, userInfo);

        userInfo.setUpdatedTime(LocalDateTime.now());
        userInfo.setActiveStatus(UserStatus.ACTIVE.type);

        int result = appUserPoMapper.updateByPrimaryKeySelective(userInfo);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        // 再次查询用户的最新信息，放入redis中
        AppUserPo user = getUser(userId);
        redis.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user));

        // 缓存双删策略
        try {
            Thread.sleep(100);
            redis.del(REDIS_USER_INFO + ":" + userId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PublisherVO> getUserList(List<String> userIdList) {
        return null;
    }
}
