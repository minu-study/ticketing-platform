package kr.hhplus.be.server.domain.seat.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeatStatusEnums {

    AVAILABLE("AVAILABLE"),
    RESERVED("RESERVED"),
    HOLD("HOLD")
    ;

    private final String status;


}
