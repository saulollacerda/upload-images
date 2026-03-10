package com.upimages.upimages.dto;

import com.upimages.upimages.annotation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

public class ImageUploadDTO {

    @ValidFile
    private MultipartFile file;

    @Size(max = 255)
    private String description;

    private Timestamp uploadDate;

    public ImageUploadDTO() {
    }
    public ImageUploadDTO(MultipartFile file, String description, Timestamp uploadDate) {
        this.file = file;
        this.description = description;
        this.uploadDate = uploadDate;

    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }
}
