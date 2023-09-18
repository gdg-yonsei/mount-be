package com.gdsc.mount.common.domain;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

@Getter
public class Node extends TimestampEntity {

    @NotNull
    private NodeType nodeType;

    @Field(name = "name")
    @NotNull
    private String name;

    @Field(name = "parent_directory_id")
    private String parentDirectoryId;

    @Field(name = "path")
    private String path;

    @Field(name = "at_root")
    private boolean atRoot;

    @Field(name = "is_a_leaf")
    private boolean isALeaf;

    protected Node() {}

    public Node(NodeType nodeType, String name, String parentDirectoryId, String path, boolean atRoot, boolean isALeaf) {
        this.nodeType = nodeType;
        this.name = name;
        this.parentDirectoryId = parentDirectoryId;
        this.path = path;
        this.atRoot = atRoot;
        this.isALeaf = isALeaf;
    }
}
