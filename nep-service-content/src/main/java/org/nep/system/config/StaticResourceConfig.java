package org.nep.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;

/**
 * 静态资源配置 — 确保上传的头像/图片可通过 /images/** 访问
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决 Windows 路径问题：file: 协议 + 绝对路径 + 正斜杠
        Path uploadPath = Path.of(System.getProperty("user.dir"), "uploads");
        String location = "file:" + uploadPath.toAbsolutePath().toString().replace("\\", "/");
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

        // 也兼容旧的 /upload/avatars/ 路径
        Path oldUploadPath = Path.of(System.getProperty("user.dir"), "upload", "avatars");
        String oldLocation = "file:" + oldUploadPath.toAbsolutePath().toString().replace("\\", "/");
        if (!oldLocation.endsWith("/")) oldLocation += "/";

        registry.addResourceHandler("/upload/avatars/**")
                .addResourceLocations(oldLocation);
    }
}
