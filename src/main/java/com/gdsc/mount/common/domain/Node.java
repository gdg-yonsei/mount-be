package com.gdsc.mount.common.domain;

import com.gdsc.mount.directory.domain.Directory;
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

    @Field(name = "parent_directory")
    private Directory parentDirectory;

    @Field(name = "path")
    private String path;

    @Field(name = "at_root")
    private boolean atRoot;

    @Field(name = "is_a_leaf")
    private boolean isALeaf = true;

    protected Node() {}

    public Node(NodeType nodeType, String name, Directory parentDirectory, String path, boolean atRoot) {
        this.nodeType = nodeType;
        this.name = name;
        this.atRoot = atRoot;
        if (atRoot) {
            this.path = "/" + name;
        } else {
            this.path = parentDirectory.getPath() + "/" + name;
            this.parentDirectory = parentDirectory;
        }
    }

    public void setNotALeaf() {
        this.isALeaf = false;
    }
}
