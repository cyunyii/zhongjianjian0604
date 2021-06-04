package cn.xmu.utils.extend;

public interface MailService {
    /**
     * 发送邮件
     * @param to 邮件收件人
     * @param subject 邮件主题
     * @param verifyCode 邮件验证码
     */
    public void sendMail(String to, String subject, String verifyCode);
}


