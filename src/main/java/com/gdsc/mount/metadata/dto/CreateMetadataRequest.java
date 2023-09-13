package com.gdsc.mount.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class CreateMetadataRequest  {
    private String fileName;
    private String username;

    protected CreateMetadataRequest() {}
}
