package cn.xmu.user.controller;

import cn.xmu.api.BaseController;
import cn.xmu.api.user.PassportControllerApi;
import cn.xmu.enums.UserStatus;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.bo.RegistLoginBO;
import cn.xmu.user.model.po.AppUserPo;
import cn.xmu.user.service.UserService;
import cn.xmu.utils.IPUtil;
import cn.xmu.utils.RedisOperator;
import cn.xmu.utils.extend.MailServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class PassportController extends BaseController  implements PassportControllerApi {
    final static Logger logger = LoggerFactory.getLogger(PassportController.class);

    @Autowired
    private MailServiceImpl mailService;

    @Autowired
    private RedisOperator redis;

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getCode(String email, HttpServletRequest request) {
        // 获得用户ip
        String userIp = IPUtil.getRequestIp(request);

        // 根据用户的ip进行限制，限制用户在60秒内只能获得一次验证码
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        logger.error(userIp);

        String random = (int)((Math.random() * 9 + 1) * 100000) + "";
        mailService.sendMail(email,"主题：这是验证码",random);

        // 把验证码存入redis，用于后续进行验证
        redis.set(MOBILE_SMSCODE + ":" + email, random, 30 * 60);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult doLogin(@Valid RegistLoginBO registLoginBO, BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        if(result.hasErrors()){
            Map<String, String> map = getErrors(result);
            return GraceJSONResult.errorMap(map);
        }

        String mail = registLoginBO.getMail();
        String smsCode = registLoginBO.getSmsCode();

        // 1. 校验验证码是否匹配
        String redisSMSCode = redis.get(MOBILE_SMSCODE + ":" + mail);
        if (StringUtils.isBlank(redisSMSCode) || !redisSMSCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        List<AppUserPo> appUserPos = userService.queryEmailIsExist(mail);
        AppUserPo appUserPo = null;
        if(appUserPos!=null && appUserPos.get(0).getActiveStatus()== UserStatus.FROZEN.type){
            // 如果用户不为空，并且状态为冻结，则直接抛出异常，禁止登录
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        }else if(appUserPos == null){
            appUserPo = userService.createUser(mail);
            if(appUserPo==null){
                return new GraceJSONResult(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
            }
        }else{
            appUserPo = appUserPos.get(0);
        }

        // 3. 保存用户分布式会话的相关操作
        int userActiveStatus = appUserPo.getActiveStatus();
        if (userActiveStatus != UserStatus.FROZEN.type) {
            // 保存token到redis
            String uToken = UUID.randomUUID().toString();
            redis.set(REDIS_USER_TOKEN + ":" + appUserPo.getId(), uToken);
//            redis.set(REDIS_USER_INFO + ":" + appUserPo.getId(), JsonUtils.objectToJson(appUserPo));

            // 保存用户id和token到cookie中
            setCookie(request, response, "utoken", uToken, COOKIE_MONTH);
            setCookie(request, response, "uid", String.valueOf(appUserPo.getId()), COOKIE_MONTH);
        }

        // 4. 用户登录或注册成功以后，需要删除redis中的短信验证码，验证码只能使用一次，用过后则作废
        redis.del(MOBILE_SMSCODE + ":" +  mail);

        return GraceJSONResult.ok(userActiveStatus);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }
}
