package cn.xmu.pojo.bo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RegistLoginBO {
    @Email
    private String mail;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

}
