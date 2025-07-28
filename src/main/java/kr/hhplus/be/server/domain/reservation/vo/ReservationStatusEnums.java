package kr.hhplus.be.server.domain.reservation.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatusEnums {

    TEMP("TEMP"),
    CONFIRMED("CONFIRMED"),
    EXPIRED("EXPIRED"),
    CANCELED("CANCELED")
    ;

    private final String status;

}
