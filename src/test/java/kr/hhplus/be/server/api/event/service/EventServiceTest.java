package kr.hhplus.be.server.api.event.service;

import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.event.dto.EventDto;
import kr.hhplus.be.server.domain.event.repository.EventRepository;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private QueueService queueService;

    @InjectMocks
    private EventService eventService;

    private String testToken;
    private Long testCategoryId;
    private Long testEventId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testToken = "test-token-123";
        testCategoryId = 1L;
        testEventId = 1L;
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("이벤트 목록 조회 - 성공")
    void getEventList_success() {

        // Given
        EventDto.GetEventList.Request request = new EventDto.GetEventList.Request();
        request.setCategoryId(testCategoryId);

        List<EventDto.EventSummaryView> mockEventList = Arrays.asList(
                EventDto.EventSummaryView.builder()
                        .categoryCode("CONCERT")
                        .categoryName("콘서트")
                        .eventId(1L)
                        .eventCode("EVENT001")
                        .eventName("정민우1 콘서트")
                        .build(),
                EventDto.EventSummaryView.builder()
                        .categoryCode("CONCERT")
                        .categoryName("콘서트")
                        .eventId(2L)
                        .eventCode("EVENT002")
                        .eventName("정민우2 콘서트")
                        .build()
        );

        QueueDto.QueueTokenValidationView validationView = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(validationView);
            when(eventRepository.getEventList(testCategoryId)).thenReturn(mockEventList);

            // When
            EventDto.GetEventList.Response response = eventService.getEventList(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).hasSize(2);
            assertThat(response.getList().get(0).getEventName()).isEqualTo("정민우1 콘서트");
            assertThat(response.getList().get(1).getEventName()).isEqualTo("정민우2 콘서트");
            
            verify(queueService).validateToken(testToken);
            verify(eventRepository).getEventList(testCategoryId);
        }
    }

    @Test
    @DisplayName("이벤트 목록 조회 - 토큰 검증 실패")
    void getEventList_tokenValidationFails() {

        // Given
        EventDto.GetEventList.Request request = new EventDto.GetEventList.Request();
        request.setCategoryId(testCategoryId);

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken))
                    .thenThrow(new AppException(ErrorCode.AUTH004));

            // When & Then
            assertThatThrownBy(() -> eventService.getEventList(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH004.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(eventRepository, never()).getEventList(any());
        }
    }

    @Test
    @DisplayName("이벤트 스케줄 목록 조회 - 성공")
    void getEventSchedules_success() {

        // Given
        EventDto.GetEventScheduleList.Request request = new EventDto.GetEventScheduleList.Request();
        request.setEventId(testEventId);

        List<EventDto.GetEventScheduleInfoView> mockScheduleList = Arrays.asList(
                EventDto.GetEventScheduleInfoView.builder()
                        .eventId(testEventId)
                        .eventScheduleId(1L)
                        .categoryCode("CONCERT")
                        .categoryName("콘서트")
                        .eventCode("EVENT001")
                        .eventName("정민우1 콘서트")
                        .startDateTime(LocalDateTime.now().plusDays(1))
                        .endDateTime(LocalDateTime.now().plusDays(1).plusHours(3))
                        .totalSeats(50)
                        .availableSeats(30)
                        .reservedSeats(15)
                        .holdSeats(5)
                        .build(),
                EventDto.GetEventScheduleInfoView.builder()
                        .eventId(testEventId)
                        .eventScheduleId(2L)
                        .categoryCode("CONCERT")
                        .categoryName("콘서트")
                        .eventCode("EVENT002")
                        .eventName("정민우2 콘서트")
                        .startDateTime(LocalDateTime.now().plusDays(2))
                        .endDateTime(LocalDateTime.now().plusDays(2).plusHours(3))
                        .totalSeats(50)
                        .availableSeats(25)
                        .reservedSeats(20)
                        .holdSeats(5)
                        .build()
        );

        QueueDto.QueueTokenValidationView validationView = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(validationView);
            when(eventRepository.getEventScheduleInfo(eq(testEventId), any(LocalDateTime.class)))
                    .thenReturn(mockScheduleList);

            // When
            EventDto.GetEventScheduleList.Response response = eventService.getAvailableEventSchedules(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).hasSize(2);
            assertThat(response.getList().get(0).getAvailableSeats()).isEqualTo(30);
            assertThat(response.getList().get(1).getAvailableSeats()).isEqualTo(25);
            
            verify(queueService).validateToken(testToken);
            verify(eventRepository).getEventScheduleInfo(eq(testEventId), any(LocalDateTime.class));
        }
    }

    @Test
    @DisplayName("이벤트 스케줄 목록 조회 - 토큰 검증 실패")
    void getEventSchedules_tokenValidationFails() {

        // Given
        EventDto.GetEventScheduleList.Request request = new EventDto.GetEventScheduleList.Request();
        request.setEventId(testEventId);

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken))
                    .thenThrow(new AppException(ErrorCode.AUTH005));

            // When & Then
            assertThatThrownBy(() -> eventService.getAvailableEventSchedules(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH005.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(eventRepository, never()).getEventScheduleInfo(any(), any());
        }
    }

    @Test
    @DisplayName("이벤트 스케줄 목록 조회 - 빈 목록 반환")
    void getEventSchedules_returnEmptyList() {

        // Given
        EventDto.GetEventScheduleList.Request request = new EventDto.GetEventScheduleList.Request();
        request.setEventId(testEventId);

        QueueDto.QueueTokenValidationView validationView = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(validationView);
            when(eventRepository.getEventScheduleInfo(eq(testEventId), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // When
            EventDto.GetEventScheduleList.Response response = eventService.getAvailableEventSchedules(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).isEmpty();
            
            verify(queueService).validateToken(testToken);
            verify(eventRepository).getEventScheduleInfo(eq(testEventId), any(LocalDateTime.class));
        }
    }
}