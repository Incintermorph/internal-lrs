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

## 3. 빠른 시작 (H2 인메모리 데이터베이스 사용)

### 3.1 프로젝트 클론 및 이동
```bash
cd c:\work\gemini\internal-lrs-full
```

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
- **H2 콘솔**: http://localhost:8080/h2-console

## 4. H2 데이터베이스 콘솔 접속

### 4.1 H2 콘솔 설정
1. 브라우저에서 http://localhost:8080/h2-console 접속
2. 다음 정보로 로그인:
   - **JDBC URL**: `jdbc:h2:mem:lrs`
   - **User Name**: `sa`
   - **Password**: (빈 값)

### 4.2 테이블 확인
로그인 후 다음 SQL로 테이블 구조를 확인할 수 있습니다:
```sql
SHOW TABLES;
DESCRIBE lrs_statement;
DESCRIBE lrs_document;
```

## 5. MySQL 데이터베이스 설정 (운영 환경)

### 5.1 MySQL 데이터베이스 생성
```sql
CREATE DATABASE lrs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lrs_user'@'localhost' IDENTIFIED BY 'lrs_pass';
GRANT ALL PRIVILEGES ON lrs.* TO 'lrs_user'@'localhost';
FLUSH PRIVILEGES;
```

### 5.2 스키마 생성
```bash
mysql -u lrs_user -p lrs < db/schema.sql
```

### 5.3 application.yml 설정 변경
`src/main/resources/application.yml` 파일에서 MySQL 설정을 활성화:

```yaml
spring:
  datasource:
    # H2 설정 주석 처리
    # url: jdbc:h2:mem:lrs;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    # username: sa
    # password: 
    # driver-class-name: org.h2.Driver
    
    # MySQL 설정 활성화
    url: jdbc:mysql://localhost:3306/lrs?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: lrs_user
    password: lrs_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # H2 콘솔 비활성화
  # h2:
  #   console:
  #     enabled: true

# MyBatis 설정 활성화
mybatis:
  mapper-locations: classpath:/mappers/*.xml
  configuration:
    map-underscore-to-camel-case: true
    default-statement-timeout: 5
```

## 6. xAPI 기능 활성화

현재 xAPI Statement와 Activity State API가 비활성화되어 있습니다. 활성화하려면:

### 6.1 컨트롤러 활성화
1. `StatementsController.java`에서 주석 제거:
   ```java
   @RestController
   @RequestMapping("/xapi/statements")
   @RequiredArgsConstructor
   public class StatementsController {
   ```

2. `ActivitiesStateController.java`에서 주석 제거:
   ```java
   @RestController
   @RequestMapping("/xapi/activities/state")
   @RequiredArgsConstructor
   public class ActivitiesStateController {
   ```

### 6.2 MyBatis 매퍼 파일 복구
`src/main/resources/mappers/` 디렉토리에 XML 매퍼 파일들을 복구해야 합니다.

## 7. API 엔드포인트

### 7.1 현재 활성화된 엔드포인트
- `GET /` - 메인 페이지 (애플리케이션 정보)
- `GET /health` - 헬스체크
- `GET /h2-console` - H2 데이터베이스 콘솔

### 7.2 xAPI 엔드포인트 (활성화 후 사용 가능)
- `POST /xapi/statements` - Statement 생성
- `PUT /xapi/statements?statementId=UUID` - Statement 업데이트
- `GET /xapi/statements` - Statement 조회
- `GET /xapi/statements?statementId=UUID` - 특정 Statement 조회
- `PUT/POST /xapi/activities/state` - Activity State 관리
- `GET /xapi/activities/state` - Activity State 조회
- `DELETE /xapi/activities/state` - Activity State 삭제

## 8. 인증 설정

### 8.1 Basic Authentication
xAPI 엔드포인트는 Basic Authentication을 사용합니다:
- **기본 사용자명**: `admin`
- **기본 비밀번호**: `admin`

### 8.2 인증 정보 변경
`application.yml`에서 인증 정보를 변경할 수 있습니다:
```yaml
lrs:
  auth:
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
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run
```

### 9.3 데이터베이스 연결 오류
- MySQL 서버가 실행 중인지 확인
- 데이터베이스 사용자 권한 확인
- 방화벽 설정 확인

## 10. 개발 환경 설정

### 10.1 IDE 설정
- **IntelliJ IDEA**: Maven 프로젝트로 import
- **Eclipse**: Maven 프로젝트로 import
- **VS Code**: Java Extension Pack 설치 후 폴더 열기

### 10.2 로그 레벨 설정
`application.yml`에서 로그 레벨 조정:
```yaml
logging:
  level:
    com.example.lrs: DEBUG
    org.springframework: INFO
```

## 11. 배포 가이드

### 11.1 JAR 파일 생성
```bash
mvn clean package
```

### 11.2 JAR 파일 실행
```bash
java -jar target/internal-lrs-0.2.0.jar
```

### 11.3 프로덕션 설정
- 외부 설정 파일 사용
- 데이터베이스 연결 풀 최적화
- 로그 파일 설정
- 모니터링 설정

이 가이드를 따라하면 Internal LRS를 성공적으로 설정하고 실행할 수 있습니다.
