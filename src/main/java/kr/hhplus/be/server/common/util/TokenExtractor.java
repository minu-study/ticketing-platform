package kr.hhplus.be.server.common.util;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
public class TokenExtractor {

    public static String getQueueToken() {

        if (StringUtils.hasText(getHeaderValue("Auth-Queue-Token"))) {
            return getHeaderValue("Auth-Queue-Token");
        } else {
            log.error("Auth-Queue-Token header is empty.");
            throw new AppException(ErrorCode.NOT_FOUND_TOKEN);
        }

    }

    private static String getHeaderValue(String headerName) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(headerName);
    }
}

