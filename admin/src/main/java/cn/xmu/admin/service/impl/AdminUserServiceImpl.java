package cn.xmu.admin.service.impl;

import cn.xmu.admin.mapper.AdminPoMapper;
import cn.xmu.admin.model.po.AdminPo;
import cn.xmu.admin.model.po.AdminPoExample;
import cn.xmu.admin.service.AdminUserService;
import cn.xmu.exception.GraceException;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.bo.NewAdminBO;
import cn.xmu.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    public AdminPoMapper adminUserMapper;

    @Autowired
    private Sid sid;

    @Override
    public AdminPo queryAdminByUsername(String username) {

        AdminPoExample adminPoExample = new AdminPoExample();
        AdminPoExample.Criteria criteria = adminPoExample.createCriteria();


        criteria.andAdminNameEqualTo(username);

        List<AdminPo> admin = adminUserMapper.selectByExample(adminPoExample);
        if(admin.size()==0){
            return null;
        }
        return admin.get(0);
    }

    @Transactional
    @Override
    public void createAdminUser(NewAdminBO newAdminBO) {

        //获得主键
//        String adminId = sid.nextShort();

        AdminPo adminUser = new AdminPo();
//        adminUser.setId(Long.valueOf(adminId));
        adminUser.setUsername(newAdminBO.getUsername());
        adminUser.setAdminName(newAdminBO.getAdminName());

        // 如果密码不为空，则需要加密密码，存入数据库
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            String pwd = BCrypt.hashpw(newAdminBO.getPassword(), BCrypt.gensalt());
            adminUser.setPassword(pwd);
        }

        // 如果人脸上传，保存faceId至数据库
        if (StringUtils.isNotBlank(newAdminBO.getFaceId())) {
            adminUser.setFaceId(newAdminBO.getFaceId());
        }

        adminUser.setCreatedTime(LocalDateTime.now());
        adminUser.setUpdatedTime(LocalDateTime.now());


        int result = adminUserMapper.insert(adminUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }
    }

    @Override
    public PagedGridResult queryAdminList(Integer page, Integer pageSize) {
        AdminPoExample adminExample = new AdminPoExample();
        adminExample.setOrderByClause("created_time DESC");


        PageHelper.startPage(page, pageSize);
        List<AdminPo> adminUserList =
                adminUserMapper.selectByExample(adminExample);


        return setterPagedGrid(adminUserList, page);
    }

    private PagedGridResult setterPagedGrid(List<?> adminUserList,
                                            Integer page) {
        PageInfo<?> pageList = new PageInfo<>(adminUserList);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(adminUserList);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getPages());
        gridResult.setTotal(pageList.getTotal());
        return gridResult;
    }
}
