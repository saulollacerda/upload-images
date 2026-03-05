package com.upimages.upimages.dto;

import java.time.LocalDateTime;

public class ImageResponseDTO {

    private String key;
    private LocalDateTime uploadedAt;

    public ImageResponseDTO(LocalDateTime uploadedAt, String key) {
        this.uploadedAt = uploadedAt;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
