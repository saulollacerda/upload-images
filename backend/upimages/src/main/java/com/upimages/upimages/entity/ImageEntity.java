package com.upimages.upimages.entity;

import com.upimages.upimages.dto.ImageResponseDTO;
import com.upimages.upimages.dto.ImageUploadDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "tb_image")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String s3Key;
    private String originalFileName;
    private Long fileSize;
    private LocalDateTime uploadDate;

    @ManyToOne()
    @JoinColumn(name = "id_user")
    private UserEntity user;

    public ImageEntity() {}

    public ImageEntity(ImageUploadDTO imageUploadDTO, ImageResponseDTO imageResponseDTO, UserEntity user) {
        this.s3Key = imageResponseDTO.getKey();
        this.originalFileName = imageUploadDTO.getFile().getOriginalFilename();
        this.fileSize = imageUploadDTO.getFile().getSize();
        this.uploadDate = imageResponseDTO.getUploadedAt();
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
