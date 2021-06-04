//package cn.xmu.api.user.fallbacks;
//
//import cn.xmu.api.user.UserControllerApi;
//import cn.xmu.grace.result.GraceJSONResult;
//import cn.xmu.grace.result.ResponseStatusEnum;
//import cn.xmu.pojo.bo.UpdateUserInfoBO;
//import cn.xmu.pojo.vo.AppUserVO;
//
//import feign.hystrix.FallbackFactory;
//import org.springframework.stereotype.Component;
//
//import javax.validation.Valid;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class UserControllerFactoryFallback
//        implements FallbackFactory<UserControllerApi> {
//
//    @Override
//    public UserControllerApi create(Throwable throwable) {
//        return new UserControllerApi() {
//            @Override
//            public GraceJSONResult getUserInfo(String userId) {
//                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
//            }
//
//            @Override
//            public GraceJSONResult getAccountInfo(String userId) {
//                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
//            }
//
//            @Override
//            public GraceJSONResult updateUserInfo(@Valid UpdateUserInfoBO updateUserInfoBO) {
//                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
//            }
//
//            @Override
//            public GraceJSONResult queryByIds(String userIds) {
//                System.out.println("进入客户端（服务调用者）的降级方法");
//                List<AppUserVO> publisherList = new ArrayList<>();
//                return GraceJSONResult.ok(publisherList);
//            }
//        };
//    }
//}
