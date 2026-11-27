# 현재 작업 로그

초기 이관 시점의 Backend와 Frontend 기록입니다. 다음 작업부터 이 파일은 해당 Jira 키의 현재 기록으로 갱신합니다.

## Backend 이관 기록

# Current Work Log

## ASK-16 게시글 상세에 현재 사용자의 작성자 여부 제공

- 사용자 요청: 2026-11-18 `애스크티처 백엔드 ASK-16 작업하자.` Backend 개발 범위만 승인됐다.
- 공통 운영 루트 ai-dlc의 backend-issue-driven-dev 및 공통 개발/Jira/Backend 리뷰 절차를 적용한다.
- 등록 YAML 파싱·형식 검증, Backend/Specs Git 루트 및 fetch/push origin과 설정 저장소 일치를 확인했다.
- Jira https://marcel615.atlassian.net/browse/ASK-16: ASK 프로젝트의 작업 유형, area:backend, 착수 가능 상태 및 댓글 0건을 확인했다. 기존 진행/검토 중 Backend Task는 없었다. 선행 ASK-15 완료 및 공식 계약 반영을 확인했다.
- Main이 가능한 전환 21을 확인하고 진행 중으로 전환한 뒤 재조회했다. Jira 본문·완료 조건은 변경하지 않는다.
- 이전 Backend #79 PR #80은 develop에 실제 merge됐고 docs/devlog/2026-11.md에 devlog가 반영됐다. 기존 기록은 Git 이력에 보존하고 새 작업 임시 기록으로 전환한다.
- Backend origin/develop 최신 기준 457d8fecad180b8610523fb8a2e5d99baec7eed8. 작업 브랜치 feature/issue-ASK-16-post-detail-is-author.
- Specs origin/main 최신 bb06abd9d2557a37ca19ae1bde3bc1b23f878357, 계약 병합 7e0fa2e787a9ecaaa2b7d63ed60983d0d113a017 포함. 두 commit 사이 공식 4문서 diff 없음. 원본 링크는 current-task.md에 기록한다.
- Architect 읽기 전용 설계를 Main이 승인했다. 기존 선택적 principal 전달, 작성자 fetch join, JWT 오류 및 Security cache-control 설정을 유지하고 DTO boolean isAuthor와 서비스 계산만 추가한다. 계약·DB·인증정책·운영 파일 변경 불필요.
- 작성자/비작성자/익명, 사용자별 반복 조회와 no-store, 없는글/삭제글, invalid/expired/revoked token, Redis 장애 503, 수정삭제 직접 요청 인증/인가를 검증한다. 기본 명령 .\gradlew.bat test.
- 테스트 환경: Corretto 17.0.21, 기존 test 프로필의 격리 H2/임시 업로드 디렉터리. Docker Desktop을 시작하고 이번 작업 전용 askteacher-ask16-test-redis (redis:7, 127.0.0.1:16379) 생성·PONG 확인. 기존 otboo-redis 6379는 변경하지 않았다. 이번 테스트 컨테이너는 종료 시 Main이 정리한다.
- 설계 게이트 통과. 구현·전체 테스트·독립 리뷰·devlog·PR·최종 head CI는 아직 미수행.
- Builder 초기 관련 테스트는 compileTestJava에서 새 통합 테스트의 multipart/일반 요청 builder 공통 타입 불일치로 실패했다. Spring 요청 builder의 공통 상위 타입을 사용하도록 테스트를 수정했으며 제품 동작 실패는 관찰되지 않았다. 수정·재검증 1회차를 진행한다.
- 관련 테스트 재검증 1회차는 36건 중 신규 서비스 테스트 1건의 Mockito strict stubbing 불일치로 실패했고 나머지 35건은 성공했다. 좋아요 독립성 테스트에서 작성자의 false 결과도 명시적으로 stub하도록 수정했다. 수정·재검증 2회차를 진행한다.
- 관련 테스트 수정·재검증 2회차 성공: `.\gradlew.bat test --tests '*PostServiceTest' --tests '*PostControllerTest' --tests '*PostDetailApiIntegrationTest' --tests '*AuthRedisFailureIntegrationTest'` (22초). JUnit XML 36건, 실패·오류·건너뜀 0건. 제품 코드 수정은 DTO·Service 두 파일로 한정됐다. 이어 설정된 기본 전체 테스트를 실행한다.
- 설정된 기본 `.\gradlew.bat test` 전체 성공(37초). JUnit XML 총 196건, 실패·오류·건너뜀 모두 0건이며 JaCoCo XML 생성 확인. 초기 컴파일 및 신규 mock fixture 실패를 수정한 후 최종 실행은 모두 통과했다.
- API별 검증 대응: PostServiceTest는 작성자/비작성자/익명·좋아요 독립성/기존 파일 필드/없는글, PostControllerTest는 선택적 principal·정확한 isAuthor 불리언 필드와 false 포함/no-store, PostDetailApiIntegrationTest는 실제 로그인·DB 기반 사용자별 반복 GET/첨부·기존필드/삭제·없는글404/invalid·empty·wrong-signature·expired·revoked401/직접 PATCH·DELETE 익명401·타인403·작성자성공/거부후 DB·첨부 불변, AuthRedisFailureIntegrationTest는 상세 Bearer503·익명공개200을 검증한다.
- Builder 최종 자체 확인: git diff --check 통과. 제품 diff는 PostDetailResponse와 PostService에 한정되며 Controller·Security·Repository·Entity·환경·공식 Specs·AI-DLC 운영 파일을 변경하지 않았다. 계약 변경과 추가 사용자 결정은 불필요하다. Frontend 실제 연동·배포는 제외 범위이며 미검증이다. commit·push·PR·독립 리뷰·devlog·최종 head CI는 Main 후속 단계다. 테스트 Redis lifecycle은 Main이 관리한다.
- Main 구현 게이트 통과: 승인 설계와 실제 diff를 확인했고 JUnit XML 196건 실패·오류·건너뜀0, JaCoCo XML 및 diff --check를 직접 확인했다. 보호 파일 변경 없음. 열린 PR 없음, develop 활성 ruleset은 PR 경유/삭제·force push 금지, 필수 승인 수0이다. 다음 단계는 구현 commit·push·PR·독립 리뷰다.
- Main이 구현 commit 59b10647bb87794fec26e5698f65a5a7cbb7e4aa를 push하고 develop 대상 PR #84(https://github.com/marcel615/Ask-Teacher-spring/pull/84)를 생성했다. Jira 기존 웹 링크와 편집 가능 여부·전환을 확인하고 PR 링크를 추가·재조회한 뒤 ASK-16 검토 중 전환·재조회를 완료했다. 최초 head CI test 진행 중, mergeable MERGEABLE. Architect 독립 리뷰를 요청했다.
- Architect 독립 리뷰 통과(검토 head 59b10647bb87794fec26e5698f65a5a7cbb7e4aa): 필수 수정·선택 개선 없음. 실제 전체 XML196건과 계약·승인범위·보호파일diff없음을 독립 확인했다. Main이 PR 일반 리뷰로 등록하고 Reporter에 월별 devlog 초안을 요청했다. 독립 추적할 신규 기술 후속 작업은 확인되지 않아 Jira/GitHub 후속 작업을 생성하지 않았다.
- Main이 이번 작업 전용 askteacher-ask16-test-redis의 이름·이미지·16379 포트를 확인한 뒤 stop/rm 완료했다. 기존 otboo-redis 및 Docker Desktop은 종료하지 않았다. 최종 제품 테스트 이후 제품·테스트 변경은 없으며 남은 변경은 devlog·작업 기록이다.
- 최초 구현 head59b1064의 GitHub CI run44298833572가 test·JaCoCo확인·Codecov업로드 포함 SUCCESS임을 Main이 확인했다. Reporter devlog append 후 기록 commit을 push하면 해당 최종 head 기준 CI·리뷰·충돌·보호 조건을 다시 확인한다.
- 2026-11-19 사용자 재개 요청으로 실제 Git/PR/Jira 상태를 재확인했다. PR84 OPEN·MERGEABLE·CLEAN, 구현head CI SUCCESS, ASK-16 검토 중 및 정확한 PR웹링크를 확인했다. Reporter의 월별 devlog 추가분을 Main이 승인 초안·근거와 대조했고 기존 내용 불변 및 ASK-16 단일 항목·diff --check 통과를 확인해 리뷰·기록 게이트를 통과시켰다. 이번 최종 변경은 devlog·작업 로그뿐이다. 최종 push 후 추적 파일을 수정하지 않고 해당 head CI·리뷰·충돌·보호조건을 확인하며 사용자 직접 merge를 기다린다.

