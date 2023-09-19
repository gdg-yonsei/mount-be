package gdsc.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Folder {

    @Id @GeneratedValue
    @Column(name = "folder_id")
    private Long id;

    private String name;
    private String userId;

    @JsonIgnore
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    private List<FileMetaData> files = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Folder> child = new ArrayList<>();

    public Folder() {
    }

    public Folder(String name, String userId, Folder parent) {
        this.name = name;
        this.userId = userId;
        this.parent = parent;
    }

}
