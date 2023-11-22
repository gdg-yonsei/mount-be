package com.gdsc.mount.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DownloadFileRequest {
    private String username;
    private String path;
    private String fileName;

    protected DownloadFileRequest() {}

}
