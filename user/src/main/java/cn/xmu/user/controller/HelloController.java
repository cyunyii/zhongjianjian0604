package cn.xmu.user.controller;

import cn.xmu.api.BaseController;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.user.service.UserService;
import cn.xmu.utils.KeyGenerateUtil;
import cn.xmu.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.xmu.utils.extend.MailServiceImpl;

@RestController
public class HelloController  {

    @GetMapping("/hello")
    public Object hello(){

        return KeyGenerateUtil.get21Num();
    }

    @Autowired
    private MailServiceImpl mailService;

    @GetMapping("/testMail")
    public Object testMail(){
        mailService.sendMail("chenw5150@163.com","主题：这是模板邮件","123456");
        return "成功";
    }

    @Autowired
    private RedisOperator redis;

    @GetMapping("/redis")
    public Object redis() {
        redis.set("age", "18");
        return GraceJSONResult.ok(redis.get("age"));
    }

    @Autowired
    private UserService userService;

    @GetMapping("/testService")
    public Object testService(){
        String mail ="chenw5150@163.com";
        userService.queryEmailIsExist(mail);
        return "Ok";
    }

}
