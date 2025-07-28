package kr.hhplus.be.server.domain.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReservationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationSummaryView {
        private Long id;
        private String eventCode;
        private String eventName;
        private LocalDateTime eventStartDateTime;
        private LocalDateTime eventEndDateTime;
        private int seatNumber;
        private String status;
        private LocalDateTime reservedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime canceledAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationDetailView {
        private Long id;
        private UUID userId;
        private Long seatId;
        private Long scheduleId;
        private Long eventId;
        private String eventName;
        private String eventCode;
        private int seatNumber;
        private String seatType;
        private String status;
        private LocalDateTime reservedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime canceledAt;
        private LocalDateTime eventStartDateTime;
        private LocalDateTime eventEndDateTime;
        private int paymentAmount;
    }

    public static class SetReservation {

        @Getter
        @Setter
        public static class Request {
            private Long scheduleId;
            private Long seatId;
        }

        @Getter
        @Builder
        public static class Response {
            private Long reservationId;
        }
    }

    public static class CancelReservation {

        @Getter
        @Setter
        public static class Request {
            private Long reservationId;
        }
    }

    public static class GetReservationList {

        @Getter
        @Builder
        public static class Response {
            private List<ReservationSummaryView> list;
        }
    }

    public static class GetReservation {
        @Getter
        @Setter
        public static class Request {
            private Long reservationId;
        }

        @Getter
        @Builder
        public static class Response {
            private ReservationDetailView view;
        }
    }


}