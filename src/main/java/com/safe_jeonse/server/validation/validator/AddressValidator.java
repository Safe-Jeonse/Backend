package com.safe_jeonse.server.validation.validator;

import com.safe_jeonse.server.service.AddressValidationService;
import com.safe_jeonse.server.validation.annotation.ValidAddress;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AddressValidator implements ConstraintValidator<ValidAddress, String> {

    private final AddressValidationService avs;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return avs.validateAddress(s);
    }
}
