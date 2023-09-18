package com.gdsc.mount.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class DirectoryCreateRequest {
    private String name;
    private String path;

    public DirectoryCreateRequest() {}
}
