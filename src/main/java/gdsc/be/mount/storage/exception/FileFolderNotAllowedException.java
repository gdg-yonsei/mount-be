package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;
import gdsc.be.mount.storage.Enum.ActionType;
import lombok.Getter;

@Getter
public class FileFolderNotAllowedException extends BusinessException {
    private final ActionType actionType;

    public FileFolderNotAllowedException(ActionType actionType) {
        super(ErrorCode.FILEFOLDER_NOT_ALLOWED);
        this.actionType = actionType;
    }
}
