# Internal LRS 설정 및 실행 가이드

## 1. 시스템 요구사항

### 1.1 필수 소프트웨어
- **Java 17** 이상
- **Maven 3.6** 이상
- **MySQL 8.0** (운영 환경) 또는 **H2 Database** (개발/테스트 환경)

### 1.2 권장 환경
- **메모리**: 최소 2GB RAM
- **디스크**: 최소 1GB 여유 공간
- **OS**: Windows 10/11, macOS, Linux

## 2. 프로젝트 구조

```
internal-lrs-full/
├── src/main/java/com/example/lrs/
│   ├── LrsApplication.java          # Spring Boot 메인 클래스
│   ├── api/                         # REST API 컨트롤러
│   │   ├── HealthController.java    # 헬스체크 API
│   │   ├── StatementsController.java # xAPI Statement API (현재 비활성화)
│   │   └── ActivitiesStateController.java # Activity State API (현재 비활성화)
│   ├── service/                     # 비즈니스 로직
│   ├── mapper/                      # MyBatis 매퍼 인터페이스
│   ├── domain/                      # 도메인 모델
│   ├── config/                      # 설정 클래스
│   └── util/                        # 유틸리티 클래스
├── src/main/resources/
│   ├── application.yml              # 애플리케이션 설정
│   ├── schema-h2.sql               # H2 데이터베이스 스키마
│   └── mappers/                    # MyBatis XML 매퍼 (현재 비활성화)
├── db/schema.sql                   # MySQL 데이터베이스 스키마
├── pom.xml                         # Maven 의존성 설정
└── README.md                       # 프로젝트 설명
```

## 3. 빠른 시작 (MySQL 데이터베이스 사용)

### 3.1 현재 디렉토리 확인
```bash
# 현재 디렉토리가 프로젝트 루트인지 확인
pwd
ls -la  # Windows에서는 dir
```

현재 디렉토리에 다음 파일들이 있어야 합니다:
- `pom.xml`
- `src/` 디렉토리
- `db/` 디렉토리
- `README.md`

### 3.2 컴파일 및 실행
```bash
# 프로젝트 컴파일
mvn clean compile

# 애플리케이션 실행
mvn spring-boot:run
```

### 3.3 실행 확인
애플리케이션이 성공적으로 시작되면 다음과 같은 메시지가 표시됩니다:
```
Started LrsApplication in X.XXX seconds
Tomcat started on port 8080 (http)
H2 console available at '/h2-console'
```

### 3.4 접속 테스트
브라우저에서 다음 URL들을 확인하세요:

- **메인 페이지**: http://localhost:8080
- **헬스체크**: http://localhost:8080/health
- **로그 확인**: http://localhost:8080/admin/logs/tail
- **로그 통계**: http://localhost:8080/admin/logs/stats

## 4. 로그 모니터링

### 4.1 로그 파일 위치
로그 파일은 다음 위치에 생성됩니다:
```
./logs/application.log
```

### 4.2 로그 API 엔드포인트
- **로그 tail**: `GET /admin/logs/tail?lines=100&level=INFO`
- **로그 검색**: `GET /admin/logs/search?query=ERROR&maxResults=50`
- **로그 통계**: `GET /admin/logs/stats`
- **테스트 로그**: `POST /admin/logs/test?level=INFO&message=test`

### 4.3 로그 레벨 설정
`src/main/resources/application.yml`에서 로그 레벨을 조정할 수 있습니다:
```yaml
logging:
  level:
    "[com.example.lrs]": DEBUG  # 애플리케이션 로그
    "[org.springframework.web]": DEBUG  # Spring Web 로그
    "[org.mybatis]": DEBUG  # MyBatis 로그
```

## 5. MySQL 데이터베이스 설정 (현재 설정)

### 5.1 현재 데이터베이스 설정
현재 애플리케이션은 다음 MySQL 데이터베이스에 연결되어 있습니다:
- **호스트**: 43.201.31.215:3306
- **데이터베이스**: test
- **사용자**: nidsuser

### 5.2 스키마 확인
데이터베이스 스키마는 `db/schema.sql` 파일에 정의되어 있습니다:
```bash
# 스키마 파일 확인
cat db/schema.sql

# MySQL에 직접 연결하여 테이블 확인
mysql -h 43.201.31.215 -u nidsuser -p test
```

### 5.3 현재 application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://43.201.31.215:3306/test?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
    username: nidsuser
    password: "@intermorph1!!"
    driver-class-name: com.mysql.cj.jdbc.Driver

  # 서버 최적화 설정
  tomcat:
    uri-encoding: UTF-8
    threads:
      max: 200
      min-spare: 10
    accept-count: 100

