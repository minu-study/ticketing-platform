package kr.hhplus.be.server.domain.balanceLog.dto;

import lombok.*;

import java.util.UUID;

public class BalanceDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BalanceView {
        private UUID userId;
        private int balance;
    }

    public static class ChargeBalance {

        @Getter
        @Setter
        public static class Request {
            private int amount;
        }


    }

}
