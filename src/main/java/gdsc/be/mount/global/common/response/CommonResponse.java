package gdsc.be.mount.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommonResponse {
    private final Boolean success;

    // 이후 리팩토링 시 도입
    // private final String message;
    // private final String code;
}
