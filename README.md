# LMS-Auth

MSA-LMS v2의 계정, 로그인, JWT 발급·재발급을 담당하는 Auth 서비스입니다.

## 사전 준비

- Java 21
- Docker Desktop 또는 Docker Engine(Testcontainers 통합 테스트에 필요)
- MySQL 8.4
- Gateway와 함께 실행할 경우 SCG 서비스

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
