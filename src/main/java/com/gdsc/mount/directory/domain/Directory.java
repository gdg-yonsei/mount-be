package com.gdsc.mount.directory.domain;

import com.gdsc.mount.common.domain.Node;
import com.gdsc.mount.common.domain.NodeType;
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


    protected Directory() {}

    public Directory(String name, String parentDirectoryId, String path, boolean atRoot, boolean isLeaf) {
        super(NodeType.DIRECTORY, name, parentDirectoryId, path, atRoot, isLeaf);
    }

}
