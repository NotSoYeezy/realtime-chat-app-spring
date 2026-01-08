package com.chat.realtimechat.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class UploadUtils {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String savedFileName = UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return savedFileName;
    }

    public Resource loadFileAsResource(String fileName) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();

        if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
            return null;
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        }

        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}