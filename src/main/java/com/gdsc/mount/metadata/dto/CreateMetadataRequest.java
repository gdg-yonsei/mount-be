package com.gdsc.mount.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class CreateMetadataRequest  {
    private String name;
    private String username;
    private String path;
    private boolean atRoot;

    protected CreateMetadataRequest() {}
}