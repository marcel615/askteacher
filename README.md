# Ask-Teacher

Spring Boot API, React 웹 앱, 공식 명세를 한 저장소에서 관리합니다.

| 경로 | 내용 |
| --- | --- |
| [backend/](backend/) | Java 17, Spring Boot, Gradle Wrapper, 개발용 Compose |
| [frontend/](frontend/) | React, TypeScript, Vite, npm 잠금 파일 |
| [specs/](specs/) | [요구사항](specs/requirements.md), [OpenAPI](specs/openapi.yaml), [인증·세션](specs/auth-policy.md), [ERD](specs/schema.dbml), [UI 명세](specs/ui-spec.md) |
| [docs/](docs/) | 공통 개발·검증 안내와 작업 기록 |

## 로컬 개발

백엔드는 `backend/`에서 `.env.example`을 참고해 `.env`를 준비하고 `docker compose up -d`로 PostgreSQL과 Redis를 시작합니다. 같은 디렉터리에서 `./gradlew bootRun`을 실행합니다. Windows에서는 `.\gradlew.bat bootRun`을 사용합니다. Compose 볼륨과 첨부 디렉터리 위치는 [백엔드 안내](backend/README.md)를 참고합니다.

별도 터미널의 `frontend/`에서 Node 26으로 `npm ci`, `npm run dev`를 실행합니다. 웹은 `http://localhost:5173`, API는 `http://localhost:8080/api`를 사용합니다.

## 빌드와 테스트

- Backend는 격리 Redis 7을 `127.0.0.1:16379`에 연결하고 `backend/`에서 `./gradlew test bootJar`를 실행합니다. Windows에서는 `.\gradlew.bat test bootJar`를 사용합니다. 별도 포트는 `TEST_REDIS_PORT`로 지정합니다.
- Frontend는 `frontend/`에서 `npm ci`, `npm run lint`, `npm run typecheck`, `npm test`, `npm run build`를 실행합니다.
- HTTP 연동 테스트는 별도 테스트 DB, Redis 및 임시 업로드 경로를 사용하는 백엔드를 띄운 뒤 `ASK14_BACKEND_URL=http://127.0.0.1:18080/api`로 `npm test`를 실행합니다. Redis 장애 검증에는 `ASK14_REDIS_CONTAINER=askteacher-ask14-link-redis`와 전용 Redis 포트 16379가 필요합니다.
- [CI](.github/workflows/ci.yml)는 `main`, `develop`의 PR과 push에서 Spring JAR, JaCoCo XML, React `dist`를 빌드·저장합니다. `CODECOV_TOKEN`을 등록하면 JaCoCo XML을 Codecov에도 올립니다.

## 작업 기록

기능의 완료 조건에 필요한 명세·Backend·Frontend 변경을 같은 Task와 PR에서 처리합니다. 공식 명세는 승인 후 같은 PR에 반영합니다. 새 PR에는 해당 [Jira ASK](https://marcel615.atlassian.net/browse/ASK) 이슈 키를 포함합니다. 현재 운영 방식은 [프로젝트 안내](https://marcel615.atlassian.net/wiki/spaces/ASK/pages/491521)에서 찾을 수 있습니다. 기획과 계약 논의는 [Confluence 기능 기획](https://marcel615.atlassian.net/wiki/spaces/ASK/pages/753692), [공통 계약 변경](https://marcel615.atlassian.net/wiki/spaces/ASK/pages/983042)에서 확인합니다. [작업 로그](docs/current-work-log.md)와 [기존 개발 일지](docs/devlog/)는 `docs/`에서 조회합니다.

AI-DLC는 [루트 설정](ai-dlc.yaml)과 [현재 작업 기록](docs/current-task.md)을 사용합니다. 작업 브랜치의 기준과 PR 대상은 GitHub 기본 브랜치인 `develop`입니다.
