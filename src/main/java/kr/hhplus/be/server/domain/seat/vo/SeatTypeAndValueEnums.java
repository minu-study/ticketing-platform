package kr.hhplus.be.server.domain.seat.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeatTypeAndValueEnums {

    VIP("VIP", 20000),
    PREMIUM("PREMIUM", 15000),
    STANDARD("STANDARD", 10000)
    ;

    private final String type;
    private final int value;

}
