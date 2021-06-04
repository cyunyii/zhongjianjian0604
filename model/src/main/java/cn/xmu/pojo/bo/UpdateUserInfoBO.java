package cn.xmu.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class UpdateUserInfoBO {
    @NotNull
    private Long id;

    @NotBlank(message = "用户昵称不能为空")
    @Length(max = 12, message = "用户昵称不能超过12位")
    private String nickname;

    @NotBlank(message = "用户头像不能为空")
    private String face;

    @NotBlank(message = "真实姓名不能为空")
    private String realname;

    @NotBlank(message = "电话号不能为空")
    private String mobile;

    @NotNull(message = "请选择一个性别")
    @Min(value = 0, message = "性别选择不正确")
    @Max(value = 1, message = "性别选择不正确")
    private Integer sex;

    @NotNull(message = "请选择生日日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") // 解决前端日期字符串传到后端后，转换为Date类型
    private Date birthday;

    @NotBlank(message = "请选择所在城市")
    private String province;

    @NotBlank(message = "请选择所在城市")
    private String city;

    @NotBlank(message = "请选择所在城市")
    private String district;

//    private String email;
}
