package kr.hhplus.be.server.domain.balanceLog.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BalanceActionEnums{

    CHARGE("CHARGE"),
    REFUND("REFUND"),
    USE("USE");

    private final String action;

}
