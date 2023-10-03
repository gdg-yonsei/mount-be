package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderDownloadExpcetion extends BusinessException {
    public FileFolderDownloadExpcetion() {
        super(ErrorCode.FILEFOLDER_DOWNLOAD_FAILED);
    }
}
