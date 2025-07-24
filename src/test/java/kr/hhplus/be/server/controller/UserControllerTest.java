package kr.hhplus.be.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.api.user.controller.UserController;
import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest
@AutoConfigureRestDocs
@Import(TestcontainersConfiguration.class)
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestDocumentationContextProvider restDocumentation;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserController userController;


    private MockMvc mockMvc;
    private final UUID testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

//        userController.users.clear();  // 기존 데이터 삭제
//        userController.users.put(
//                testUserId,
//                new User(testUserId, "정민우1", 100000, LocalDateTime.now(), LocalDateTime.now())
//        );

    }


    @Test
    void getUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.userName").value("정민우1"))
                .andExpect(jsonPath("$.balance").value(100000))
                .andDo(document("get-user-by-id",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("사용자 ID (UUID)")
                        ),
                        responseFields(
                                fieldWithPath("id").description("사용자 ID"),
                                fieldWithPath("userName").description("사용자 이름"),
                                fieldWithPath("balance").description("사용자 잔액"),
                                fieldWithPath("insertAt").description("생성일시"),
                                fieldWithPath("updateAt").description("수정일시")
                        )
                ));
    }

    @Test
    void getUserBalance() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/balance", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.balance").value(100000))
                .andDo(document("get-user-balance",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID (UUID)")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("사용자 ID"),
                                fieldWithPath("balance").description("사용자 잔액")
                        )
                ));
    }

    @Test
    void chargeBalance() throws Exception {
        BalanceDto.ChargeBalance.Request request = new BalanceDto.ChargeBalance.Request();
        request.setAmount(100000);

        mockMvc.perform(post("/api/users/{userId}/balance/charge", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.balance").value(200000))
                .andDo(document("charge-balance",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID (UUID)")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("충전할 금액")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("사용자 ID"),
                                fieldWithPath("balance").description("충전 후 잔액")
                        )
                ));
    }

}