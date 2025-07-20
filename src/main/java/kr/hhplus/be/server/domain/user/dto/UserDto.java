package kr.hhplus.be.server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDto {

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

}
