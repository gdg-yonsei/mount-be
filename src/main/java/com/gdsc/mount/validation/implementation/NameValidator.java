package com.gdsc.mount.validation.implementation;

import com.gdsc.mount.validation.annotation.ValidName;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<ValidName, String> {
    private static final String validPattern = "^[a-zA-Z0-9_.-]+$";
    Pattern pattern = Pattern.compile(validPattern);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validateName(value);
    }

    private boolean validateName(String name) {
        return pattern.matcher(name).matches();
    }
}
