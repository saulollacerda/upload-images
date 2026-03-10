package com.upimages.upimages.annotation;

import com.upimages.upimages.validator.ValidFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFileValidator.class)
public @interface ValidFile {
    String message() default "Arquivo inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
