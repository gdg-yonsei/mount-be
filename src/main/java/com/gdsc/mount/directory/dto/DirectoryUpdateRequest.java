package com.gdsc.mount.directory.dto;

import lombok.Getter;

@Getter
public class DirectoryUpdateRequest {
    private String pathIncludingDirectory;
    private String newDirectoryName;
    private String username;

    public DirectoryUpdateRequest(String pathIncludingDirectory, String newDirectoryName, String username) {
        this.pathIncludingDirectory = pathIncludingDirectory;
        this.newDirectoryName = newDirectoryName;
        this.username = username;
    }
}
