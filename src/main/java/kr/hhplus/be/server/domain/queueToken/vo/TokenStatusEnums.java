package kr.hhplus.be.server.domain.queueToken.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenStatusEnums {

    ACTIVE("ACTIVE"),
    WAITING("WAITING"),
    EXPIRED("EXPIRED");

    private final String status;

}
