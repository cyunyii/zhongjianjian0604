package cn.xmu.user.service.impl;

import cn.xmu.api.BaseService;
import cn.xmu.user.model.po.AppUserPo;
import cn.xmu.user.model.po.AppUserPoExample;
import com.github.pagehelper.PageHelper;
import cn.xmu.enums.UserStatus;
import cn.xmu.user.mapper.AppUserPoMapper;
import cn.xmu.user.service.AppUserMngService;
import cn.xmu.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class AppUserMngServiceImpl extends BaseService implements AppUserMngService {

    @Autowired
    public AppUserPoMapper appUserMapper;

    @Override
    public PagedGridResult queryAllUserList(String nickname,
                                            Integer status,
                                            Date startDate,
                                            Date endDate,
                                            Integer page,
                                            Integer pageSize) {

        AppUserPoExample example = new AppUserPoExample();
        example.setOrderByClause("created_time desc");
        AppUserPoExample.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(nickname)) {
            criteria.andNicknameLike("%" + nickname + "%");
        }

        if (UserStatus.isUserStatusValid(status)) {
            criteria.andActiveStatusEqualTo(status);
        }

        if (startDate != null) {
            criteria.andCreatedTimeGreaterThanOrEqualTo(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (endDate != null) {
            criteria.andCreatedTimeLessThanOrEqualTo(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        PageHelper.startPage(page, pageSize);
        List<AppUserPo> list = appUserMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void freezeUserOrNot(Long userId, Integer doStatus) {
        AppUserPo user = new AppUserPo();
        user.setId(userId);
        user.setActiveStatus(doStatus);
        appUserMapper.updateByPrimaryKeySelective(user);
    }
}
