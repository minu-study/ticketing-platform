package kr.hhplus.be.server.common.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseUtil {

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
}

