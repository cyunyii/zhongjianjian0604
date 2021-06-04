package cn.xmu.files.service.impl;

import cn.xmu.files.mapper.AppUserPoMapper;
import cn.xmu.files.model.po.AppUserPo;
import cn.xmu.files.service.UploaderService;
//import cn.xmu.files.util.ImgUploads;
import cn.xmu.files.util.ImgUploads;
import cn.xmu.grace.result.GraceJSONResult;
import cn.xmu.grace.result.ResponseStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploaderServiceImpl implements UploaderService {


    final static Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    @Autowired
    private  AppUserPoMapper appUserPoMapper;

    @Value("${minio.endpoint}")
    private String ENDPOINT;
    @Value("${minio.bucketName}")
    private String BUCKETNAME;
    @Value("${minio.accessKey}")
    private String ACCESSKEY;
    @Value("${minio.secretKey}")
    private String SECRETKEY;

    @Override
    @Transactional
    public String uploadFace(Long id, MultipartFile multipartFile) {


        AppUserPo appUserPo = appUserPoMapper.selectByPrimaryKey(id);

        if(appUserPo == null){

            //return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

        //AppUserBO appUserBO = new AppUserBO(appUserPo);

        String path = null;
        try {

            path = ImgUploads.remoteSaveImg(multipartFile, 1, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);

            //文件上传错误
            if (path=="511") {
                logger.debug("511");
               // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }
            if (path=="512") {
                logger.debug("512");
                //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
            }
            if (path=="513") {
                logger.debug("513");
               //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
            }

            String oldFilename = appUserPo.getFace();
            //appUserBO.setFace(path);
            appUserPo.setFace(path);
            int ret = appUserPoMapper.updateByPrimaryKeySelective(appUserPo);
            if(ret==0){

                //数据库更新失败，需要删除新增的图片
                logger.debug("511");
                ImgUploads.deleteRemoteImg(path, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);
               // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldFilename != null) {
                ImgUploads.deleteRemoteImg(oldFilename, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);
            }
              }catch (IOException e) {
            logger.debug("513");
           // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
            }
            return  path;
           // return GraceJSONResult.ok(path);
    }

    //上传多个图片（写文章时，可以拖拽上传多个文件并在界面显示）
    @Transactional
    @Override
    public List<String> uploadsomeFiles(Long id, MultipartFile[] files) {


        AppUserPo appUserPo = appUserPoMapper.selectByPrimaryKey(id);

        if(appUserPo == null){

            //return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

        //AppUserBO appUserBO = new AppUserBO(appUserPo);

        List<String> paths = new ArrayList<>();
        try {

            if(files!=null&&files.length > 0) {

                for (MultipartFile multipartFile : files) {

                    String path = " ";

                    path = ImgUploads.remoteSaveImg(multipartFile, 1, ACCESSKEY, SECRETKEY, BUCKETNAME, ENDPOINT);

                    paths.add(path);

                    //文件上传错误
                    if (path == "511") {
                        logger.debug("511");
                        // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
                    }
                    if (path == "512") {
                        logger.debug("512");
                        //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                    }
                    if (path == "513") {
                        logger.debug("513");
                        //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
                    }


                }
            }
        }catch (IOException e) {
            logger.debug("513");
            // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
        }
        return  paths;
        // return GraceJSONResult.ok(path);

    }
}




