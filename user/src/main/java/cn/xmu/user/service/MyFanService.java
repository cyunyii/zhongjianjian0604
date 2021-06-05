package cn.xmu.user.service;

import cn.xmu.enums.Sex;
import cn.xmu.pojo.vo.RegionRatioVO;
import cn.xmu.utils.PagedGridResult;

import java.util.List;

public interface MyFanService {

    /**
     * 查询当前用户是否关注作家
     */
    public boolean isMeFollowThisWriter(String writerId, String fanId);


    /**
     * 关注成为粉丝
     */
    public void follow(String writerId, String fanId);

    /**
     * 粉丝取消关注
     */
    public void unfollow(String writerId, String fanId);

    /**
     * 查询我的粉丝数
     */
    public PagedGridResult queryMyFansList(String writerId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * 查询粉丝数 男女
     */
    public Integer queryFansCounts(String writerId, Sex sex);

    /**
     * 查询粉丝数 地区
     */
    public List<RegionRatioVO> queryRegionRatioCounts(String writerId);
}