---

## Frontend 이관 기록

# ASK-21 작업 기록

- 원격과 동일한 React develop에서 feature/issue-21-typescript-migration 브랜치를 만들었다.
- 2026-11-23 Jira에서 진행 중으로 전환하고 재조회했다. 이번 전환은 타입 검사 환경부터 시작한다.

- 1단계 자체 리뷰를 완료했다. TypeScript 6.0.3의 엄격 검사와 TS·TSX용 ESLint 규칙을 적용했다. JS·JSX는 점진 전환 동안 사용한다. 기존 빈 확장 인터페이스는 동일 타입의 별칭으로 정리했다.
- npm run typecheck, npm run lint, npm run build 통과. Vite 개발 서버의 HTML과 변환된 JSX가 각각 HTTP 200을 반환했다. 기존 테스트 26개 통과, 격리된 백엔드 URL이 필요한 1개는 미실행했다.
- 다음 단계는 게시글 목록의 타입 전환과 브라우저에서 검색·필터·페이징·화면 상태를 검증하는 것이다.

- Draft PR #35를 열었다. develop 대상과 1단계 커밋을 확인했다.

- 2단계 자체 리뷰를 완료했다. 목록 검색·페이징과 카테고리 API 및 쿼리 키에 타입을 연결하고, Node 테스트의 변경된 모듈 경로를 갱신했다.
- 타입 검사, 린트, 빌드, 기존 테스트 26개가 통과했다. 격리된 백엔드 URL을 요구하는 테스트 1개는 미실행했다.
- Chrome 157에서 격리된 API 응답으로 목록 로딩, 검색어와 카테고리 동시 적용, 페이지 이동과 새로고침 후 URL 복원, 빈 결과, 404 및 500 재시도를 검증했다. GET 쿼리와 응답을 확인했고 JavaScript 예외는 없었다.

