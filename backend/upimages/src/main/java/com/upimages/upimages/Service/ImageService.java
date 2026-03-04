package com.upimages.upimages.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
public class ImageService {

    private final S3Client s3Client;

    public ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void imageValidation(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 5000000L) {
            throw new IllegalArgumentException("File size exceeds the limit of 5MB");
        }

        if (!file.getOriginalFilename().matches(".*\\.(jpg|png|webp)$")){
            throw new IllegalArgumentException("Tipo de dado nao aceitado");
        }
    }

    public void uploadImageToS3(MultipartFile file) {
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(generateFileName(file.getOriginalFilename()))
                        .contentType(file.getContentType())
                        .build();
    }

    private String generateFileName(String originalName) {
        return UUID.randomUUID() + "-" + originalName;
    }
}
