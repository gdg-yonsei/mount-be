package gdsc.be.mount.storage.dto.response;

import org.springframework.core.io.UrlResource;

public record FileDownloadResponse(UrlResource urlResource, String contentDisposition){
}
