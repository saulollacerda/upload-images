package com.upimages.upimages.controller;

import com.upimages.upimages.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@ControllerAdvice(value = "/images")
public class ImageController {

    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("description") String description) throws Exception {
        service.imageValidation(file);

        String key = service.uploadImageToS3(file);

        return ResponseEntity.ok(Map.of(
                "key", key
        ));
    }
}
