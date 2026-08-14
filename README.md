# LMS-Auth

MSA-LMS v2의 계정, 로그인, JWT 발급·재발급을 담당하는 Auth 서비스입니다.

## 사전 준비

- Java 21
- Docker Desktop 또는 Docker Engine(Testcontainers 통합 테스트에 필요)
- MySQL 8.4
- Gateway와 함께 실행할 경우 SCG 서비스

## 환경 변수

| 변수 | 필수 | 설명 | 예시 |
|---|---:|---|---|
| `APP_PORT` | 예 | Auth HTTP 포트 | `8081` |
| `DB_HOST` | 예 | MySQL 호스트 | `localhost` |
| `DB_PORT` | 예 | MySQL 포트 | `3306` |
| `DB_NAME` | 예 | Auth 스키마 | `lms_auth` |
| `DB_USER` | 예 | DB 계정 | `auth` |
| `DB_PASSWORD` | 예 | DB 비밀번호 | 로컬 전용 값 |
| `JWT_KID` | 예 | JWT 키 식별자 | `local-auth-key` |
| `JWT_PRIVATE_KEY_B64` | 예 | PKCS#8 PEM 전체를 다시 Base64 인코딩한 값 | 실제 키 값 |
| `JWT_PUBLIC_KEY_B64` | 예 | X.509 PEM 전체를 다시 Base64 인코딩한 값 | 실제 키 값 |
| `GATEWAY_URI` | 예 | OpenAPI에 표시할 Gateway 기준 URL | `http://localhost:8080` |
| `APP_DESCRIPTION` | 예 | OpenAPI 서버 설명 | `Local Gateway` |
| `FILE_SERVER_URI` | 예 | 파일 공개 기준 URL | `http://localhost:8080/files` |
| `FILE_STORAGE_PATH` | 예 | 업로드 파일 저장 경로 | `./storage` |

비밀번호와 JWT 키는 저장소에 커밋하지 말고 환경 변수나 로컬 전용 `.env`에서 관리합니다.

Refresh 세션 Redis 변경(`feature/redis-refresh-session`)까지 적용한 통합 상태에서는 다음 변수도 사용할 수 있습니다.

| 변수 | 기본값 | 설명 |
|---|---:|---|
| `REDIS_HOST` | `localhost` | Auth와 SCG가 공유하는 Redis 호스트 |
| `REDIS_PORT` | `6379` | Redis 포트 |
| `REDIS_PASSWORD` | 빈 값 | 로컬 Redis 비밀번호 |
| `REDIS_TIMEOUT` | `2s` | Redis 명령 제한 시간 |

해당 변경이 합쳐진 뒤에는 Auth 저장소의 `docker-compose.yml`로 AOF `everysec`와 `noeviction`이 적용된 로컬 Redis를 실행할 수 있습니다.

```powershell
docker compose up -d redis
docker compose ps
```

## 로컬 기동

필수 환경 변수를 설정한 뒤 Windows에서는 다음 명령을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

Unix 계열 환경에서는 `./gradlew bootRun`을 사용합니다. Gateway를 통하지 않고 Auth에 직접 접근할 때도 인증·인가 및 JWT 검증 규칙은 동일하게 적용됩니다.

## 테스트

통합 테스트는 `mysql:8.4` Testcontainers를 자동으로 시작합니다. 로컬 MySQL 환경 변수나 운영용 DB가 필요하지 않으며, 운영용 dummy SQL도 실행하지 않습니다.

```powershell
docker version
.\gradlew.bat test
```

특정 context smoke test만 실행하려면 다음 명령을 사용합니다.

```powershell
.\gradlew.bat test --tests com.msa4lmsv2auth.Msa4LmsV2AuthApplicationTests
```

Docker daemon에 연결할 수 없으면 Testcontainers 기반 테스트는 시작되지 않습니다. 먼저 Docker 상태를 확인한 뒤 다시 실행합니다.

## 주요 엔드포인트

- `POST /api/auth/student/login`
- `POST /api/auth/professor/login`
- `POST /api/auth/admin/login`
- `POST /api/auth/reissue-token`
- `POST /api/auth/logout`
- `GET /api-docs`
