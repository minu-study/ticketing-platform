package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalCustomExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        ApiResponse.ApiResponseBuilder builder = ApiResponse.builder()
                .resultCode(ex.getErrorCode() != null ? ex.getErrorCode() : "9999")
                .msg(ex.getMessage());

        if (ex.getData() != null) {
            builder.data(ex.getData());
        }

        return ResponseEntity
                .status(ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST)
                .body(builder.build());
    }


    // 그 외 모든 예외 처리 (Optional)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .resultCode("9999")
                        .msg("서버 내부 오류가 발생했습니다.")
                        .build());
    }
}
