package cn.xmu.user.service.impl;

import cn.xmu.api.BaseService;
import cn.xmu.enums.Sex;
import cn.xmu.pojo.vo.RegionRatioVO;
import cn.xmu.user.mapper.FansPoMapper;
import cn.xmu.user.model.po.AppUserPo;
import cn.xmu.user.model.po.FansPo;
import cn.xmu.user.model.po.FansPoExample;
import cn.xmu.user.service.MyFanService;
import cn.xmu.user.service.UserService;
import cn.xmu.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

//无basesevice怎么改????
//BaseService 有redis和pagedGridResult的定义
//po bo vo mo mapper pomapper ???

@Service
public class MyFanServiceImpl extends BaseService implements MyFanService {

    @Autowired
    private FansPoMapper fansMapper;

    @Autowired
    private UserService userService;

//    @Autowired
//    private Sid sid;



    @Override
    public boolean isMeFollowThisWriter(String writerId, String fanId) {
//
//        Fans fan = new Fans();
//        fan.setFanId(fanId);
//        fan.setWriterId(writerId);

        FansPo fansPo = new FansPo();
        fansPo.setFanId(fanId);
        fansPo.setWriterId(writerId);

        FansPoExample fansPoExample = new FansPoExample();
        FansPoExample.Criteria criteria = fansPoExample.createCriteria();
        criteria.andWriterIdEqualTo(writerId);
        criteria.andFanIdEqualTo(fanId);

        List<FansPo> fansPos = fansMapper.selectByExample(fansPoExample);

        if (null == fansPos || fansPos.isEmpty() ) {
            return false;
        } else
            return true;
//        //pomapper怎么计算数量???没有自动生成的函数
//        int count = fansMapper.selectCount(fan);

//        return count > 0 ? true : false;
    }

    //用户的某些信息为空时的判空
    @Transactional
    @Override
    public void follow(String writerId, String fanId) {
        // 获得粉丝用户的信息
        AppUserPo appUserPo = userService.getUser(Long.parseLong(fanId));


    //    String fanPkId = sid.nextShort();

        FansPo fansPo = new FansPo();
     //   fansPo.setId( Long.parseLong(fanPkId));

       // fansPo.setId(12L);
        fansPo.setFanId(fanId);

        fansPo.setProvince(appUserPo.getProvince());
        fansPo.setWriterId(writerId);
        fansPo.setFanNickname(appUserPo.getNickname());
        fansPo.setFace(appUserPo.getFace());
        fansPo.setSex(appUserPo.getSex());

        fansMapper.insertSelective(fansPo);
       // fansMapper.insert(fansPo);

//        Fans fans = new Fans();
//        fans.setId(fanPkId);
//        fans.setFanId(fanId);
//        fans.setWriterId(writerId);
//
//        fans.setFace(fanInfo.getFace());
//        fans.setFanNickname(fanInfo.getNickname());
//        fans.setSex(fanInfo.getSex());
//        fans.setProvince(fanInfo.getProvince());

//        fansMapper.insert(fans);

        // redis 作家粉丝数累加
        redis.increment(REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        // redis 当前用户的（我的）关注数累加
        redis.increment(REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);
    }

    @Transactional
    @Override
    public void unfollow(String writerId, String fanId) {

        //取关的话就是解除含有fanid和writerid关系的数据库数据
        //但该条数据的主键不是他们两个
        //所以如果要根据主键删除数据的话，先根据已有的两个信息查找出主键
        Long id = null;

        FansPoExample fansPoExample = new FansPoExample();
        FansPoExample.Criteria criteria = fansPoExample.createCriteria();
        criteria.andWriterIdEqualTo(writerId);
        criteria.andFanIdEqualTo(fanId);

        List<FansPo> fansPos = fansMapper.selectByExample(fansPoExample);

        id = fansPos.get(0).getId();

        fansMapper.deleteByPrimaryKey(id);

//        Fans fans = new Fans();
//        fans.setWriterId(writerId);
//        fans.setFanId(fanId);
//
//        fansMapper.delete(fans);

        // redis 作家粉丝数累减
        redis.decrement(REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        // redis 当前用户的（我的）关注数累减
        redis.decrement(REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);
    }

    @Override
    public PagedGridResult queryMyFansList(String writerId,
                                           Integer page,
                                           Integer pageSize) {

        FansPoExample fansPoExample = new FansPoExample();
        FansPoExample.Criteria criteria = fansPoExample.createCriteria();
        criteria.andWriterIdEqualTo(writerId);

        PageHelper.startPage(page, pageSize);
        List<FansPo> fansPos = fansMapper.selectByExample(fansPoExample);

//        List<Fans> fans = new ArrayList<>(fansPos.size());
//
//        for (FansPo po : fansPos) {
//            Fans fans1 = new Fans(po);
//            fans.add(fans1);
//        }

//        Fans fans = new Fans();
//        fans.setWriterId(writerId);
       // List<Fans> list = fansMapper.select(fans);

        return setterPagedGrid(fansPos, page);
    }

    @Override
    public Integer queryFansCounts(String writerId, Sex sex) {

        FansPoExample fansPoExample = new FansPoExample();
        FansPoExample.Criteria criteria = fansPoExample.createCriteria();
        criteria.andWriterIdEqualTo(writerId);
        criteria.andSexEqualTo(sex.type);

        List<FansPo> fansPos = fansMapper.selectByExample(fansPoExample);
        Integer count = fansPos.size();

//        Fans fans = new Fans();
//        fans.setWriterId(writerId);
//        fans.setSex(sex.type);
//        //数量怎么计算
//        Integer count = fansMapper.selectCount(fans);

        return count;
    }

    public static final String[] regions = {"北京", "天津", "上海", "重庆",
            "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆",
            "香港", "澳门"};

    @Override
    public List<RegionRatioVO> queryRegionRatioCounts(String writerId) {



        List<RegionRatioVO> list = new ArrayList<>();

        for (String r : regions) {

            FansPoExample fansPoExample = new FansPoExample();
            FansPoExample.Criteria criteria = fansPoExample.createCriteria();
            criteria.andWriterIdEqualTo(writerId);

            criteria.andProvinceEqualTo(r);
            List<FansPo> fansPos = fansMapper.selectByExample(fansPoExample);
            Integer count = fansPos.size();

            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(r);
            regionRatioVO.setValue(count);

            list.add(regionRatioVO);
        }

        return list;
    }
}
