package com.gdsc.mount.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectoryCreateRequest {
    private String path;
    private String username;
}