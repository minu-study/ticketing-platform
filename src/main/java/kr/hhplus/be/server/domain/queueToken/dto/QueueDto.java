package kr.hhplus.be.server.domain.queueToken.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class QueueDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueTokenInfoView {
        private String token;
        private UUID userId;
        private int position;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueuePositionView {
        private String token;
        private UUID userId;
        private int position;
        private int estimatedWaitTime; // 예상 대기 시간 (분)
        private String status; // WAITING, ACTIVE, EXPIRED
        private LocalDateTime expiresAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueTokenValidationView {
        private String token;
        private UUID userId;
    }

    public static class GetQueueToken {

        @Getter
        @Setter
        public static class Request {
            private UUID userId;
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private QueueTokenInfoView view;
        }

    }

    public static class GetQueuePosition {

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private QueuePositionView view;
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationResponse {
        private boolean valid;
        private UUID userId;
        private String status; // WAITING, ACTIVE, EXPIRED
        private String message;
    }
}