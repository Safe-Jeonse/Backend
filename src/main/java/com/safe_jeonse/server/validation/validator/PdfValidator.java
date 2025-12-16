package com.safe_jeonse.server.validation.validator;

import com.safe_jeonse.server.validation.annotation.ValidPdf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.apache.tika.*;

@RequiredArgsConstructor
public class PdfValidator implements ConstraintValidator<ValidPdf, MultipartFile> {

    private final Tika tika;

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        if (file == null || file.isEmpty()) {
            return true;
        }
        try {
            String detectedType = tika.detect(file.getInputStream());
            return "application/pdf".equalsIgnoreCase(detectedType);
        } catch (IOException e) {
            return false;
        }
    }
}