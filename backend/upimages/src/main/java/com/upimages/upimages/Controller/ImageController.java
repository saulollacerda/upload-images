package com.upimages.upimages.Controller;

import com.upimages.upimages.Service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ImageService service;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                        @RequestParam("description") String description) throws Exception {
        service.imageValidation(file);

        return ResponseEntity.ok(Map.of(
                "name", file.getOriginalFilename(),
                "size", file.getSize(),
                "description", description
        ));
    }
}
