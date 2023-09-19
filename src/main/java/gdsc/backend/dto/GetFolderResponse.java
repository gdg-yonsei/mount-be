package gdsc.backend.dto;

import lombok.Data;

@Data
public class GetFolderResponse {

    private String name;
    private String userId;

    public GetFolderResponse(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }
}
