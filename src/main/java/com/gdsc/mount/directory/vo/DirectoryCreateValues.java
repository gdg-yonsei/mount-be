package com.gdsc.mount.directory.vo;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectoryCreateValues {
    @NotBlank
    @ValidName
    private String username;

    @NotBlank
    @ValidPath
    private String pathWithDirectory;
}
