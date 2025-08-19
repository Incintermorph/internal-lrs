# Internal LRS 코드베이스 분석 보고서

## 1. 프로젝트 개요

### 1.1 기본 정보
- **프로젝트명**: Internal LRS (Learning Record Store)
- **버전**: 0.2.0
- **표준**: xAPI 1.0.3 준수
- **기술 스택**: Spring Boot 3.3.2 + MyBatis 3.0.3 + MySQL 8
- **Java 버전**: 17

### 1.2 주요 기능
- xAPI Statement 저장/조회 (`/xapi/statements`)
- Activity State 관리 (`/xapi/activities/state`)
- Basic Authentication 인증
- ETag 기반 조건부 요청 지원
- 실시간 로그 모니터링 (`/admin/logs/*`)
- 상세한 에러 로깅 및 추적

## 2. 아키텍처 분석

### 2.1 전체 구조
```
./
├── src/main/java/com/example/lrs/
│   ├── LrsApplication.java          # Spring Boot 메인 클래스
│   ├── api/                         # REST 컨트롤러 계층
│   │   ├── StatementsController.java
│   │   ├── ActivitiesStateController.java
│   │   ├── HealthController.java
│   │   └── LogController.java       # 로그 모니터링 API
│   ├── service/                     # 비즈니스 로직 계층
│   │   ├── StatementService.java
│   │   └── DocumentService.java
│   ├── mapper/                      # 데이터 접근 계층 (MyBatis)
│   │   ├── StatementMapper.java
│   │   └── DocumentMapper.java
│   ├── domain/                      # 도메인 모델
│   │   └── Statement.java
│   ├── config/                      # 설정 및 필터
│   │   ├── BasicAuthFilter.java
│   │   └── GlobalExceptionHandler.java
│   └── util/                        # 유틸리티 클래스
│       ├── JsonUtil.java
│       └── HashUtil.java
├── src/main/resources/
│   ├── application.yml              # 애플리케이션 설정
│   └── mappers/                     # MyBatis XML 매퍼
├── logs/                            # 로그 파일 디렉토리
├── db/                              # 데이터베이스 스키마
└── target/                          # 빌드 결과물
```

### 2.2 계층별 역할

#### 2.2.1 API 계층 (Controllers)
- **StatementsController**: xAPI Statement CRUD 작업
  - `POST /xapi/statements`: Statement 생성
  - `PUT /xapi/statements?statementId=UUID`: Statement 멱등 저장
  - `GET /xapi/statements`: Statement 조회 (필터링 지원)
  - `GET /xapi/statements?statementId=UUID`: 특정 Statement 조회

- **ActivitiesStateController**: Activity State 관리
  - `PUT/POST /xapi/activities/state`: State 저장/업데이트
  - `GET /xapi/activities/state`: State 조회
  - `DELETE /xapi/activities/state`: State 삭제

- **LogController**: 로그 모니터링 API
  - `GET /admin/logs/tail`: 로그 tail 조회
  - `GET /admin/logs/search`: 로그 검색
  - `GET /admin/logs/stats`: 로그 통계
  - `POST /admin/logs/test`: 테스트 로그 생성

#### 2.2.2 서비스 계층 (Business Logic)
- **StatementService**: Statement 관련 비즈니스 로직
  - JSON 파싱 및 검증
  - Statement ID 자동 생성
  - 중복 Statement 처리
  - 조회 필터링 로직

- **DocumentService**: Document(State) 관리 로직
  - ETag 기반 조건부 처리
  - Agent SHA256 해싱
  - 문서 CRUD 작업

#### 2.2.3 데이터 접근 계층 (MyBatis Mappers)
- **StatementMapper**: Statement 테이블 접근
- **DocumentMapper**: Document 테이블 접근
- XML 매퍼 파일로 SQL 쿼리 정의

## 3. 데이터베이스 설계

### 3.1 테이블 구조

#### 3.1.1 lrs_statement 테이블
```sql
- id: CHAR(36) PRIMARY KEY (Statement UUID)
- stored: TIMESTAMP(6) (저장 시간)
- timestamp_utc: TIMESTAMP(6) (Statement 타임스탬프)
- authority, actor, verb, object, result, context, attachments: JSON 컬럼
- full_statement: JSON (전체 Statement)
- voided: TINYINT(1) (무효화 여부)
- Generated Columns (인덱싱용):
  - verb_id: VARCHAR(500)
  - actor_account_name: VARCHAR(255)
  - activity_id: VARCHAR(1000)
  - registration: CHAR(36)
```

