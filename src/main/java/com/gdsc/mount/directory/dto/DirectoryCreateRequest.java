package com.gdsc.mount.directory.dto;

import lombok.Getter;

@Getter
public class DirectoryCreateRequest {
    private String path;
    private String username;

    public DirectoryCreateRequest(String path, String username) {
        this.path = path;
        this.username = username;
    }
}
