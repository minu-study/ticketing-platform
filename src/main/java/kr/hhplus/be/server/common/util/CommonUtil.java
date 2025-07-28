package kr.hhplus.be.server.common.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;

@Slf4j
public class CommonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public static ResponseEntity<ApiResponse> convertResponse() {
        return ResponseEntity.ok(ApiResponse.builder().build());
    }

    public static ResponseEntity<ApiResponse> convertResponse(Object result) {
        HashMap<String, Object> convert = OBJECT_MAPPER.convertValue(result, HashMap.class);
        return ResponseEntity.ok(
                ApiResponse.builder().data(convert).build());
    }

    public static String getQueueToken() {

        if (StringUtils.hasText(getHeaderValue("Auth-Queue-Token"))) {
            String token = getHeaderValue("Auth-Queue-Token");
            return token;
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
