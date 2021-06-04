package cn.xmu.api.admin;

import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.pojo.bo.AdminLoginBO;
import cn.xmu.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "管理员admin维护", tags = {"管理员admin维护的controller"})
@RequestMapping("adminMng")
public interface AdminMngControllerApi {

    @ApiOperation(value = "管理员登录", notes = "管理员登录", httpMethod = "POST")
    @PostMapping("/adminLogin")
    public GraceJSONResult adminLogin(@RequestBody  @Valid AdminLoginBO adminLoginBO, BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response);
    //
    @ApiOperation(value = "查询admin用户名是否存在", notes = "查询admin用户名是否存在", httpMethod = "POST")
    @PostMapping("/adminIsExist")
    public GraceJSONResult adminIsExist(@RequestParam String username);
    //
    @ApiOperation(value = "创建admin", notes = "创建admin", httpMethod = "POST")
    @PostMapping("/addNewAdmin")
    public GraceJSONResult addNewAdmin(@RequestBody @Valid NewAdminBO newAdminBO,BindingResult result,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @ApiOperation(value = "查询admin列表", notes = "查询admin列表", httpMethod = "POST")
    @PostMapping("/getAdminList")
    public GraceJSONResult getAdminList(
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页查询每一页显示的条数", required = false)
            @RequestParam Integer pageSize);

    @ApiOperation(value = "admin退出登录", notes = "admin退出登录", httpMethod = "POST")
    @PostMapping("/adminLogout")
    public GraceJSONResult adminLogout(@RequestParam String adminId,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @ApiOperation(value = "admin管理员的人脸登录", notes = "admin管理员的人脸登录", httpMethod = "POST")
    @PostMapping("/adminFaceLogin")
    public GraceJSONResult adminFaceLogin(@RequestBody AdminLoginBO adminLoginBO,
                                          HttpServletRequest request,
                                          HttpServletResponse response);
}