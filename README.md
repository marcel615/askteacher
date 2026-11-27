# Ask-Teacher

Spring Boot API, React 웹 앱, 공식 명세를 한 저장소에서 관리합니다.

| 경로 | 내용 |
| --- | --- |
| [backend/](backend/) | Java 17, Spring Boot, Gradle Wrapper, 개발용 Compose |
| [frontend/](frontend/) | React, TypeScript, Vite, npm 잠금 파일 |
| [specs/](specs/) | [요구사항](specs/docs/requirements.md), [OpenAPI](specs/docs/openapi.yaml), [인증·세션](specs/docs/auth-policy.md), [ERD](specs/docs/schema.dbml), [UI 명세](specs/docs/ui-spec.md) |
| [docs/](docs/) | 공통 개발·검증 안내와 저장소 이관 기록 |

## 로컬 개발

백엔드는 `backend/`에서 `.env.example`을 참고해 `.env`를 준비하고 `docker compose up -d`로 PostgreSQL과 Redis를 시작합니다. 같은 디렉터리에서 `./gradlew bootRun`을 실행합니다. Windows에서는 `.\gradlew.bat bootRun`을 사용합니다. Compose 볼륨과 첨부 디렉터리 위치는 [백엔드 안내](backend/README.md)를 참고합니다.

별도 터미널의 `frontend/`에서 Node 26으로 `npm ci`, `npm run dev`를 실행합니다. 웹은 `http://localhost:5173`, API는 `http://localhost:8080/api`를 사용합니다.

[검증 및 이관 기록](docs/migration.md)에 CI, 원본 이력 조회와 Jira·Confluence 링크가 있습니다.

AI-DLC는 [루트 설정](ai-dlc.yaml)과 [현재 작업 기록](docs/current-task.md)을 사용합니다. 작업 브랜치의 기준과 PR 대상은 GitHub 기본 브랜치인 `develop`입니다.
