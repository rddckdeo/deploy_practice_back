# deploy — 배포 연습용 게시판 백엔드

Spring Boot 4.1.2 · Java 21 · Gradle · MySQL · Flyway 12.4.0

**코드는 그대로 두고, 프로필(local / prod)과 환경 변수만 바꿔서 로컬과 서버에서 실행하는 구조**입니다.

## API

모든 API는 `/api`로 시작합니다. Nginx에서 `/api/` 경로를 이 서버(8080)로 넘기면 됩니다.

| Method | URL | 설명 | 성공 응답 |
|---|---|---|---|
| GET | `/api/health` | 서버 상태 확인 | `200` `OK` |
| GET | `/api/posts` | 목록 (최신순) | `200` |
| GET | `/api/posts/{id}` | 상세 | `200` / 없으면 `404` |
| POST | `/api/posts` | 작성 | `201` |
| PUT | `/api/posts/{id}` | 수정 | `200` |
| DELETE | `/api/posts/{id}` | 삭제 | `204` |

작성 요청 예시:

```json
{ "title": "제목", "content": "내용", "author": "작성자" }
```

수정할 때는 `title`과 `content`만 보냅니다. 입력값이 올바르지 않으면 `400`이 돌아오고, 에러 본문은 ProblemDetail JSON 형식입니다.

## 설정 파일 구조

```
src/main/resources/
├── application.yml                 # 공통 설정 (기본 프로필 = local, 포트 8080, ddl-auto=validate)
├── application-local.yml           # 내 PC DB 정보 ← Git 제외 (.gitignore)
├── application-local.yml.example   # 위 파일의 템플릿 (Git에 올라감, 비밀번호 비어 있음)
├── application-prod.yml            # 서버용, DB 정보는 환경 변수로만 받음
└── db/migration/V1__init.sql       # 테이블 정의 (Flyway가 실행)
```

| 프로필 | 활성화 방법 | DB 접속 정보 출처 |
|---|---|---|
| `local` | 기본값 (아무것도 안 해도 됨) | `application-local.yml` |
| `prod` | `--spring.profiles.active=prod` | 환경 변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |

비밀번호를 지키는 장치:
- `application-local.yml`은 `.gitignore`에 들어 있어 커밋되지 않습니다.
- `bootJar` 빌드에서도 빠지므로 서버에 올리는 jar에 들어가지 않습니다.
- `application-prod.yml`에는 `${DB_PASSWORD}` 같은 자리표시자만 있습니다. 환경 변수가 없으면 앱이 시작되지 않습니다.

## DB 테이블 (Flyway)

- 테이블은 `db/migration/V1__init.sql`로 만들고, 앱이 시작될 때 Flyway가 자동으로 적용합니다.
- JPA는 `ddl-auto: validate`라서 테이블을 바꾸지 않습니다. 엔티티와 테이블이 맞는지 검사만 합니다.
- **이미 적용된 마이그레이션 파일은 수정하지 마세요.** 체크섬 오류가 납니다. 테이블을 바꿀 때는 `V2__설명.sql`처럼 새 파일을 추가하고, 엔티티(`Post.java`)도 함께 맞춥니다.

## 로컬 실행

### 1. MySQL 준비 (최초 1회)

```sql
CREATE DATABASE practice_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'practice_user'@'localhost' IDENTIFIED BY '비밀번호';
GRANT ALL PRIVILEGES ON practice_db.* TO 'practice_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 로컬 설정 파일 만들기

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# application-local.yml 을 열어 password: 에 비밀번호 입력
```

### 3. 실행

```bash
./gradlew bootRun          # Windows: gradlew.bat bootRun
curl http://localhost:8080/api/health   # → OK
```

IDE에서 `Application.java`를 실행해도 local 프로필로 뜹니다.

## 빌드 (실행 가능한 jar)

```bash
./gradlew clean build      # Windows: gradlew.bat clean build
# → build/libs/deploy-0.0.1-SNAPSHOT.jar
```

- 테스트는 DB 없이 웹 계층만 검사하므로, MySQL이 없는 환경에서도 빌드됩니다.
- 테스트를 건너뛰려면 `./gradlew clean bootJar`를 사용하세요.

## 서버 실행 (prod 프로필 + 환경 변수)

### Linux (bash)

```bash
export DB_URL='jdbc:mysql://localhost:3306/practice_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8'
export DB_USERNAME='practice_user'
export DB_PASSWORD='서버DB비밀번호'

java -jar deploy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

백그라운드로 실행하고 로그를 파일로 남기려면:

```bash
nohup java -jar deploy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
tail -f app.log
```

프로필도 환경 변수로 지정할 수 있습니다. systemd나 Docker로 넘어갈 때 편리합니다.

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar deploy-0.0.1-SNAPSHOT.jar
```

> `export DB_PASSWORD=...`를 직접 치면 셸 히스토리에 남습니다. 연습이 끝나면 systemd의 `EnvironmentFile=`(권한 600)이나 `.env` 파일로 옮기는 것을 권장합니다. `.env`도 `.gitignore`에 들어 있습니다.

### Windows (PowerShell)에서 prod 프로필 테스트

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/practice_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8'
$env:DB_USERNAME = 'practice_user'
$env:DB_PASSWORD = '비밀번호'
java -jar build/libs/deploy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 동작 확인

```bash
curl http://localhost:8080/api/health
curl -X POST http://localhost:8080/api/posts \
     -H 'Content-Type: application/json' \
     -d '{"title":"첫 글","content":"배포 성공!","author":"me"}'
curl http://localhost:8080/api/posts
```

## CORS

CORS 설정은 일부러 넣지 않았습니다. 개발 중에는 Vite 프록시가, 운영에서는 Nginx가 프론트와 API를 같은 주소(origin)로 묶어 주기 때문입니다.

Nginx 설정 예시:

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

prod 프로필에는 `server.forward-headers-strategy: framework`가 켜져 있어서, 위 `X-Forwarded-*` 헤더를 인식합니다.
