package com.gdsc.mount.validation;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NodeNameValidator implements ConstraintValidator<NodeNameValidation, String> {

    private static final char[] FORBIDDEN_CHARS = {'#', '%', '&', '{', '}', '\\', '<', '>', '*', '?', '/', ' ', '$', '!', '\'', '"', ':', '@', '+', '`', '|', '=', '€'};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validateName(value);
    }

    // TODO: name이 아닌 전체 path를 검사해야 함. /마다 잘라서 검사? 걍 전체로?
    private boolean validateName(String name) {
        for (char forbiddenChar : FORBIDDEN_CHARS) {
            if (name.indexOf(forbiddenChar) != -1) {
                return false;
            }
        }

        // Check for emojis and alt codes
        for (char character : name.toCharArray()) {
            if (character > 127) {
                return false;
            }
        }

        // Check for starting or ending with space, period, hyphen, or underline
        return !Pattern.matches("^[ .\\-_]|.*[ .\\-_]$", name);

        // If all checks pass, the name is valid
    }
}
