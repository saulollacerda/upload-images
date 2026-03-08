package com.upimages.upimages.controller;

import com.upimages.upimages.dto.ImageResponseDTO;
import com.upimages.upimages.dto.ImageUploadDTO;
import com.upimages.upimages.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/images")
public class ImageController {

    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ImageResponseDTO> uploadImage(@Valid @ModelAttribute ImageUploadDTO dto) throws Exception {
        ImageResponseDTO dtoResponse = service.uploadImageToS3(dto);
        service.insertImage(dto, dtoResponse);

        return ResponseEntity.ok().body(dtoResponse);
    }

    @GetMapping(path = "/{key}")
    public ResponseEntity<String> getImage(@PathVariable String key) {
        String url = service.getPresignedImage(key);

        return ResponseEntity.ok().body(url);
    }
}