# MyBatis 설정
mybatis:
  mapper-locations: classpath:/mappers/*.xml
  configuration:
    map-underscore-to-camel-case: true
    default-statement-timeout: 5
```

## 6. xAPI 기능 (현재 활성화됨)

xAPI Statement와 Activity State API가 현재 활성화되어 있습니다.

### 6.1 활성화된 컨트롤러
- `StatementsController.java` - xAPI Statement 관리
- `ActivitiesStateController.java` - Activity State 관리
- `LogController.java` - 로그 모니터링

### 6.2 MyBatis 매퍼 파일
다음 매퍼 파일들이 활성화되어 있습니다:
- `src/main/resources/mappers/StatementMapper.xml`
- `src/main/resources/mappers/DocumentMapper.xml`

### 6.3 현재 디렉토리 구조
```
./
├── src/main/java/com/example/lrs/
│   ├── api/                    # REST API 컨트롤러
│   │   ├── StatementsController.java
│   │   ├── ActivitiesStateController.java
│   │   ├── HealthController.java
│   │   └── LogController.java
│   ├── service/               # 비즈니스 로직
│   ├── mapper/               # MyBatis 매퍼 인터페이스
│   └── config/               # 설정 클래스
├── src/main/resources/
│   ├── application.yml       # 애플리케이션 설정
│   └── mappers/             # MyBatis XML 매퍼
├── logs/                    # 로그 파일
├── db/                     # 데이터베이스 스키마
└── target/                 # 빌드 결과물
```

## 7. API 엔드포인트

### 7.1 현재 활성화된 엔드포인트
- `GET /` - 메인 페이지 (애플리케이션 정보)
- `GET /health` - 헬스체크

### 7.2 xAPI 엔드포인트 (현재 사용 가능)
- `POST /xapi/statements` - Statement 생성
- `PUT /xapi/statements?statementId=UUID` - Statement 업데이트
- `GET /xapi/statements` - Statement 조회
- `GET /xapi/statements?statementId=UUID` - 특정 Statement 조회
- `PUT/POST /xapi/activities/state` - Activity State 관리
- `GET /xapi/activities/state` - Activity State 조회
- `DELETE /xapi/activities/state` - Activity State 삭제

### 7.3 관리자 엔드포인트
- `GET /admin/logs/tail` - 로그 tail 조회
- `GET /admin/logs/search` - 로그 검색
- `GET /admin/logs/stats` - 로그 통계
- `POST /admin/logs/test` - 테스트 로그 생성

## 8. 인증 설정

### 8.1 Basic Authentication
xAPI 엔드포인트는 Basic Authentication을 사용합니다:
- **기본 사용자명**: `admin`
- **기본 비밀번호**: `admin`

### 8.2 인증 정보 변경
`src/main/resources/application.yml`에서 인증 정보를 변경할 수 있습니다:
```yaml
lrs:
  auth:
    basic:
      username: your_username
      password: your_password
```

## 9. 문제 해결

### 9.1 포트 충돌
8080 포트가 사용 중인 경우 `application.yml`에서 포트 변경:
```yaml
server:
  port: 8081
```

### 9.2 메모리 부족
JVM 힙 메모리 증가:
```bash
# Linux/Mac
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run

# Windows
set MAVEN_OPTS=-Xmx2g
mvn spring-boot:run
```

### 9.3 데이터베이스 연결 오류
- MySQL 서버가 실행 중인지 확인
- 네트워크 연결 확인 (43.201.31.215:3306)
- 데이터베이스 사용자 권한 확인
- 방화벽 설정 확인

### 9.4 로그 파일 확인
```bash
# 로그 디렉토리 확인
ls -la logs/

# 실시간 로그 확인
tail -f logs/application.log

# 에러 로그만 확인
grep ERROR logs/application.log
```

## 10. 개발 환경 설정

### 10.1 IDE 설정
- **IntelliJ IDEA**: Maven 프로젝트로 import
- **Eclipse**: Maven 프로젝트로 import
- **VS Code**: Java Extension Pack 설치 후 폴더 열기

### 10.2 로그 레벨 설정
`src/main/resources/application.yml`에서 로그 레벨 조정:
```yaml
logging:
  level:
    "[com.example.lrs]": DEBUG
    "[org.springframework.web]": DEBUG
    "[org.mybatis]": DEBUG
  file:
    name: logs/application.log
```

## 11. 배포 가이드

### 11.1 JAR 파일 생성
```bash
# 현재 디렉토리에서 실행
mvn clean package

# 생성된 JAR 파일 확인
ls -la target/internal-lrs-0.2.0.jar
```

### 11.2 JAR 파일 실행
```bash
# 현재 디렉토리에서 실행
java -jar target/internal-lrs-0.2.0.jar

# 또는 백그라운드 실행
nohup java -jar target/internal-lrs-0.2.0.jar > logs/app.log 2>&1 &
```

### 11.3 프로덕션 설정
- 외부 설정 파일 사용
- 데이터베이스 연결 풀 최적화
- 로그 파일 설정
- 모니터링 설정

이 가이드를 따라하면 Internal LRS를 성공적으로 설정하고 실행할 수 있습니다.
