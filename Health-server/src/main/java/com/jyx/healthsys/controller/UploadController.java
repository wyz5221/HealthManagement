package com.jyx.healthsys.controller;

import com.jyx.Data_unification.Unification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {

    @Value("${file.upload-dir}")  // 改为更明确的配置名
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    @PostMapping("/image")
    public Unification<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件非空
            if (file.isEmpty()) {
                return Unification.fail("请选择要上传的文件");
            }

            // 创建绝对路径的目录
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                    log.info("创建上传目录: {}", uploadPath);
                } catch (IOException e) {
                    log.error("无法创建上传目录: {}", uploadPath, e);
                    return Unification.fail("服务器存储配置错误");
                }
            }

            // 生成唯一文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 解析目标文件路径
            Path targetLocation = uploadPath.resolve(filename);

            try {
                // 保存文件
                file.transferTo(targetLocation);
                log.info("文件保存到: {}", targetLocation);

                // 返回访问URL
                String fileUrl = accessPath + filename;
                return Unification.success(fileUrl);
            } catch (IOException ex) {
                log.error("无法保存文件: {}", filename, ex);
                return Unification.fail("无法保存文件");
            }
        } catch (Exception ex) {
            log.error("上传失败", ex);
            return Unification.fail("上传失败: " + ex.getMessage());
        }
    }
}