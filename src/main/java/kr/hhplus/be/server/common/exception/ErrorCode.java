package kr.hhplus.be.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // AUTH
    NOT_FOUND_ACCOUNT("NOT_FOUND_ACCOUNT", "존재하지 않는 계정입니다."),
    DUPLICATE_ACCOUNT("DUPLICATE_ACCOUNT", "이미 존재하는 계정 정보 입니다."),
    NOT_FOUND_TOKEN("NOT_FOUND_TOKEN", "인증 토큰이 존재하지 않습니다."),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 인증 정보입니다."),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰 입니다."),

    // RESERVATION
    SEAT_NOT_AVAILABLE("SEAT_NOT_AVAILABLE", "예약가능한 좌석이 아닙니다."),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다."),

    // SYSTEM
    DATA_NOT_FOUND("DATA_NOT_FOUND", "존재하지 않는 데이터입니다."),

    // PAYMENT
    INSUFFICIENT_PAYMENT_AMOUNT("INSUFFICIENT_PAYMENT_AMOUNT", "결제에 실패했습니다. 결제금이 상품 가격보다 적습니다."),
    UNSUPPORTED_PAYMENT_METHOD("UNSUPPORTED_PAYMENT_METHOD", "지원하지 않는 결제 수단입니다."),
    INVALID_CHARGE_AMOUNT("INVALID_CHARGE_AMOUNT", "결제금 충전에 실패했습니다. 결제금액은 양수만 가능합니다."),
    INVALID_PAYMENT_AMOUNT("INVALID_PAYMENT_AMOUNT", "결제에 실패했습니다.결제금은 양수만 가능합니다."),
    INVALID_RESERVATION_STATUS_FOR_PAYMENT("INVALID_RESERVATION_STATUS_FOR_PAYMENT", "대기 예약만 결제가 가능합니다,  예약 상태를 확인해주세요."),
    PAYMENT_PROCESSING_ERROR("PAYMENT_PROCESSING_ERROR", "결제 처리 중 오류가 발생했습니다."),
    RESERVATION_CANNOT_BE_CANCELED("RESERVATION_CANNOT_BE_CANCELED", "취소가 불가한 예약입니다."),
    ;

    private final String code;
    private final String msg;

}
