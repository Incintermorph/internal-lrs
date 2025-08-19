# Internal LRS API 테스트 가이드

## 1. 현재 상태 확인

### 1.1 애플리케이션 상태 확인
```bash
curl http://localhost:8080/health
```

**응답 예시:**
```json
{
  "status": "UP",
  "timestamp": "2025-08-19T10:05:37",
  "application": "Internal LRS",
  "version": "0.2.0"
}
```

### 1.2 메인 페이지 확인
```bash
curl http://localhost:8080/
```

**응답 예시:**
```json
{
  "message": "Internal LRS (xAPI 1.0.3) is running",
  "endpoints": {
    "health": "/health",
    "h2-console": "/h2-console",
    "statements": "/xapi/statements (currently disabled)",
    "activities-state": "/xapi/activities/state (currently disabled)"
  }
}
```

## 2. 로그 모니터링 테스트

### 2.1 로그 API 테스트
```bash
# 로그 tail 조회 (최근 100줄)
curl http://localhost:8080/admin/logs/tail?lines=100

# 특정 레벨 로그만 조회
curl http://localhost:8080/admin/logs/tail?lines=50&level=ERROR

# 로그 검색
curl "http://localhost:8080/admin/logs/search?query=ERROR&maxResults=20"

# 로그 통계
curl http://localhost:8080/admin/logs/stats

# 테스트 로그 생성
curl -X POST "http://localhost:8080/admin/logs/test?level=ERROR&message=Test%20error%20message"
```

### 2.2 로그 파일 직접 확인
```bash
# 현재 디렉토리에서 로그 파일 확인
ls -la logs/

# 실시간 로그 모니터링
tail -f logs/application.log

# 에러 로그만 필터링
grep ERROR logs/application.log | tail -20
```

## 3. xAPI 기능 테스트 (현재 활성화됨)

### 3.1 Basic Authentication 설정
모든 xAPI 요청에는 Basic Authentication이 필요합니다:
- **Username**: `admin`
- **Password**: `admin`

### 3.2 Statement API 테스트

#### 3.2.1 Statement 생성 (POST)
```bash
curl -X POST http://localhost:8080/xapi/statements \
  -H "Content-Type: application/json" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -d '{
    "actor": {
      "name": "John Doe",
      "mbox": "mailto:john@example.com"
    },
    "verb": {
      "id": "http://adlnet.gov/expapi/verbs/experienced",
      "display": {"en-US": "experienced"}
    },
    "object": {
      "id": "http://example.com/course/1",
      "definition": {
        "name": {"en-US": "Introduction to xAPI"}
      }
    }
  }'
```

#### 3.2.2 Statement 조회 (GET)
```bash
# 모든 Statement 조회
curl -X GET http://localhost:8080/xapi/statements \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin

# 특정 Statement 조회
curl -X GET "http://localhost:8080/xapi/statements?statementId=YOUR_STATEMENT_ID" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin

# 필터링된 조회
curl -X GET "http://localhost:8080/xapi/statements?verb=http://adlnet.gov/expapi/verbs/experienced&limit=10" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin
```

#### 3.2.3 Statement 업데이트 (PUT)
```bash
curl -X PUT "http://localhost:8080/xapi/statements?statementId=YOUR_STATEMENT_ID" \
  -H "Content-Type: application/json" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -d '{
    "actor": {
      "name": "John Doe",
      "mbox": "mailto:john@example.com"
    },
    "verb": {
      "id": "http://adlnet.gov/expapi/verbs/completed",
      "display": {"en-US": "completed"}
    },
    "object": {
      "id": "http://example.com/course/1"
    }
  }'
```

### 3.3 Activity State API 테스트

#### 3.3.1 State 저장 (PUT)
```bash
curl -X PUT "http://localhost:8080/xapi/activities/state" \
  -H "Content-Type: application/json" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -G \
  -d "activityId=http://example.com/course/1" \
  -d "agent={"name":"John Doe","mbox":"mailto:john@example.com"}" \
  -d "stateId=progress" \
  --data-binary '{"completed": 75, "lastAccessed": "2025-08-19T10:00:00Z"}'
```

#### 3.3.2 State 조회 (GET)
```bash
curl -X GET "http://localhost:8080/xapi/activities/state" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -G \
  -d "activityId=http://example.com/course/1" \
  -d "agent={\"name\":\"John Doe\",\"mbox\":\"mailto:john@example.com\"}" \
  -d "stateId=progress"
```

