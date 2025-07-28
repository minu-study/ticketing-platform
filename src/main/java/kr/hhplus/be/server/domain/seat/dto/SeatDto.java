package kr.hhplus.be.server.domain.seat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class SeatDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SeatView {
        private Long eventScheduleId;
        private int seatNumber;
        private String seatType;
        private String status;
    }

    public static class getSeats {

        @Getter
        @Setter
        public static class Request {
            @NotNull
            private Long scheduleId;
        }

        @Getter
        @Builder
        public static class Response {
            private List<SeatView> list;
        }

    }

}
