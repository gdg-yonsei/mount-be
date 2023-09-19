package gdsc.backend.dto;

import lombok.Data;

@Data
public class GetFileMetaDataResponse {

    private String saveFileName;
    private String userId;

    public GetFileMetaDataResponse(String saveFileName, String userId) {
        this.saveFileName = saveFileName;
        this.userId = userId;
    }

}
