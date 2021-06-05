# 中间件大作业

###### 注意：此版本前端代码和后端代码应该放在一个文件夹下，并且前端代码应该有portal\a目录（即进阶版之后的版本）

#### 19:14 cyx
- 修改model里部分vo文件->id为Long类型
- 陈芸衣修改user的AppUserMngServiceImpl文件
- 实现第九章，页面静态化（需要使用进阶前端）
- 修改article的pomapper，实现example修改，更改了article的删除文章部分的逻辑代码（将物理删除改为逻辑删除）

#### 20:40 wmc
- 修改，comment（model、controller），AppuserVo(String-》long id)，article（model 部分string-》long id）

#### 21:59 cyx
- 在第九章静态页面基础上，增加rabbitmq功能，实现rabbitmq上传消息，下载静态页面
- 修改article和article-html的配置文件的静态页面下载路径为相对路径

#### 21:59 wmc
- 修改ArticlePortalServiceImpl.java