package kr.hhplus.be.server.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
public class AppException extends RuntimeException {

    private String message= "9999";
    private String errorCode;
    private HttpStatus httpStatus;

    private Map<String, Object> data;

    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
        this.message = message;
    }
    public AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public AppException(ErrorCode error) {
        super(error.getMsg());
        this.errorCode = error.getCode();
        this.message = error.getMsg();
    }

    public AppException(ErrorCode error, String message) {
        super(error.getMsg() + message);
        this.errorCode = error.getCode();
        this.message = error.getMsg() + message;
    }

    public AppException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.data = data;
    }

    public AppException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