#### 3.1.2 lrs_document 테이블
```sql
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- doc_type: ENUM('state','activity_profile','agent_profile')
- activity_id: VARCHAR(1000)
- agent: JSON
- agent_sha: CHAR(64) (Agent JSON의 SHA256 해시)
- registration: CHAR(36)
- state_id: VARCHAR(200)
- profile_id: VARCHAR(200)
- content_type: VARCHAR(255)
- content: LONGBLOB
- etag: CHAR(64)
- updated: TIMESTAMP(6)
```

### 3.2 인덱스 전략
- Statement 테이블: stored, verb_id, actor_account_name, activity_id, registration
- Document 테이블: 복합 UNIQUE 키로 중복 방지

## 4. 주요 기능 분석

### 4.1 인증 및 보안
- **BasicAuthFilter**: 모든 `/xapi/*` 요청에 대해 Basic Auth 검증
- **X-Experience-API-Version** 헤더 필수 (1.0.x)
- 설정 가능한 사용자명/비밀번호 (기본값: admin/admin)
- 상세한 보안 로깅 및 감사 추적

### 4.2 Statement 처리
- JSON 파싱 및 필수 필드 검증 (actor, verb, object)
- Statement ID 자동 생성 (UUID)
- 멱등성 보장 (동일 ID로 다른 내용 저장 시 에러)
- 조회 필터링: since, verb, activity, registration, limit

### 4.3 State 관리
- ETag 기반 조건부 요청 (If-Match, If-None-Match)
- Agent JSON의 SHA256 해시로 식별
- 바이너리 콘텐츠 지원
- UPSERT 패턴으로 생성/업데이트

### 4.4 에러 처리 및 로깅
- **GlobalExceptionHandler**로 중앙집중식 예외 처리
- ETag 관련 에러: 412 Precondition Failed
- 기타 상태 에러: 409 Conflict
- 일반 에러: 500 Internal Server Error
- **상세한 로깅 시스템**:
  - 모든 API 요청/응답 로깅
  - 데이터베이스 쿼리 로깅
  - 에러 스택 트레이스 로깅
  - 성능 메트릭 로깅
- **로그 파일 관리**: `./logs/application.log`
- **실시간 로그 모니터링 API** 제공

## 5. 기술적 특징

### 5.1 MySQL 8 활용
- 원격 MySQL 서버 연결 (43.201.31.215:3306)
- UTF8MB4 문자셋으로 완전한 Unicode 지원
- 연결 풀 최적화 (HikariCP)
- 쿼리 성능 모니터링

### 5.2 Spring Boot 3.x 활용
- Jakarta EE 네임스페이스 사용
- 최신 Spring 기능 활용
- Auto-configuration 활용

### 5.3 MyBatis 통합
- XML 매퍼로 복잡한 SQL 관리
- 동적 쿼리 지원
- 타입 안전성 보장

## 6. 확장 계획 (TODO)

### 6.1 미구현 기능
- `/xapi/activities/profile` 엔드포인트
- `/xapi/agents/profile` 엔드포인트
- Statement GET 고급 필터링
- `more` 커서 기반 페이징
- Attachments (multipart/mixed) 지원

### 6.2 개선 사항
- OAuth2 인증 지원
- 시간 기반 파티셔닝
- 데이터 보관 정책
- 에러 로깅 및 모니터링 강화

## 7. 코드 품질 평가

### 7.1 장점
- 명확한 계층 분리
- xAPI 표준 준수
- 적절한 에러 처리
- 데이터베이스 설계 최적화
- 코드 가독성 양호
- **상세한 로깅 시스템 구축**
- **실시간 로그 모니터링 API**
- **성능 최적화된 서버 설정**

### 7.2 개선 가능 영역
- 단위 테스트 부재
- API 문서화 부족 (Swagger/OpenAPI)
- 보안 강화 (OAuth2, JWT)
- 캐싱 시스템 도입
- 메트릭 수집 및 대시보드

## 8. 배포 및 운영

### 8.1 실행 방법
```bash
# 현재 디렉토리에서 실행
mvn clean compile
mvn spring-boot:run

# 또는 JAR 파일로 실행
mvn clean package
java -jar target/internal-lrs-0.2.0.jar
```

### 8.2 설정 파일
- `src/main/resources/application.yml`:
  - 데이터베이스 연결 (MySQL 원격 서버)
  - MyBatis 설정
  - 인증 정보
  - 로깅 설정
  - 서버 최적화 설정
- 포트: 8080 (기본값)
- 타임아웃: 5초
- 로그 파일: `./logs/application.log`

### 8.3 모니터링
- 실시간 로그 확인: `tail -f logs/application.log`
- 로그 API: http://localhost:8080/admin/logs/tail
- 헬스체크: http://localhost:8080/health

이 분석 보고서는 Internal LRS 코드베이스의 전체적인 구조와 주요 기능을 포괄적으로 다루고 있습니다.
