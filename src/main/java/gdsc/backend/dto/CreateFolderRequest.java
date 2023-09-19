package gdsc.backend.dto;

import lombok.Data;

@Data
public class CreateFolderRequest {
    private String name;
    private String userId;
    private Long parentId;
}
