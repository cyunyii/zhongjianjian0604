package cn.xmu.files.util;

import io.minio.*;

import io.minio.http.Method;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImgUploads {


    final static Logger logger = LoggerFactory.getLogger(ImgUploads.class);

    public static String remoteSaveImg(MultipartFile multipartFile,
                                       int size, String ACCESSKEY, String SECRETKEY, String BUCKETNAME, String ENDPOINT) throws IOException {


        //报错的返回判断错误码
        //判断是否是图片
        if(!isImg(multipartFile))
            return "512";
        //return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);

        //判断文件大小是否符合要求
        if(multipartFile.getSize()>size*1024*1024){
            return "513";
            // return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_ERROR);
        }

        String s = null;
        try {
            String policy = "{\n" +
                    "  \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Action\": [\n" +
                    "                \"s3:GetBucketLocation\",\n" +
                    "                \"s3:ListBucket\"\n" +
                    "            ],\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": \"*\",\n" +
                    "            \"Resource\": \"arn:aws:s3:::" + BUCKETNAME+ "\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"Action\": \"s3:GetObject\",\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": \"*\",\n" +
                    "            \"Resource\": \"arn:aws:s3:::" + BUCKETNAME + "/*\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"Version\": \"2012-10-17\"\n" +
                    "}";

            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESSKEY,SECRETKEY)
                    .build();

            //存入bucket不存在则创建，并设置为只读
            boolean isExist = minioClient .bucketExists(BucketExistsArgs.builder().bucket(BUCKETNAME).build());
            if(!isExist){

                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKETNAME).build());
            }

//            //将存储桶策略配置设置为存储桶。权限设置
//            minioClient.setBucketPolicy(
//                    SetBucketPolicyArgs.builder().config(policy).bucket(BUCKETNAME).build());

            String filename =multipartFile.getOriginalFilename();

            //日期文件夹的设置
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // 文件存储的目录结构
            // String objectName = sdf.format(new Date()) + "/" + filename;

            String objectName = filename;

            // 存储文件
            // 使用putObject上传一个文件到存储桶中。
            minioClient.putObject(PutObjectArgs.builder().bucket(BUCKETNAME).object(objectName).contentType(multipartFile.getContentType()).
                    stream(multipartFile.getInputStream(), -1, 1024 * 1024 * 10).build());

//            //路径获取
//            String s1 = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(BUCKETNAME).object(filename).
//                    method(Method.GET).build());

            //常规访问路径获取
            //8001路径
            //s=ENDPOINT + "/" + BUCKETNAME + "/" + objectName;

            //8000路径访问
            s="http://znbwc.cn:8000" + "/" + BUCKETNAME + "/" + objectName;

            logger.info("文件上传成功!");
            return s;

        } catch (Exception e) {
            logger.info("上传发生错误: {}！", e.getMessage());
            return "511";
            //  return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

    }

    /**
     * 删除远程服务器文件
     *
     */
    public static void deleteRemoteImg(String multipartFile,
                                       String ACCESSKEY, String SECRETKEY, String BUCKETNAME,String ENDPOINT){

        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESSKEY,SECRETKEY)
                    .build();

            // minioClient.removeObject(BUCKETNAME,multipartFile);

            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(multipartFile)
                    .bucket(BUCKETNAME).build();
            minioClient.removeObject(objectArgs);
        } catch (Exception e) {
            logger.error( "删除失败"+e.getMessage());
        }
        return ;


    }
    /**
     * 判断文件是否是图片
     *
     * @param multipartFile 文件
     *
     */
    public static boolean isImg(MultipartFile multipartFile) throws IOException{
        BufferedImage bi = ImageIO.read(multipartFile.getInputStream());
        if(bi == null){
            return false;
        }
        return true;
    }
}