- 3단계에서 게시글 상세·작성·수정, 좋아요·댓글, 첨부, 라우터와 공통 소스를 TypeScript로 전환했다.
- 타입 검사, 린트, 빌드, 기존 테스트 26개가 통과했다. Chrome에서 상세 PDF 링크, 첨부 검증 및 전송, 400 이후 본문·첨부 보존과 재시도, 수정 시 저장된 첨부 보존, 댓글 작성과 정렬을 격리된 API 응답으로 확인했다.
- 상세에서 좋아요와 댓글에 동일한 React key를 주는 기존 경고를 기준 브랜치에서도 확인했다. 인증 세대별 재마운트 동작을 최종 단계에서 검토한다.

- 4단계에서 회원가입·로그인, 세션·권한 경계와 나머지 모듈을 전환했다. 기존 JS·JSX 앱 소스 30개를 모두 전환하고 임시 allowJs를 제거했다.
- 독립 리뷰에서 기존 Node 지원 범위의 테스트 실행 문제가 발견됐다. tsx 기반 npm test와 README 명령을 반영했고 Architect 재리뷰에서 추가 필수 지적은 없었다. 실제 Node 20.19.5에서 로컬 테스트 26개가 통과했다.
- 타입 검사, 린트, 빌드와 diff 검사가 통과했다. 격리 Spring·H2·전용 Redis의 실제 HTTP 테스트를 포함해 총 36개가 통과했다. 만료 토큰 재발급, 권한, Redis 장애 응답과 로그아웃을 확인했다.
- Chrome 157을 격리 Spring에 연결해 회원가입·로그인, 검색·필터, PDF를 포함한 작성과 기존 PDF를 보존하는 수정, 댓글·좋아요, 탭 간 로그아웃을 확인했다. 응답 31건에서 요청·응답과 화면을 대조했고 JavaScript 예외는 없었다. 기준 브랜치의 상세 React key 중복 경고가 재현됐다. 404·500과 첨부 오류 후 입력 보존은 Chrome에서 격리된 응답을 주입해 추가 확인했다.
- 기존 [PR #35](https://github.com/marcel615/Ask-Teacher-react/pull/35)에서 전체 전환을 검토한다.
