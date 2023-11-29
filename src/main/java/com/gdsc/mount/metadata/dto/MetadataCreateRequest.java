package com.gdsc.mount.metadata.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class MetadataCreateRequest {
    @ValidName
    @NotBlank
    private String name;

    @NotBlank
    @ValidName
    private String username;

    @NotBlank
    @ValidPath
    private String path;

    protected MetadataCreateRequest() {}
}
