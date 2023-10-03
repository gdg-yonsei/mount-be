package gdsc.be.mount.storage.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FileFolderUpdateRequest(
        @NotBlank
        String userName,
        @NotBlank
        String newFolderName
){

}

