package kr.hhplus.be.server.api.event.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class EventDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventSummaryView {
        private String categoryCode;
        private String categoryName;
        private Long eventId;
        private String eventCode;
        private String eventName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetEventScheduleInfoView {
        private Long eventId;
        private Long eventScheduleId;
        private String categoryCode;
        private String categoryName;
        private String eventCode;
        private String eventName;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private int totalSeats;
        private int availableSeats;
        private int reservedSeats;
        private int holdSeats;

    }

    public static class GetEventList {

        @Getter
        @Setter
        public static class Request {
            private Long categoryId;
        }

        @Getter
        @Builder
        public static class Response {
            private List<EventSummaryView> list;
        }

    }

    public static class GetEventScheduleList {

        @Getter
        @Setter
        public static class Request {
            private Long eventId;
        }

        @Getter
        @Builder
        public static class Response {
            private List<GetEventScheduleInfoView> list;
        }
    }

}