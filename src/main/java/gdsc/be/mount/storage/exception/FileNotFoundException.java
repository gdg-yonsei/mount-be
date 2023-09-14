package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileNotFoundException extends BusinessException {
    public final static BusinessException EXCEPTION = new FileNotFoundException();

    private FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND);
    }

}