#### 3.3.3 State 삭제 (DELETE)
```bash
curl -X DELETE "http://localhost:8080/xapi/activities/state" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -G \
  -d "activityId=http://example.com/course/1" \
  -d "agent={\"name\":\"John Doe\",\"mbox\":\"mailto:john@example.com\"}" \
  -d "stateId=progress"
```

## 4. Postman 컬렉션

### 4.1 환경 변수 설정
```json
{
  "name": "Internal LRS Environment",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "username",
      "value": "admin",
      "enabled": true
    },
    {
      "key": "password", 
      "value": "admin",
      "enabled": true
    }
  ]
}
```

### 4.2 공통 헤더 설정
모든 xAPI 요청에 다음 헤더를 추가:
```
X-Experience-API-Version: 1.0.3
Authorization: Basic YWRtaW46YWRtaW4=
```

## 5. 성능 테스트

### 5.1 부하 테스트 (Apache Bench)
```bash
# 헬스체크 엔드포인트 부하 테스트
ab -n 1000 -c 10 http://localhost:8080/health

# Statement 생성 부하 테스트 (xAPI 활성화 후)
ab -n 100 -c 5 -p statement.json -T application/json \
   -H "X-Experience-API-Version: 1.0.3" \
   -A admin:admin \
   http://localhost:8080/xapi/statements
```

### 5.2 메모리 사용량 모니터링
```bash
# JVM 메모리 사용량 확인
jstat -gc [PID]

# 힙 덤프 생성
jmap -dump:format=b,file=heapdump.hprof [PID]
```

## 6. 오류 처리 테스트

### 6.1 인증 오류 테스트
```bash
# 잘못된 인증 정보
curl -X GET http://localhost:8080/xapi/statements \
  -H "X-Experience-API-Version: 1.0.3" \
  -u wrong:credentials
```

**예상 응답**: `401 Unauthorized`

### 6.2 잘못된 요청 테스트
```bash
# 필수 헤더 누락
curl -X GET http://localhost:8080/xapi/statements \
  -u admin:admin
```

**예상 응답**: `400 Bad Request`

### 6.3 잘못된 JSON 테스트
```bash
curl -X POST http://localhost:8080/xapi/statements \
  -H "Content-Type: application/json" \
  -H "X-Experience-API-Version: 1.0.3" \
  -u admin:admin \
  -d '{"invalid": json}'
```

**예상 응답**: `400 Bad Request`

## 7. 로그 모니터링

### 7.1 애플리케이션 로그 확인
```bash
# 현재 디렉토리에서 로그 확인
ls -la logs/

# 실시간 로그 확인
tail -f logs/application.log

# 에러 로그만 확인
grep ERROR logs/application.log

# 특정 시간대 로그 확인
grep "2025-08-19 10:" logs/application.log

# 로그 파일 크기 확인
du -h logs/application.log
```

### 7.2 로그 API를 통한 분석
```bash
# API를 통한 로그 통계
curl http://localhost:8080/admin/logs/stats | jq

# 특정 키워드 검색
curl "http://localhost:8080/admin/logs/search?query=Statement&maxResults=10" | jq

# 에러 로그만 조회
curl "http://localhost:8080/admin/logs/tail?level=ERROR&lines=50" | jq
```

## 8. 데이터베이스 성능 테스트

### 8.1 현재 데이터베이스 연결 확인
```bash
# MySQL 연결 테스트
mysql -h 43.201.31.215 -u nidsuser -p test -e "SELECT COUNT(*) FROM lrs_statement;"

# 테이블 구조 확인
mysql -h 43.201.31.215 -u nidsuser -p test -e "DESCRIBE lrs_statement;"
```

### 8.2 쿼리 성능 확인
```sql
-- 현재 데이터베이스에서 실행
-- Statement 조회 성능
EXPLAIN SELECT * FROM lrs_statement WHERE verb_id = 'http://adlnet.gov/expapi/verbs/experienced';

-- 인덱스 사용률 확인
SHOW INDEX FROM lrs_statement;

-- 테이블 상태 확인
SHOW TABLE STATUS LIKE 'lrs_statement';
```

### 8.3 애플리케이션 로그를 통한 성능 모니터링
```bash
# 데이터베이스 관련 로그 확인
grep -i "mysql\|hikari\|statement" logs/application.log

# 쿼리 실행 시간 로그 확인
grep -i "query\|select\|insert" logs/application.log
```

이 가이드를 통해 Internal LRS의 모든 기능을 체계적으로 테스트할 수 있습니다.
