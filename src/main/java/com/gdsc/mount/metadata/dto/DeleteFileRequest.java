package com.gdsc.mount.metadata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteFileRequest {

    @NotBlank
    private String username;

    @NotNull
    private String path;

    @NotBlank
    private String fileName;

    protected DeleteFileRequest() {}

}
