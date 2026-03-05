package com.upimages.upimages.service;

import com.upimages.upimages.dto.ImageResponseDTO;
import com.upimages.upimages.dto.ImageUploadDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ImageService {

    private final S3Client s3Client;

    public ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public ImageResponseDTO uploadImageToS3(ImageUploadDTO dto) throws IOException  {
        String key = generateFileName(dto.getFile().getOriginalFilename());

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(dto.getFile().getContentType())
                        .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(dto.getFile().getBytes())
        );

        return new ImageResponseDTO(LocalDateTime.now(), key);
    }

    private String generateFileName(String originalName) {
        return UUID.randomUUID() + "-" + originalName;
    }
}
