package kr.hhplus.be.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    AUTH001("AUTH001", "존재하지 않는 계정입니다."),
    AUTH002("AUTH002", "이미 존재하는 계정 정보 입니다."),
    AUTH003("AUTH003", "인증 헤더가 비어있습니다."),
    AUTH004("AUTH004", "유효하지 않은 인증 정보입니다."),
    AUTH005("AUTH005", "만료된 토큰 입니다."),

    QUEUE001("QUEUE001", "아직 대기 순번입니다. 잠시후 다시 요청해주세요."),

    DB001("DB001", "존재하지 않는 데이터입니다."),

    PAYMENT001("PAYMENT001", "결제에 실패했습니다. 결제금이 상품 가격보다 적습니다."),
    PAYMENT002("PAYMENT002", "지원하지 않는 결제 수단입니다."),
    PAYMENT003("PAYMENT003", "결제금 충전에 실패했습니다. 결제금액은 양수만 가능합니다."),
    PAYMENT004("PAYMENT004", "결제에 실패했습니다.결제금은 양수만 가능합니다."),
    ;

    private final String code;
    private final String msg;

}
