package com.upimages.upimages.validator;

import com.upimages.upimages.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            replaceMessage(context, "O arquivo não pode ser vazio");
            return false;
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            replaceMessage(context, "Tipo de arquivo não permitido. Aceitos: JPEG, PNG, GIF, WEBP");
            return false;
        }

        if (file.getSize() > MAX_SIZE) {
            replaceMessage(context, "O arquivo não pode ser maior do que 5MB");
            return false;
        }

        return true;
    }

    private void replaceMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message);
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
