
# Internal LRS (xAPI 1.0.3) — Spring Boot + MyBatis + MySQL (Full)

## 🚀 현재 활성화된 기능

### API 엔드포인트
- **/xapi/statements**: `POST`, `PUT?statementId=UUID`, `GET`(since/verb/activity/registration/limit)
- **/xapi/activities/state**: `PUT/POST/GET/DELETE` + ETag/If-Match/If-None-Match
- **/admin/logs/**: 실시간 로그 모니터링 API
- **/health**: 헬스체크 API

### 기술 스택
- **Spring Boot 3.3.2** + **MyBatis 3.0.3**
- **MySQL 8** (원격 서버: 43.201.31.215:3306)
- **Basic Auth** 필터 + `X-Experience-API-Version` 헤더 검사
- **상세한 로깅 시스템** (./logs/application.log)
- **성능 최적화된 Tomcat 설정**

## 1) 빠른 시작

### 현재 디렉토리 확인
```bash
# 현재 디렉토리가 프로젝트 루트인지 확인
ls -la
# 다음 파일들이 있어야 함: pom.xml, src/, db/, logs/
```

### 애플리케이션 실행
```bash
# 컴파일 및 실행
mvn clean compile
mvn spring-boot:run

# 또는 JAR 파일로 실행
mvn clean package
java -jar target/internal-lrs-0.2.0.jar
```

### 접속 확인
- **메인 페이지**: http://localhost:8080
- **헬스체크**: http://localhost:8080/health
- **로그 모니터링**: http://localhost:8080/admin/logs/tail

## 2) 로그 모니터링

### 로그 파일 확인
```bash
# 로그 디렉토리 확인
ls -la logs/

# 실시간 로그 확인
tail -f logs/application.log

# 에러 로그만 확인
grep ERROR logs/application.log
```

### 로그 API 사용
```bash
# 최근 로그 조회
curl http://localhost:8080/admin/logs/tail?lines=100

# 에러 로그만 조회
curl "http://localhost:8080/admin/logs/tail?level=ERROR&lines=50"

# 로그 검색
curl "http://localhost:8080/admin/logs/search?query=Statement&maxResults=10"

# 로그 통계
curl http://localhost:8080/admin/logs/stats
```

## 3) xAPI 테스트

### Statement 등록
```bash
curl -u admin:admin -H "X-Experience-API-Version: 1.0.3" -H "Content-Type: application/json"   -d '{"actor":{"account":{"homePage":"https://lms.example.com","name":"123"}}, "verb":{"id":"http://adlnet.gov/expapi/verbs/initialized"}, "object":{"id":"https://lms.example.com/xapi/item/42"}}'   http://localhost:8080/xapi/statements
```

### Statement 멱등 저장(PUT)
```bash
curl -u admin:admin -H "X-Experience-API-Version: 1.0.3" -H "Content-Type: application/json"   -X PUT "http://localhost:8080/xapi/statements?statementId=11111111-1111-1111-1111-111111111111"   -d '{"id":"11111111-1111-1111-1111-111111111111","actor":{"account":{"homePage":"https://lms.example.com","name":"123"}}, "verb":{"id":"http://adlnet.gov/expapi/verbs/completed"}, "object":{"id":"https://lms.example.com/xapi/item/42"}}'
```

### State 저장/조회
```bash
curl -u admin:admin -H "X-Experience-API-Version: 1.0.3"   -H "Content-Type: application/json" -H "If-None-Match: *"   --data-binary '{"resume":"page:10"}'   "http://localhost:8080/xapi/activities/state?activityId=https://lms.example.com/xapi/item/42&agent=%7B%22account%22:%7B%22homePage%22:%22https://lms.example.com%22,%22name%22:%22123%22%7D%7D&stateId=resume-state"

curl -u admin:admin -H "X-Experience-API-Version: 1.0.3"   "http://localhost:8080/xapi/activities/state?activityId=https://lms.example.com/xapi/item/42&agent=%7B%22account%22:%7B%22homePage%22:%22https://lms.example.com%22,%22name%22:%22123%22%7D%7D&stateId=resume-state" -i
```

## 4) 확장 TODO
- `/xapi/activities/profile`, `/xapi/agents/profile`
- Statements `GET` 고급 필터, `more` 커서, attachments(multipart/mixed)
- OAuth2 인증
- 시간 파티셔닝, 보관 정책, 에러 로깅/모니터링
