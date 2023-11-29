package com.gdsc.mount.directory.dto;

import lombok.Getter;

@Getter
public class DirectoryDeleteRequest {
    private String username;
    private String pathWithDirectory;
    private String directoryName;
    protected DirectoryDeleteRequest() {}
}
