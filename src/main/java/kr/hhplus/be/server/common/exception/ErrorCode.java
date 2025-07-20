package kr.hhplus.be.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    AUTH001("AUTH001", "인증에 실패했습니다."),
    AUTH002("AUTH002", "이미 존재하는 계정 정보 입니다."),

    DB001("DB001", "존재하지 않는 데이터입니다."),

    MEMBER001("MEMBER001", "포인트 적립에 실패했습니다. 포인트는 양수만 가능합니다."),

    PAYMENT001("PAYMENT001", "결제에 실패했습니다. 결제금이 상품 가격보다 적습니다."),
    PAYMENT002("PAYMENT002", "지원하지 않는 결제 수단입니다."),
    ;

    private final String code;
    private final String msg;

}
