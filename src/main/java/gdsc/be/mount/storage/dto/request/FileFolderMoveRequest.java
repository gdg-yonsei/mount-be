package gdsc.be.mount.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileFolderMoveRequest(
        @NotBlank
        String userName,
        @NotNull
        Long newParentFolderId
){

}

