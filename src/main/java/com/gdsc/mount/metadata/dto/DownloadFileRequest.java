package com.gdsc.mount.metadata.dto;

import lombok.Getter;

@Getter
public class DownloadFileRequest {
    private String username;
    private String path;
    private String fileName;

    protected DownloadFileRequest() {}

}
