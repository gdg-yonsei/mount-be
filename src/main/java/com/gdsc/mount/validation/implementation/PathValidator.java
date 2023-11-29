package com.gdsc.mount.validation.implementation;

import com.gdsc.mount.validation.annotation.ValidPath;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<ValidPath, String> {

    private static final String validPattern = "^[a-zA-Z0-9_.-]+$";
    Pattern pattern = Pattern.compile(validPattern);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validatePath(value);
    }

    private boolean validatePath(String path) {
        return checkForLeadingTrailingSlash(path) && checkForConsecutiveSlashes(path) && checkEachNode(path);
    }

    private boolean checkForConsecutiveSlashes(String path) {
        return !path.contains("//");
    }

    private boolean checkEachNode(String path) {
        String[] nodes = path.split("/");
        for (int i = 1; i < nodes.length - 1; i++) {
            if (!pattern.matcher(nodes[i]).matches()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForLeadingTrailingSlash(String path) {
        return path.startsWith("/") && path.endsWith("/");
    }
}
