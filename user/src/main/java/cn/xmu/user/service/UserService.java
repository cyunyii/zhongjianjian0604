package cn.xmu.user.service;

import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.pojo.bo.UpdateUserInfoBO;
import cn.xmu.pojo.vo.PublisherVO;
import cn.xmu.user.model.po.AppUserPo;

import java.util.List;

public interface UserService {
    /**
     * 判断用户是否存在，如果存在返回user信息
     */
    public List<AppUserPo> queryEmailIsExist(String mail);

    /**
     * 创建用户，新增用户记录到数据库
     */
    public AppUserPo createUser(String mail);

    /**
     * 根据用户主键id查询用户信息
     */
    public AppUserPo getUser(Long userId);

    /**
     * 用户修改信息，完善资料，并且激活
     */
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO);

    /**
     * 根据用户id查询用户
     */
    public List<PublisherVO> getUserList(List<String> userIdList);
}
