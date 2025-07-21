package kr.hhplus.be.server.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BalanceActionEnums{

    CHARGE("CHARGE"),
    USE("USE");

    private final String action;

}
