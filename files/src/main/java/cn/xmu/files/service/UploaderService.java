package cn.xmu.files.service;

import cn.xmu.grace.result.GraceJSONResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploaderService {

//GraceJSONResult throws Exception
    public  String uploadFace(Long id, MultipartFile multipartFile);

    public List<String> uploadsomeFiles(Long id, MultipartFile[] files);


}
