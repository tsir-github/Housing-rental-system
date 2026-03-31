package com.atguigu.lease.common.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件到MinIO
     * @param file 要上传的文件
     * @return 文件访问URL
     */
    String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, 
                                           IOException, NoSuchAlgorithmException, InvalidKeyException, 
                                           InvalidResponseException, XmlParserException, InternalException;
}