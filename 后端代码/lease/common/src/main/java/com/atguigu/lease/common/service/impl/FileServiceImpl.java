package com.atguigu.lease.common.service.impl;

import com.atguigu.lease.common.minio.MinioProperties;
import com.atguigu.lease.common.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件服务实现类
 * 负责文件上传到MinIO存储
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private MinioProperties minioProperties;

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, 
                                                   IOException, NoSuchAlgorithmException, InvalidKeyException, 
                                                   InvalidResponseException, XmlParserException, InternalException {

        // 检查并创建存储桶
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioProperties.getBucketName()).config(createBucketPolicyConfig(minioProperties.getBucketName())).build());
        }
        
        // 生成文件名：日期/UUID-原文件名
        String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        // 上传文件到MinIO
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(filename)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        
        // 返回文件访问URL
        return String.join("/", minioProperties.getEndpoint(), minioProperties.getBucketName(), filename);
    }

    /**
     * 创建存储桶策略配置
     * 允许公开读取存储桶中的所有对象
     */
    private String createBucketPolicyConfig(String bucketName) {
        return """
                {
                  "Statement" : [ {
                    "Action" : "s3:GetObject",
                    "Effect" : "Allow",
                    "Principal" : "*",
                    "Resource" : "arn:aws:s3:::%s/*"
                  } ],
                  "Version" : "2012-10-17"
                }
                """.formatted(bucketName);
    }
}