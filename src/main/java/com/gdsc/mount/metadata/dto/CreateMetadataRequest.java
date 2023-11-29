package com.gdsc.mount.metadata.dto;

import com.gdsc.mount.validation.NodeNameValidation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CreateMetadataRequest  {
    @NodeNameValidation
    private String name;

    @NotBlank
    private String username;

    @NotNull
    private String path;

    @NotNull
    private boolean atRoot;

    protected CreateMetadataRequest() {}
}
