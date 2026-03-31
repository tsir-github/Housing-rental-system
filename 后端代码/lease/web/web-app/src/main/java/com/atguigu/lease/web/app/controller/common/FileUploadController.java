package com.atguigu.lease.web.app.controller.common;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.service.FileService;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 移动端文件上传控制器
 * 复用后台管理系统的FileService
 */
@Tag(name = "移动端文件管理")
@RequestMapping("/app/common/file")
@RestController
public class FileUploadController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件到MinIO
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) 
            throws ServerException, InsufficientDataException, ErrorResponseException, 
                   IOException, NoSuchAlgorithmException, InvalidKeyException, 
                   InvalidResponseException, XmlParserException, InternalException {
        
        // 验证文件
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只允许上传图片文件");
        }
        
        // 验证文件大小（10MB限制，与配置一致）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail("文件大小不能超过10MB");
        }
        
        // 复用现有的FileService上传到MinIO
        String url = fileService.upload(file);
        
        return Result.ok(url);
    }
}