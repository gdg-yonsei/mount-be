package com.gdsc.mount.metadata.dto;

import lombok.Getter;

@Getter
public class CreateMetadataRequest  {
    private String fileName;
    private String username;

    protected CreateMetadataRequest() {};

    public CreateMetadataRequest(String fileName, String username) {
        this.fileName = fileName;
        this.username = username;
    }
}
