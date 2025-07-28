# Ticketing-platform

## 소개
이 저장소는 이벤트 티켓 예매 시스템 서버 백엔드 프로젝트입니다.

## 목차
- [요구사항 분석](#요구사항-분석)
- [클래스 다이어그램](#클래스-다이어그램)
- [ERD](#erd)
- [시퀀스 다이어그램](#시퀀스-다이어그램)
- [아키텍처 패턴 및 레이어별 책임](#아키텍처-패턴-및-레이어별-책임)


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

    %% User (유저 및 잔액)
    class User {
        +UUID id
        +String userName
        +int balance
        +int getBalance() : 사용자의 현재 보유 잔액을 반환
        +void chargeBalance(int amount) : 잔액을 입력 금액만큼 충전
        +boolean deductBalance(int amount) : 잔액 차감(실패시 false)
    }

    %% QueueToken (대기열 토큰)
    class QueueToken {
        +Long id
        +UUID userId
        +String token
        +int position
        +DateTime issuedAt
        +DateTime expiresAt
        +boolean isExpired(DateTime now) : 토큰 만료 여부 반환
        +int getCurrentPosition() : 대기열 내 현재 순번 반환
    }

    %% BalanceLog (충전 및 사용 로그)
    class BalanceLog {
        +Long id
        +UUID userId
        +int amount
        +String type
        +DateTime createdAt
        +static BalanceLog createCharge(UUID userId, int amount) : 충전 로그 객체 생성
        +static BalanceLog createPayment(UUID userId, int amount) : 결제 로그 객체 생성
    }

    %% Event (공연/콘서트)
    class Event {
        +Long id
        +String name
        +String code
        +String description
        +List~EventSchedule~ getSchedules() : 이벤트의 전체 일정 리스트 반환
    }

    %% EventSchedule (공연 일정/회차)
    class EventSchedule {
        +Long id
        +Long eventId
        +LocalDateTime startDateTime
        +LocalDateTime endDateTime
        +List~Seat~ getAvailableSeats() : 예약가능 좌석 목록 반환
        +List~Seat~ getAllSeats() : 전체 좌석 목록 반환
    }

    %% Seat (좌석)
    class Seat {
        +Long id
        +Long scheduleId
        +int seatNumber
        +String seatType
        +String status
        +DateTime holdExpiresAt
        +boolean isAvailable() : 현재 예약 가능 여부 반환
        +boolean isHoldExpired(DateTime now) : 임시 배정 만료 여부 반환
        +void hold(UUID userId, DateTime expiresAt) : 사용자를 임시 배정(만료시각 포함)
        +void releaseHold() : 임시 배정 해제, 예약 가능 상태로 복구
        +void confirm(UUID userId) : 예약 확정 처리
    }

    %% Reservation (예약 내역)
    class Reservation {
        +Long id
        +UUID userId
        +Long seatId
        +Long scheduleId
        +String status
        +DateTime reservedAt
        +DateTime expiresAt
        +DateTime canceledAt
        +static Reservation createTemp(UUID userId, Long seatId, Long scheduleId, DateTime expiresAt) : 임시 예약 생성
        +void confirm() : 예약 확정
        +void cancel() : 예약 취소 혹은 만료 처리
        +boolean isExpired(DateTime now) : 만료 여부 반환
    }

    %% Payment (결제 내역)
    class Payment {
        +Long id
        +UUID userId
        +Long reservationId
        +int amount
        +String status
        +DateTime paidAt
        +DateTime canceledAt
        +static Payment create(UUID userId, Long reservationId, int amount) : 결제 내역 객체 생성
        +void complete() : 결제 완료 처리
        +void fail() : 결제 실패 처리
        +void cancel() : 결제 취소 처리
    }

    %% 관계
    User "1" -- "0..*" QueueToken : 유저가 보유한 대기열 토큰
    User "1" -- "0..*" Reservation : 유저의 예약 목록
    User "1" -- "0..*" Payment : 유저의 결제 내역
    User "1" -- "0..*" BalanceLog : 유저의 잔액 이력
    Event "1" -- "0..*" EventSchedule : 이벤트의 일정
    EventSchedule "1" -- "0..*" Seat : 일정별 좌석
    Seat "1" -- "0..1" Reservation : 좌석의 예약 상태
    Reservation "1" -- "0..1" Payment : 예약의 결제 정보
    EventSchedule "1" -- "0..*" Reservation : 일정의 예약 정보
```

## ERD
```mermaid
erDiagram
    USER {
        UUID id PK "유저 고유 식별자"
        STRING userName "유저명"
        INT balance "현재 보유 잔액"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    QUEUE_TOKEN {
        Long id PK "대기열 토큰 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        STRING token "대기열 토큰 값"
        INT position "대기열의 순번"
        DATETIME issuedAt "토큰 발급 시간"
        DATETIME expiresAt "토큰 만료 시간"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    BALANCE_LOG {
        LONG id PK "잔액 이력 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        INT amount "충전/사용 금액"
        STRING type "이력 유형 (CHARGE, PAYMENT 등)"
        DATETIME createdAt "이력 발생일시"
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
        STRING seatType "좌석 타입 (STANDARD, VIP 등)"
        STRING status "좌석상태 (AVAILABLE, HOLD, RESERVED 등)"
        DATETIME holdExpiresAt "임시 배정 만료 시각"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    RESERVATION {
        LONG id PK "예약 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        LONG seatId FK "좌석 고유 식별자"
        LONG scheduleId FK "이벤트 스케줄 고유 식별자"
        STRING status "예약 상태 (TEMP, CONFIRMED, CANCEL 등)"
        DATETIME reservedAt "예약 일시"
        DATETIME expiresAt "임시 예약 만료 시각"
        DATETIME canceledAt "취소 일시"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }
    PAYMENT {
        LONG id PK "결제 고유 식별자"
        UUID userId FK "유저 고유 식별자"
        LONG reservationId FK "예약 고유 식별자"
        INT amount "결제 금액"
        STRING status "결제상태 (COMPLETE, FAILED, CANCELED 등)"
        DATETIME paidAt "결제 일시"
        DATETIME canceledAt "결제 취소 일시"
        DATETIME insertAt "생성일시"
        DATETIME updateAt "수정일시"
    }

    USER ||--o{ QUEUE_TOKEN : "유저의 대기열 토큰"
    USER ||--o{ RESERVATION : "유저의 예약"
    USER ||--o{ PAYMENT : "유저의 결제"
    USER ||--o{ BALANCE_LOG : "유저의 잔액 이력"
    CATEGORY ||--o{ EVENT : "카테고리별 이벤트"
    EVENT ||--o{ EVENT_SCHEDULE : "이벤트별 일정"
    EVENT_SCHEDULE ||--o{ SEAT : "일정별 좌석"
    SEAT ||--o| RESERVATION : "좌석의 예약"
    RESERVATION ||--o| PAYMENT : "예약의 결제"
    EVENT_SCHEDULE ||--o{ RESERVATION : "일정의 예약"
```

## 시퀀스 다이어그램
1. 대기열 토큰 발급 및 대기 순번 조회
```mermaid
sequenceDiagram
    participant User
    participant QueueService
    participant QueueTokenRepository

    User->>QueueService: 대기열 진입 요청
    QueueService->>QueueTokenRepository: 대기열 토큰 생성
    QueueTokenRepository-->>QueueService: 생성된 토큰/정보 반환
    QueueService-->>User: 대기열 토큰 및 순번 반환

    User->>QueueService: 대기열 순번 조회 요청
    QueueService->>QueueTokenRepository: 토큰/순번 정보 조회
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

    User->>EventService: 예약 가능 날짜 목록 조회(토큰 인증)
    EventService->>EventScheduleRepository: 활성 일정 전체 조회
    EventScheduleRepository-->>EventService: 일정 목록 반환
    EventService-->>User: 예약 가능 일정 반환

    User->>EventService: 특정 일정의 좌석 정보 조회(토큰 인증)
    EventService->>SeatRepository: 좌석 목록 및 상태 조회
    SeatRepository-->>EventService: 좌석 정보 반환
    EventService-->>User: 좌석 정보 반환
```

3. 좌석 예약 요청 및 임시 배정
```mermaid
sequenceDiagram
    participant User
    participant ReservationService
    participant SeatRepository
    participant ReservationRepository

    User->>ReservationService: 좌석 예약 요청(토큰, 일정, 좌석)
    ReservationService->>SeatRepository: 좌석 상태 Lock/검증
    SeatRepository-->>ReservationService: 좌석 사용 가능/불가 응답
    ReservationService->>ReservationRepository: 임시 예약 생성(만료시각 포함)
    ReservationRepository-->>ReservationService: 임시 예약 정보 반환
    ReservationService->>SeatRepository: 좌석 상태 HOLD 및 holdExpiresAt 설정
    ReservationService-->>User: 임시 예약 성공/실패 알림
```

4. 임시 예약 만료 및 해제
```mermaid
sequenceDiagram
    participant Scheduler
    participant ReservationRepository
    participant SeatRepository

    Scheduler->>ReservationRepository: 임시 예약 만료 체크(스케줄러 주기)
    ReservationRepository->>SeatRepository: HOLD 좌석 해제
    ReservationRepository->>ReservationRepository: 임시 예약 상태 CANCEL 처리
```

5. 잔액 충전/조회
```mermaid
sequenceDiagram
    participant User
    participant UserService
    participant UserRepository
    participant BalanceLogRepository

    User->>UserService: 잔액 충전 요청(금액)
    UserService->>UserRepository: 잔액 증가 처리
    UserRepository-->>UserService: 업데이트 결과 반환
    UserService->>BalanceLogRepository: 충전 로그 기록
    UserService-->>User: 충전 결과 반환

    User->>UserService: 잔액 조회 요청
    UserService->>UserRepository: 잔액 정보 조회
    UserRepository-->>UserService: 잔액 반환
    UserService-->>User: 잔액 반환
```

6. 결제 및 예약 확정
```mermaid
sequenceDiagram
    participant User
    participant PaymentService
    participant ReservationRepository
    participant PaymentRepository
    participant UserRepository
    participant SeatRepository
    participant QueueTokenRepository

    User->>PaymentService: 결제 요청(예약ID, 토큰)
    PaymentService->>ReservationRepository: 예약 유효성/만료 체크
    ReservationRepository-->>PaymentService: 예약 상태 반환
    PaymentService->>UserRepository: 잔액 차감 요청
    UserRepository-->>PaymentService: 잔액 차감 결과 반환
    PaymentService->>PaymentRepository: 결제 내역 기록
    PaymentRepository-->>PaymentService: 결제 정보 반환
    PaymentService->>ReservationRepository: 예약 상태 CONFIRMED 처리
    PaymentService->>SeatRepository: 좌석 RESERVED 처리
    PaymentService->>QueueTokenRepository: 토큰 만료 처리(결제 완료 후)
    PaymentService-->>User: 결제 결과 및 상태 반환
```

## 아키텍처 패턴 및 레이어별 책임

### 선택한 아키텍처 패턴: Layered Architecture (계층형 아키텍처)

### 레이어별 책임 정의

#### 1. Presentation Layer (API Layer)
**위치**: `src/main/java/kr/hhplus/be/server/api/*/controller`

**책임**:
- HTTP 요청/응답 처리
- 요청 데이터 검증 및 변환
- 응답 데이터 포맷팅
- 예외 처리 및 에러 응답 생성

**주요 구성요소**:
- `*Controller` 클래스들
- `ApiResponse` 공통 응답 모델

**제약사항**:
- 비즈니스 로직을 포함하지 않음
- 데이터베이스에 직접 접근하지 않음
- Application Layer의 Service만 의존

#### 2. Application Layer (Service Layer)
**위치**: `src/main/java/kr/hhplus/be/server/api/*/service`

**책임**:
- 비즈니스 유스케이스 구현
- 트랜잭션 관리
- 도메인 객체 간의 협력 조정
- 외부 시스템과의 통합
- 보안 및 인증 처리

**주요 구성요소**:
- `QueueService`: 대기열 토큰 관리
- `EventService`: 이벤트 및 스케줄 조회
- `SeatService`: 좌석 정보 관리
- `ReservationService`: 예약 생성, 취소, 확정
- `UserService`: 사용자 및 잔액 관리
- `PaymentService`: 결제 처리
- `BalanceLogService`: 잔액 이력 관리

**제약사항**:
- UI/웹 관련 로직을 포함하지 않음
- 데이터베이스 스키마에 직접 의존하지 않음
- Domain Layer의 Repository 인터페이스만 의존

#### 3. Domain Layer
**위치**: `src/main/java/kr/hhplus/be/server/domain`

**책임**:
- 핵심 비즈니스 규칙 및 로직 구현
- 도메인 모델 정의
- 데이터 접근 인터페이스 정의
- 비즈니스 불변식 보장

**주요 구성요소**:

**3-1. Entities** (`domain/*/entity`)
- `User`: 사용자 정보 및 잔액 관리
- `QueueToken`: 대기열 토큰 정보
- `Event`, `EventSchedule`: 이벤트 및 일정 정보
- `Seat`: 좌석 정보 및 상태 관리
- `Reservation`: 예약 정보 및 상태 관리
- `Payment`: 결제 정보
- `BalanceLog`: 잔액 변동 이력

**3-2. Value Objects** (`domain/*/vo`)
- `TokenStatusEnums`: 토큰 상태 정의
- `SeatStatusEnums`: 좌석 상태 정의
- `ReservationStatusEnums`: 예약 상태 정의
- `PaymentStatusEnums`: 결제 상태 정의
- `BalanceActionEnums`: 잔액 변동 유형 정의

**3-3. DTOs** (`domain/*/dto`)
- 레이어 간 데이터 전송을 위한 객체들
- 요청/응답 모델 정의

**3-4. Repository Interfaces** (`domain/*/repository`)
- 데이터 접근 계약 정의
- 도메인 중심의 쿼리 메서드 정의

**제약사항**:
- 외부 프레임워크에 의존하지 않음
- 상위 레이어를 참조하지 않음
- 순수한 비즈니스 로직만 포함

#### 4. Infrastructure Layer
**위치**: `src/main/java/kr/hhplus/be/server`

**책임**:
- 외부 시스템과의 연동
- 데이터베이스 구현체 제공
- 설정 및 구성 관리
- 기술적 관심사 처리

**주요 구성요소**:

**4-1. Repository Implementations** (`domain/*/repository/*Impl`)
- JPA/QueryDSL을 이용한 데이터 접근 구현
- 복잡한 쿼리 로직 구현

**4-2. Configuration** (`config/`)
- `JpaConfig`: JPA 설정
- `QuerydslConfig`: QueryDSL 설정

**4-3. Common Components** (`common/`)
- `CommonUtil`: 공통 유틸리티
- `AppException`, `ErrorCode`: 예외 처리
- `ApiResponse`: 공통 응답 모델

**4-4. Scheduler** (`scheduler/`)
- `TokenAndReservationScheduler`: 토큰 및 예약 만료 처리

**제약사항**:
- 비즈니스 로직을 포함하지 않음
- Domain Layer의 인터페이스를 구현
- 기술적 세부사항만 담당
