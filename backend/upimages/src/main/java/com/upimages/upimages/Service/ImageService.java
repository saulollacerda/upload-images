package com.upimages.upimages.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

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
}
