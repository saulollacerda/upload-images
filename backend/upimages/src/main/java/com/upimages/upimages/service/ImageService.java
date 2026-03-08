package com.upimages.upimages.service;

import com.upimages.upimages.dto.ImageResponseDTO;
import com.upimages.upimages.dto.ImageUploadDTO;
import com.upimages.upimages.entity.ImageEntity;
import com.upimages.upimages.entity.UserEntity;
import com.upimages.upimages.repository.ImageRepository;
import com.upimages.upimages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public ImageService(S3Client s3Client, ImageRepository imageRepository, UserRepository userRepository, S3Presigner s3Presigner) {
        this.imageRepository = imageRepository;
        this.s3Client = s3Client;
        this.userRepository = userRepository;
        this.s3Presigner = s3Presigner;
    }


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

    public void insertImage (ImageUploadDTO imageUploadDTO, ImageResponseDTO imageResponseDTO) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        ImageEntity imageEntity = new ImageEntity(imageUploadDTO, imageResponseDTO, user);

        imageRepository.save(imageEntity);
    }

    public String getPresignedImage(String key) {

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        GetObjectPresignRequest getObjectPresignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest =
                s3Presigner.presignGetObject(getObjectPresignRequest);

        s3Presigner.close();

        return presignedGetObjectRequest.url().toString();
    }

    private String generateFileName(String originalName) {
        return UUID.randomUUID() + "-" + originalName;
    }
}
