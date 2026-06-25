package org.nep.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("文件为空");
        if (file.getSize() > 5 * 1024 * 1024) return Result.fail("文件大小不能超过5MB");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            return Result.fail("只允许上传图片文件");

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            File dest = new File(dir, filename);

            file.transferTo(dest);

            String url = "/images/" + filename;
            return Result.ok("上传成功", url);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}