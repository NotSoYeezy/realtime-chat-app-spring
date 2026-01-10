package com.chat.realtimechat.controller;

import com.chat.realtimechat.util.UploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class MediaServeController {

    private final UploadUtils uploadUtils;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String savedFileName = uploadUtils.saveFile(file);

            String fileUrl = "/api/files/" + savedFileName;

            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        try {
            Resource resource = uploadUtils.loadFileAsResource(filename);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            try {
                String probedType = Files.probeContentType(resource.getFile().toPath());
                if (probedType != null) {
                    contentType = probedType;
                }
            } catch (IOException ex) {
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}