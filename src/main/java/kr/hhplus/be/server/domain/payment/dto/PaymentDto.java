package kr.hhplus.be.server.domain.payment.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentView {
        private Long id;
        private UUID userId;
        private Long reservationId;
        private int amount;
        private String status;
        private LocalDateTime paidAt;
        private LocalDateTime canceledAt;
    }

    public static class SetPayment {

        @Getter
        @Setter
        public static class Request {
            private Long reservationId;
        }

    }

    public static class GetPayment {
        @Getter
        @Setter
        public static class Request {
            private UUID userId;
        }

        @Getter
        @Builder
        public static class Response {
            private List<PaymentView> list;
        }
    }

}