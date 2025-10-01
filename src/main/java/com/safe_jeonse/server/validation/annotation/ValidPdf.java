package com.safe_jeonse.server.validation.annotation;

import com.safe_jeonse.server.validation.validator.PdfValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PdfValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface ValidPdf {
    String message() default "유효한 PDF 파일이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
