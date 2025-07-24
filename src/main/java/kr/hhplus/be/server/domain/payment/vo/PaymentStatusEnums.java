package kr.hhplus.be.server.domain.payment.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatusEnums {

    PENDING("PENDING"),
    COMPLETE("COMPLETE"),
    CANCELED("CANCELED"),
    FAILED("FAILED")
    ;

    private final String status;

}
