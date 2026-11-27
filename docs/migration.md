# 저장소 이관 및 검증

## 이관 기준점

| 영역 | 원본 브랜치 | 기준 SHA |
| --- | --- | --- |
| [Specs](https://github.com/marcel615/Ask-Teacher-specs) | `main` | `ee0ef5313acab5887ee11b59272c29d88af9f504` |
| [Backend](https://github.com/marcel615/Ask-Teacher-spring) | `develop` | `2c4d02d76cfe5690755c4b0a8deddd0e28df7eac` |
| [Frontend](https://github.com/marcel615/Ask-Teacher-react) | `develop` | `dc16b07b9d4efbff6a51b0733640001b1a1d637d` |

최초 가져오기 커밋은 세 기준 SHA를 부모로 가지고 각 원본 트리를 영역 경로에 그대로 담습니다. 원격 원본 브랜치는 `legacy/<영역>/<브랜치>`에 있습니다. 원본 브랜치에서 접근할 수 없던 PR 커밋은 `legacy-pr/<영역>/<번호>` 태그로 보존합니다. 이동 전 파일은 `git show <원본 SHA>:<원래 경로>`로 조회할 수 있습니다.

활성 공식 원본은 `main`의 [specs/docs/](../specs/docs/)입니다. 각 영역의 예전 `ai-dlc.yaml`, 작업 일지 및 중첩 GitHub 설정은 당시를 재현하기 위한 기록이며, 새 저장소 CI는 루트 `.github/workflows/`에서 실행합니다.

## 빌드와 테스트

- Backend는 격리 Redis 7을 `127.0.0.1:16379`에 연결하고 `backend/`에서 `./gradlew test bootJar`를 실행합니다. Windows에서는 `.\gradlew.bat test bootJar`를 사용합니다. 별도 포트는 `TEST_REDIS_PORT`로 지정합니다.
- Frontend는 `frontend/`에서 `npm ci`, `npm run lint`, `npm run typecheck`, `npm test`, `npm run build`를 실행합니다.
- 기존 HTTP 연동 테스트는 `ASK14_BACKEND_URL=http://127.0.0.1:18080/api`를 지정해 `frontend/`에서 `npm test`로 실행합니다. 이 주소에 별도 테스트 DB, Redis 및 임시 업로드 경로를 사용하는 백엔드가 먼저 떠 있어야 합니다. 장애 검증에는 `ASK14_REDIS_CONTAINER=askteacher-ask14-link-redis`와 전용 Redis 포트 16379가 필요합니다.
- GitHub Actions의 `CI`는 `main` PR과 push에서 두 앱을 빌드하고 Spring JAR, JaCoCo XML, React `dist`를 저장합니다. Codecov는 새 저장소의 업로드 토큰을 `CODECOV_TOKEN`으로 등록하면 같은 XML을 업로드합니다.

## 작업 기록

GitHub의 `ASK-` 자동 링크는 기존 Jira 이슈 주소를 가리킵니다. 새 PR에는 해당하는 [Jira ASK](https://marcel615.atlassian.net/browse/ASK) 이슈 키를 포함합니다. 기존 상세 기록은 원래 이슈와 [Confluence 기능 기획·논의](https://marcel615.atlassian.net/wiki/spaces/ASK/pages/753692), [공통 계약 변경](https://marcel615.atlassian.net/wiki/spaces/ASK/pages/983042)에서 조회합니다. 과거 GitHub PR·이슈 URL은 원본 저장소를 가리킵니다.

기존 개발 일지는 [Backend](../backend/docs/devlog/)와 [Frontend](../frontend/docs/devlog/)에서 확인할 수 있습니다.
