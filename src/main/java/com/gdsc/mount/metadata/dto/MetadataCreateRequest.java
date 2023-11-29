package com.gdsc.mount.metadata.dto;

import com.gdsc.mount.validation.NodeNameValidation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class MetadataCreateRequest {
    @NodeNameValidation
    private String name;

    @NotBlank
    private String username;

    // TODO: add validation that it ends and starts with "/"
    // TODO: add validation that it should have no cosecutive // in it
    @NotNull
    private String path;

    protected MetadataCreateRequest() {}
}
