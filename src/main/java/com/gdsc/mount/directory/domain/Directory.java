package com.gdsc.mount.directory.domain;

import com.gdsc.mount.common.domain.Node;
import com.gdsc.mount.common.domain.NodeType;
import com.gdsc.mount.metadata.domain.Metadata;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "directories")
public class Directory extends Node {

    @DBRef
    private List<Node> children = new ArrayList<>();


    public Directory() {}

    public Directory(String name, Directory parentDirectory, String path, boolean atRoot) {
        super(NodeType.DIRECTORY, name, parentDirectory, path, atRoot);
    }

    public void addDirectory(Directory directory) {
        children.add(directory);
    }

    public void addMetadata(Metadata metadata) {
        children.add(metadata);
    }

}
