package com.upimages.upimages.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
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

    public String uploadImageToS3(MultipartFile file) throws IOException {
        String key = generateFileName(file.getOriginalFilename());

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(file.getBytes())
        );

        return key;
    }

    private String generateFileName(String originalName) {
        return UUID.randomUUID() + "-" + originalName;
    }
}
