package cn.xmu.api.article;

import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.pojo.bo.NewArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Api(value = "门户端文章业务的controller", tags = {"门户端文章业务的controller"})
@RequestMapping("portal/article")
public interface ArticleControllerApi {


    public default LocalDateTime string2LDT(String string){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime LocalTime = LocalDateTime.parse(string,df);
        return LocalTime;

    }

    @PostMapping("createArticle")
    @ApiOperation(value = "用户发文", notes = "用户发文", httpMethod = "POST")
    public GraceJSONResult createArticle(@RequestBody @Valid NewArticleBO newArticleBO,
                                         BindingResult result);

    /*
    查不出东西？ keywordlike有问题？
     */
    @PostMapping("queryMyList")
    @DateTimeFormat
    @ApiOperation(value = "查询用户的所有文章列表", notes = "查询用户的所有文章列表", httpMethod = "POST")
    public GraceJSONResult queryMyList(@RequestParam(value="userId") String userId,
                                       @RequestParam String keyword,
                                       @RequestParam Integer status,
                                       @RequestParam (value="endDate",required = false)LocalDateTime startDate,
                                       @RequestParam (value="endDate",required = false)LocalDateTime endDate,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize);

    @PostMapping("queryAllList")
    @ApiOperation(value = "管理员查询用户的所有文章列表", notes = "管理员查询用户的所有文章列表", httpMethod = "POST")
    public GraceJSONResult queryAllList(@RequestParam Integer status,
                                        @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                        @RequestParam Integer page,
                                        @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                        @RequestParam Integer pageSize);


    @PostMapping("doReview")
    @ApiOperation(value = "管理员对文章进行审核通过或者失败", notes = "管理员对文章进行审核通过或者失败", httpMethod = "POST")
    public GraceJSONResult doReview(@RequestParam String articleId,
                                    @RequestParam Integer passOrNot);

    @PostMapping("/delete")
    @ApiOperation(value = "用户删除文章", notes = "用户删除文章", httpMethod = "POST")
    public GraceJSONResult delete(@RequestParam String userId,
                                  @RequestParam String articleId);

    @PostMapping("/withdraw")
    @ApiOperation(value = "用户撤回文章", notes = "用户撤回文章", httpMethod = "POST")
    public GraceJSONResult withdraw(@RequestParam String userId,
                                    @RequestParam String articleId);
}
