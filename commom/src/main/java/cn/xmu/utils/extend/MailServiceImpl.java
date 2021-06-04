package cn.xmu.utils.extend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@EnableAsync(proxyTargetClass = true)
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    //邮件发件人
    @Value("${mail.fromMail.addr}")
    private String from;

    @Autowired
    TemplateEngine templateEngine;

    @Override
    @Async
    public void sendMail(String to, String subject, String verifyCode) {
        //创建邮件正文
        Context context = new Context();
        context.setVariable("verifyCode", verifyCode);
        context.setVariable("useremail", to);
        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process("emailTemplate", context);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(emailContent);
        try {
            mailSender.send(message);
            //logger.info("简单邮件已经发送。");
        } catch (Exception e) {
//            logger.error("发送简单邮件时发生异常！", e);
        }
    }

}