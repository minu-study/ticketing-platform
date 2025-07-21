package kr.hhplus.be.server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserView {
        private UUID id;
        private String userName;
        private String email;
        private int balance;
    }


    public static class SetUser {

        @Setter
        @Getter
        public static class Request {
            @NotBlank
            private String userName;
            @NotBlank
            private String email;
        }
    }

    public static class GetUser {

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private UserView view;
        }
    }

}
