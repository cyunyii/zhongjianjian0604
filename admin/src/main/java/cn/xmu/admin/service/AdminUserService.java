package cn.xmu.admin.service;


import cn.xmu.admin.model.po.AdminPo;
import cn.xmu.pojo.bo.NewAdminBO;
import cn.xmu.utils.PagedGridResult;

public interface AdminUserService {

    /**
     * 获得管理员的用户信息
     */
    public AdminPo queryAdminByUsername(String username);

    /**
     * 新增管理员
     */
    public void createAdminUser(NewAdminBO newAdminBO);

    /**
     * 分页查询admin列表
     */
    public PagedGridResult queryAdminList(Integer page, Integer pageSize);

}
