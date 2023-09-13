package gdsc.be.mount.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.UrlResource;

@Getter
@Builder
@AllArgsConstructor
public class FileDownloadResponse {
    private UrlResource urlResource;
    private String contentDisposition;
}
