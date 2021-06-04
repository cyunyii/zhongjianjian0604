package cn.xmu.files.controller;

import cn.xmu.api.BaseController;
import cn.xmu.api.files.FileUploaderControllerApi;
import cn.xmu.exception.GraceException;
import cn.xmu.files.mapper.AppUserPoMapper;
import cn.xmu.files.model.po.AppUserPo;
import cn.xmu.files.service.UploaderService;
import cn.xmu.files.service.impl.UploaderServiceImpl;
import cn.xmu.files.util.ImgUploads;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.grace.result.ResponseStatusEnum;
import cn.xmu.pojo.bo.NewAdminBO;
import cn.xmu.utils.FileUtils;
import cn.xmu.utils.extend.AliImageReviewUtils;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mysql.cj.util.Base64Decoder;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import com.mongodb.client.model.Filters;

@RestController
public class FileUploaderController  implements FileUploaderControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FileUploaderController.class);
    @Autowired
    private AliImageReviewUtils aliImageReviewUtils;

    @Autowired
    private UploaderService uploaderService;

    @Autowired
    private AppUserPoMapper appUserPoMapper;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Value("${minio.endpoint}")
    private String ENDPOINT;
    @Value("${minio.bucketName}")
    private String BUCKETNAME;
    @Value("${minio.accessKey}")
    private String ACCESSKEY;
    @Value("${minio.secretKey}")
    private String SECRETKEY;

    @PostMapping("/uploadFace")
    @Override
    public GraceJSONResult uploadFace(Long userId,
                                      MultipartFile file)throws Exception  {

        AppUserPo appUserPo = appUserPoMapper.selectByPrimaryKey(userId);

        if(appUserPo == null){

            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        String path = null;
        try {

            path = ImgUploads.remoteSaveImg(file, 1, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);

            //文件上传错误
            if (path=="511") {
                logger.debug("511");
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }


            // 文件图片格式不支持
            if (path=="512") {
                logger.debug("512");
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
            }
            //仅支持500kb大小以下的图片上传！
            if (path=="513") {
                logger.debug("513");
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
            }

            String oldFilename = appUserPo.getFace();

            appUserPo.setFace(path);
            int ret = appUserPoMapper.updateByPrimaryKeySelective(appUserPo);
            if(ret==0){

                //数据库更新失败，需要删除新增的图片
                logger.debug("511");
                ImgUploads.deleteRemoteImg(path, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldFilename != null) {

                ImgUploads.deleteRemoteImg(oldFilename, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);
            }
        }catch (IOException e) {
            logger.debug("513");
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        return GraceJSONResult.ok(doAliImageReview(path));
        // return GraceJSONResult.ok(path);


    }


    @Override
    public GraceJSONResult uploadSomeFiles(Long userId,
                                           MultipartFile[] files) throws Exception {

        // 声明list，用于存放多个图片的地址路径，返回到前端

        AppUserPo appUserPo = appUserPoMapper.selectByPrimaryKey(userId);

        if(appUserPo == null){

            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        //循环存放图片，然后循环保存图片路径
        List<String> paths = new ArrayList<>();

        if(files!=null&&files.length > 0) {

            for (MultipartFile multipartFile : files) {

                String path = " ";

                path = ImgUploads.remoteSaveImg(multipartFile, 1, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);

                //文件上传错误
                if (path == "511") {
                    logger.debug("511");
                    continue;
                    //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
                }
                if (path == "512") {
                    logger.debug("512");
                    continue;
                    //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }
                if (path == "513") {
                    logger.debug("513");
                    continue;
                    // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
                }
                paths.add(path);
            }
        }
        return GraceJSONResult.ok(paths);
    }




    //进行上传图片的自动审核（阿里云），第一个月每天3000张免费审核
    //如果审核不通过，图片仍旧会上传至minio，但是会返回minio中error.jpeg图片（前端展示的也是这个）
    public static final String FAILED_IMAGE_URL = "http://znbwc.cn:8000/picture/error.jpeg";
    private String doAliImageReview(String pendingImageUrl) {

        // pendingImageUrl = "http://znbwc.cn:8000/picture/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
        boolean result = false;
        try {
            result = aliImageReviewUtils.reviewImage(pendingImageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!result) {
            return FAILED_IMAGE_URL;
        }

        return pendingImageUrl;
    }

    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {

        String file64 = newAdminBO.getImg64();

        byte[] bytes = Base64.getDecoder().decode(file64.trim());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        ObjectId fileID = gridFSBucket.uploadFromStream(newAdminBO.getUsername() + ".png", byteArrayInputStream);

        //获得文件在gridFS中的主键
        String fileIdStr = fileID.toString();
        return GraceJSONResult.ok(fileIdStr);
    }

    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(faceId) || faceId.equalsIgnoreCase("null")) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }
        //从gridFs中读取
        File adminFace=readGridFSByFaceId(faceId);
        FileUtils.downloadFileByStream(response,adminFace);

    }



    private File readGridFSByFaceId(String faceId) throws Exception {
        GridFSFindIterable gridFSFiles = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));
        GridFSFile gridFS=gridFSFiles.first();

        if (gridFS == null) {

            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        String fileName=gridFS.getFilename();
        System.out.println(fileName);

        File fileTemp=new File("/workspace/temp_face");
        if(!fileTemp.exists()){
            fileTemp.mkdirs();
        }
        File myFile=new File("/workspace/temp_face"+fileName);

        OutputStream os= new FileOutputStream(myFile);
        //下载到服务器或者本地

        gridFSBucket.downloadToStream(new ObjectId(faceId),os);
        return myFile;

    }

    @Override
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {

//    获得gridfs中人脸文件
        File myface=readGridFSByFaceId(faceId);
//      转换人脸为base64
       String base64Face= FileUtils.fileToBase64(myface);

       return GraceJSONResult.ok(base64Face);
    }
}


