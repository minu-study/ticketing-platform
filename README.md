# Ticketing-platform

## 소개
이 저장소는 이벤트 티켓 예매 시스템 서버 백엔드 프로젝트입니다.

## 목차
- [요구사항 분석](#요구사항-분석)
- [클래스 다이어그램](#클래스-다이어그램)
- [ERD](#erd)
- [시퀀스 다이어그램](#시퀀스-다이어그램)


## 요구사항 분석
### **1.1. 유저 대기열 토큰 관리**

- **토큰 발급 API**
    - 사용자는 서비스 이용 전 대기열 토큰을 발급받아야 한다.
    - 토큰에는 사용자 UUID, 대기 순서, 발급/만료 시간 등 정보가 포함된다.
- **대기번호 조회 API**
    - 사용자 토큰을 통해 자신의 대기 순서를 확인할 수 있다.
    - 모든 서비스 API는 유효한 토큰이 있어야 접근 가능하다.
- **대기열 정책**
    - 대기열은 FIFO(First-In-First-Out)로 동작한다.
    - 대기열 상태는 폴링 방식으로 확인한다.
        - 폴링 방식 말고 더 나은 방식있다면 대체할 수도 있다.

### **1.2. 예약 가능 날짜/좌석 조회**

- **예약 가능 날짜 조회 API**
    - 예약 가능한 콘서트 날짜 리스트를 반환한다.
    - 날짜별로 예약 가능한 좌석 수, 잔여 좌석 정보 포함
- **예약 가능 좌석 조회 API**
    - 특정 날짜를 입력받아 해당 날짜의 예약 가능한 좌석 번호들을 반환한다.
    - 좌석별 상태(예약 가능, 예약 불가)를 함께 제공한다.

### **1.3. 좌석 예약 요청 및 임시 배정**

- **좌석 예약 요청 API**
    - 사용자, 날짜, 좌석 번호, 토큰 정보를 입력받아 예약 요청을 처리한다.
    - 좌석은 예약 요청 시점부터 약 5분(정책에 따라 조정 가능)간 임시 배정된다.
    - 임시 배정 시간 내 결제가 완료되지 않으면 자동으로 임시 배정 해제.
    - 임시 배정 중인 좌석은 다른 사용자가 예약할 수 없다.
    - 동시성 이슈 방지를 위해 분산락, DB 트랜잭션 락
- **임시 배정 상태 관리**
    - 임시 배정 상태, 시작/만료 시간, 해당 사용자 정보 저장.
    - 임시 배정 만료 시 자동 해제(스케줄러, 만료 체크 등 필요).

### **1.4. 잔액 충전 및 조회**

- **잔액 충전 API**
    - 사용자 식별자와 충전 금액을 입력받아 잔액을 충전한다.
    - 충전 내역은 이력으로 저장한다.
- **잔액 조회 API**
    - 사용자 식별자를 입력받아 현재 잔액을 조회한다.

### **1.5. 결제 및 예약 확정**

- **결제 API**
    - 예약 ID, 토큰을 입력받아 결제 요청을 처리한다.
    - 결제 시 사용자 잔액에서 금액 차감, 결제 내역 생성.
    - 결제 완료 시 좌석 예약이 확정되고, 임시 배정이 해제된다.
    - 결제 실패 시(잔액 부족 등) 적절한 오류 반환.
    - 결제 완료 시 대기열 토큰 만료 처리.
- **결제 내역 관리**
    - 결제 금액, 결제 시간, 결제 상태(성공/실패) 등 저장.

### **1.6. 동시성 및 대기열 정책**

- 여러 사용자가 동시에 사용하더라도 좌석 중복 예약, 대기열 순서 꼬임 등이 발생하지 않도록 동시성 제어 필수
- 분산 환경에서의 데이터 일관성 확보를 위한 설계 필요

### **2. 비즈니스 규칙 및 정책**

- **좌석 번호:** 1~50번, 날짜별로 관리
- **임시 배정 시간:** 기본 5분(정책에 따라 조정 가능)
- **대기열 정책:** 선착순, 토큰 만료 시 삭제 및 무시
- **잔액 부족 시:** 결제 불가, 임시 배정 해제
- **결제 완료 시:** 좌석 소유권 사용자에게 귀속, 대기열 토큰 만료

### **3. 예외 및 에러 처리**

- 유효하지 않은 토큰, 만료된 토큰 접근 시: 인증 에러 반환
- 좌석 임시 배정 중복 요청 시: 예약 불가 에러 반환
- 잔액 부족 시: 결제 실패 에러 반환
- 임시 배정 만료 후 결제 시도: 예약 만료 에러 반환
- 기타 예상치 못한 시스템 오류: 공통 에러 코드 및 메시지 정의

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
