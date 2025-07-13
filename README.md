# Ticketing-platform

## 소개
이 저장소는 이벤트 티켓 예매 시스템 서버 백엔드 프로젝트입니다.

## 목차
- [클래스 다이어그램](#클래스-다이어그램)
- [ERD](#erd)
- [시퀀스 다이어그램](#시퀀스-다이어그램)


## 클래스 다이어그램

```mermaid
classDiagram
    direction TB
    %% 유저 정보와 잔액을 관리하는 클래스
    class User {
        +UUID id : User 고유 식별자
        +String userName : 유저명
        +int balance : 현재 보유 잔액
        +getBalance(id) : 유저의 잔액을 조회하는 메서드
        +chargeBalance(id, amount) : 유저의 잔액을 충전하는 메서드
    }
    %% 대기열 토큰을 관리하는 클래스
    class QueueToken {
        +String token : 대기열 토큰 값
        +UUID userId : 유저 고유 식별자
        +int position : 대기열의 순번
        +DateTime issuedAt : 토큰 발급 시간
        +DateTime expiresAt : 토큰 만료 시간
        +boolean isExpired(DateTime now) : 주어진 시간 기준으로 만료여부 반환
    }
    %% 이벤트 카테고리(콘서트, 강연, 스포츠 등)를 관리하는 클래스
    class Category {
        +Long id : 카테고리 고유 식별자
        +String code : 카테고리 코드
        +String name : 카테고리명, 예: CONCERT
    }
    %% 실제 이벤트(공연, 경기 등)를 관리하는 클래스
    class Event {
        +Long id : 이벤트 고유 식별자
        +Long categoryId : 카테고리 고유 식별자
        +String code : 이벤트 코드, 예: WORLDMUSIC20250713
        +String name : 이벤트명
        +String description : 이벤트 설명
        +Boolean enable : 이벤트 활성 여부
    }
    %% 이벤트의 일정(회차, 날짜/시간)을 관리하는 클래스
    class EventSchedule {
        +Long id : 이벤트 일정 고유 식별자
        +Long eventId : 이벤트 고유 식별자
        +LocalDateTime startDateTime : 이벤트 시작 일시
        +LocalDateTime endDateTime : 이벤트 마감 일시
    }
    %% 좌석 정보를 관리하는 클래스
    class Seat {
        +Long id : 좌석 고유 식별자
        +Long scheduleId : 일정 고유 식별자
        +int seatNumber : 좌석번호
        +String seatType : 좌석 타입, 예: STANDARD, VIP, STANDING 등
        +String status : 좌석상태, 예: 예약 가능, 예약 대기, 예약 불가
    }
    %% 좌석 예약 내역을 관리하는 클래스
    class Reservation {
        +Long id : 예약 고유 식별자 
        +UUID userId : 유저 고유 식별자
        +Long seatId : 좌석 고유 식별자
        +Long scheduleId : 이벤트 스케줄 고유 식별자
        +String status: 예약 상태, 예: 대기, 확정, 취소
        +DateTime reservedAt : 예약 일시
        +DateTime canceledAt : 취소 일시 
    }
    %% 결제 내역을 관리하는 클래스
    class Payment {
        +Long id : 결제 고유 식별자
        +UUID userId : 유저 고유 식별자
        +Long reservationId : 예약 고유 식별자
        +int amount : 구매금액
        +String status : 구매상태, 예: 결제완료, 결제대기, 결제취소
        +DateTime paidAt : 구매일시
        +DateTime canceledAt :취소 일시
    }
    User "1" -- "0..*" QueueToken : has
    User "1" -- "0..*" Reservation : makes
    User "1" -- "0..*" Payment : pays
    Category "1" -- "0..*" Event : has
    Event "1" -- "0..*" EventSchedule : has
    EventSchedule "1" -- "0..*" Seat : includes
    Seat "1" -- "0..1" Reservation : reserved_by
    Reservation "1" -- "0..1" Payment : paid_by
    Event "1" -- "0..*" Reservation : for
```

## ERD
```mermaid
erDiagram
    USER {
        UUID id PK "User 고유 식별자"
        STRING userName "유저명"
        INT balance "현재 보유 잔액"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    QUEUE_TOKEN {
        STRING token PK "대기열 토큰 값"
        UUID userId FK "유저 고유 식별자"
        INT position "대기열의 순번"
        DATETIME issuedAt "토큰 발급 시간"
        DATETIME expiresAt "토큰 만료 시간"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    CATEGORY {
        LONG id PK "카테고리 고유 식별자"
        STRING code "카테고리 코드"
        STRING name "카테고리명"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    EVENT {
        LONG id PK "이벤트 고유 식별자"
        LONG categoryId FK "카테고리 고유 식별자"
        STRING code "이벤트 코드"
        STRING name "이벤트명"
        STRING description "이벤트 설명"
        BOOLEAN enable "이벤트 활성 여부"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    EVENT_SCHEDULE {
        LONG id PK "이벤트 일정 고유 식별자"
        LONG eventId FK "이벤트 고유 식별자"
        DATETIME startDateTime "이벤트 시작 일시"
        DATETIME endDateTime "이벤트 마감 일시"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    SEAT {
        LONG id PK "좌석 고유 식별자"
        LONG scheduleId FK "일정 고유 식별자"
        INT seatNumber "좌석번호"
        STRING seatType "좌석 타입"
        STRING status "좌석상태"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    RESERVATION {
        LONG id PK "예약 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        LONG seatId FK "좌석 고유 식별자"
        LONG scheduleId FK "이벤트 스케줄 고유 식별자"
        STRING status "예약 상태"
        DATETIME reservedAt "예약 일시"
        DATETIME canceledAt "취소 일시"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    PAYMENT {
        LONG id PK "결제 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        LONG reservationId FK "예약 고유 식별자"
        INT amount "구매금액"
        STRING status "구매상태"
        DATETIME paidAt "구매일시"
        DATETIME canceledAt "취소 일시"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }

    USER ||--o{ QUEUE_TOKEN : "has"
    USER ||--o{ RESERVATION : "makes"
    USER ||--o{ PAYMENT : "pays"
    CATEGORY ||--o{ EVENT : "has"
    EVENT ||--o{ EVENT_SCHEDULE : "has"
    EVENT_SCHEDULE ||--o{ SEAT : "includes"
    SEAT ||--o| RESERVATION : "reserved_by"
    RESERVATION ||--o| PAYMENT : "paid_by"
    EVENT ||--o{ RESERVATION : "for"
```

## 시퀀스 다이어그램
1. 대기열 토큰 발급 및 대기열 조회
```mermaid
sequenceDiagram
    participant User
    participant QueueService
    participant QueueTokenRepository

    User->>QueueService: 대기열 진입 요청
    QueueService->>QueueTokenRepository: 대기열 토큰 생성
    QueueTokenRepository-->>QueueService: 토큰 정보 반환
    QueueService-->>User: 대기열 토큰/순번 반환

    User->>QueueService: 대기열 순번 조회
    QueueService->>QueueTokenRepository: 토큰 정보 조회
    QueueTokenRepository-->>QueueService: 토큰/순번 반환
    QueueService-->>User: 대기열 상태 반환
```

2. 예약 가능 날짜/좌석 조회
```mermaid
sequenceDiagram
    participant User
    participant EventService
    participant EventScheduleRepository
    participant SeatRepository

    User->>EventService: 예약 가능 날짜 목록 조회
    EventService->>EventScheduleRepository: 활성화된 일정 조회
    EventScheduleRepository-->>EventService: 일정 목록 반환
    EventService-->>User: 일정 목록 반환

    User->>EventService: 특정 일정의 좌석 정보 조회
    EventService->>SeatRepository: 좌석 목록 및 상태 조회
    SeatRepository-->>EventService: 좌석 정보 반환
    EventService-->>User: 좌석 정보 반환
```

3. 좌석 예약 요청
```mermaid
sequenceDiagram
    participant User
    participant ReservationService
    participant SeatRepository
    participant ReservationRepository

    User->>ReservationService: 좌석 예약 요청(일정, 좌석)
    ReservationService->>SeatRepository: 좌석 상태 확인 및 Lock
    SeatRepository-->>ReservationService: 좌석 사용 가능 여부 반환
    ReservationService->>ReservationRepository: 임시 예약 생성
    ReservationRepository-->>ReservationService: 예약 정보 반환
    ReservationService-->>User: 예약 성공/실패 응답
```
4. 잔액 충전/조회
```mermaid
sequenceDiagram
    participant User
    participant UserService
    participant UserRepository

    User->>UserService: 잔액 충전 요청(금액)
    UserService->>UserRepository: 잔액 증가 처리
    UserRepository-->>UserService: 업데이트 결과 반환
    UserService-->>User: 충전 결과 반환

    User->>UserService: 잔액 조회 요청
    UserService->>UserRepository: 잔액 조회
    UserRepository-->>UserService: 잔액 반환
    UserService-->>User: 잔액 반환
```

5. 결제 처리
```mermaid
sequenceDiagram
    participant User
    participant PaymentService
    participant ReservationRepository
    participant PaymentRepository
    participant UserRepository

    User->>PaymentService: 결제 요청(예약ID, 금액)
    PaymentService->>ReservationRepository: 예약 상태/만료 확인
    ReservationRepository-->>PaymentService: 예약 유효성 반환
    PaymentService->>UserRepository: 잔액 차감
    UserRepository-->>PaymentService: 잔액 차감 결과 반환
    PaymentService->>PaymentRepository: 결제 내역 생성
    PaymentRepository-->>PaymentService: 결제 정보 반환
    PaymentService->>ReservationRepository: 예약 상태 확정 처리
    PaymentService-->>User: 결제 성공/실패 응답
```
