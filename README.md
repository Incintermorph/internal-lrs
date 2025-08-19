
# Internal LRS (xAPI 1.0.3) — Spring Boot + MyBatis + MySQL (Full)

구성:
- **/xapi/statements**: `POST`, `PUT?statementId=UUID`, `GET`(since/verb/activity/registration/limit, 단순)
- **/xapi/activities/state**: `PUT/POST/GET/DELETE` + ETag/If-Match/If-None-Match
- **Basic Auth** 필터 + `X-Experience-API-Version` 헤더 검사
- **MyBatis** 매퍼(XML) + 서비스 + 컨트롤러
- **MySQL 8** JSON 컬럼 및 생성 칼럼 인덱스

## 1) DB 준비
```sql
CREATE DATABASE lrs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lrs_user'@'%' IDENTIFIED BY 'lrs_pass';
GRANT ALL ON lrs.* TO 'lrs_user'@'%';
```
스키마 적용:
```sql
SOURCE db/schema.sql;
```

## 2) 실행
`src/main/resources/application.yml` DB 접속 정보 수정 후:
```bash
mvn spring-boot:run
```

## 3) 빠른 테스트

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
