# 현재 작업

초기 이관 시점의 Backend와 Frontend 기록입니다. 다음 작업부터 이 파일은 해당 Jira 키의 현재 기록으로 갱신합니다.

## Backend 이관 기록

# Current Task

## 작업과 승인 근거

- Task: [ASK-16 게시글 상세에 현재 사용자의 작성자 여부 제공](https://marcel615.atlassian.net/browse/ASK-16).
- 2026-11-18 사용자 요청 `애스크티처 백엔드 ASK-16 작업하자`에 따른 Backend 개발이다. Jira의 프로젝트 ASK, 유형 작업, 역할 라벨 `area:backend`, 사용자 착수 가능 상태와 선행 ASK-15 완료를 Main이 확인했다. 댓글은 0건이다.
- Main은 실제 착수 직전 상태와 가능한 전환을 확인하고 ASK-16을 진행 중으로 전환한 뒤 재조회했다. Jira 개발 상태와 PR 링크 외의 본문·댓글 변경은 이번 범위가 아니다.
- Main은 Architect의 최소 DTO·Service 변경, 기존 공개 조회·인증·인가·캐시 설정 유지와 회귀 검증 설계를 승인했다. 공식 계약·DB·인증 정책·운영 파일 변경 승인은 아니다.
- 이전 Backend #79의 PR #80이 develop에 실제 병합된 사실과 월별 devlog 반영을 Main이 확인했다. 이전 작업 기록을 새 ASK-16 설계로 전환한다.
- 공통 운영 루트: `ai-dlc`. 대상 저장소: `Ask-Teacher-spring`. 대상 설정: 저장소 루트 `ai-dlc.yaml`의 활성 서비스 `askteacher`.
- 브랜치: `feature/issue-ASK-16-post-detail-is-author` → `develop`. Main이 최신 기준 `457d8fecad180b8610523fb8a2e5d99baec7eed8`과 clean 작업 트리를 확인하고 브랜치를 생성했다.

## 공식 원본과 확인 버전

Specs `main` 확인 SHA는 `bb06abd9d2557a37ca19ae1bde3bc1b23f878357`이다. Task에 적힌 계약 병합 SHA `7e0fa2e787a9ecaaa2b7d63ed60983d0d113a017`는 이 commit의 ancestor이고 아래 네 공식 문서는 두 commit 사이 변경이 없다. 설정된 공식 원본을 읽기 전용으로 확인했으며 Backend에 원문을 복사하지 않는다.

- [요구사항](https://github.com/marcel615/Ask-Teacher-specs/blob/bb06abd9d2557a37ca19ae1bde3bc1b23f878357/docs/requirements.md).
- [API](https://github.com/marcel615/Ask-Teacher-specs/blob/bb06abd9d2557a37ca19ae1bde3bc1b23f878357/docs/openapi.yaml).
- [인증·세션 정책](https://github.com/marcel615/Ask-Teacher-specs/blob/bb06abd9d2557a37ca19ae1bde3bc1b23f878357/docs/auth-policy.md).
- [ERD](https://github.com/marcel615/Ask-Teacher-specs/blob/bb06abd9d2557a37ca19ae1bde3bc1b23f878357/docs/schema.dbml).

공식 계약은 공개 상세 조회를 유지하고, 성공 응답에 필수 불리언 `isAuthor`를 항상 제공하도록 정한다. 유효하게 인증된 작성자만 true이며 타인과 익명은 false다. 만료·유효하지 않은 Bearer는 401, 인증 서비스 장애는 503이며 Cache-Control에는 no-store를 포함한다. isAuthor는 화면 정보이며 수정·삭제 서버 인가를 대신하지 않는다.

## 목표와 구현 범위

1. `PostDetailResponse`에 primitive `boolean isAuthor`를 추가한다. 기존 상세 필드·첨부파일 응답을 보존하고 JSON 키가 정확히 `isAuthor`이며 false도 항상 포함되게 한다.
2. `PostService.getPost(postId, userId)`에서 조회된 작성자 ID와 유효하게 인증된 사용자 ID를 비교한다. `userId != null && userId.equals(post.getUser().getId())`로 계산해 DTO factory에 전달한다. 좋아요 여부와 작성자 여부를 독립적으로 계산한다.
3. 기존 Controller의 선택적 `@AuthenticationPrincipal` 전달과 Repository의 작성자·카테고리 동시 조회를 재사용한다. 추가 사용자 조회·DB 컬럼·권한 추상화·DTO overload 확장을 도입하지 않는다.
4. Spring Security의 기존 cache-control 기본 설정을 유지하고 실제 상세 HTTP 응답의 no-store를 테스트한다. 중복 Controller 헤더나 전역 보안 재구성 없이 기존 계약을 보존한다.
5. 기존 JWT 서명·만료·현재 Redis 세션 검증, 인증 오류 JSON과 401/503 동작, 공개 GET 허용 및 수정·삭제 인증·작성자 검증을 유지한다.

예상 제품 변경은 `PostDetailResponse.java`와 `PostService.java`다. DTO 생성자를 사용하는 테스트 및 관련 단위·HTTP·통합 테스트를 함께 갱신한다. 기존 수정·삭제 동작은 회귀 검증으로 확인한다.

## 계약 보존과 대안 검토

- Controller는 이미 인증 사용자 ID를 전달하고 Repository는 작성자를 함께 조회한다. Service의 ID 비교로 요구사항을 충족하므로 별도 작성자 조회 API·DB 필드·사용자 ID 응답 확장은 불필요하다.
- 프론트엔드의 닉네임 비교나 화면 정보만으로 수정·삭제를 허용하는 대안은 서버가 계산한 현재 사용자 정보와 서버 인가 계약을 보장하지 못한다. 기존 서버 인증·작성자 검증을 그대로 둔다.
- 모든 공개 요청에서 토큰 실패를 익명으로 취급하는 대안은 명세의 401/503과 충돌하므로 채택하지 않는다. 기존 필터를 재사용한다.
- 캐시 방지를 위한 새 전역 필터나 Security 설정 변경 대신 기존 Security cache-control을 실제 HTTP 테스트로 확인한다.
- 공식 요구사항·API·인증·ERD 변경은 필요하지 않다. 구현 중 변경 필요성이 발견되면 기존 계약을 지키는 대안을 먼저 검토하고 로컬 작업 로그에 사유·대안·한계·영향·원본 버전을 남겨 Main에 보고한다. 영향받는 작업은 별도 계약 관리 결과와 재승인·재검증 전 보류한다.

## 제외 범위

- 공식 Specs 및 Backend 사본의 공식 계약화, DB·Entity 구조·Repository 쿼리 변경.
- JWT·Redis 세션 정책·서버 인가 변경, 비밀값·중요 환경 설정·배포 변경.
- Frontend 화면 구현·연동·배포와 ASK-14의 상태 변경 또는 기능 전체 완료 주장.
- AGENTS.md·Skill·Agent·ai-dlc.yaml·워크플로·리뷰 기준·템플릿 등 AI-DLC 운영 파일 변경.
- 개발 상태·PR 링크를 제외한 Jira Task 본문·댓글 수정, 개발 중 후속 Jira 작업 생성, 사용자 대신 PR merge.

## 검증 계획과 완료 조건

- `PostServiceTest`: 작성자 true, 타인 false, 익명 false, 좋아요 여부와 작성자 여부의 독립성, 없는 게시글 오류 및 기존 상세 필드 보존을 검증한다.
- `PostControllerTest`: 선택적 principal 전달, JSON의 필수 불리언 isAuthor와 정확한 필드명, false 포함 및 Cache-Control의 no-store를 확인한다.
- 실제 Security·Service·DB를 연결한 API 통합 테스트: 같은 게시글에 작성자·타인·익명·작성자 순서로 접근해 사용자별 값과 no-store를 확인한다. 기존 첨부·상세 필드, 없는 글·삭제된 글의 404를 유지한다.
- 상세 경로에 만료·유효하지 않은·폐기된 Bearer를 보내 401을 검증한다. 인증 서비스 장애의 503과 토큰 없는 공개 조회를 격리된 장애 통합 환경에서 확인한다.
- 직접 PATCH·DELETE 요청에 익명 401, 타인 403, 작성자 성공을 검증하고 거부된 요청이 게시글·첨부파일을 변경하지 않음을 확인한다.
- 기존 H2·임시 업로드 경로와 개발 Redis와 분리된 테스트 Redis(`TEST_REDIS_PORT`, 기본 16379)를 사용한다. 고정 운영 데이터·수동 서버·테스트 실행 순서에 의존하지 않고 생성한 테스트 자원을 정리한다.
- 관련 테스트 후 설정된 Windows 기본 명령 `.\gradlew.bat test`를 실제 실행한다. 실행 결과·실패·건너뜀·미검증 사항과 API별 대응 테스트를 작업 로그에 기록한다. 실행 전 통과로 간주하지 않는다.
- `git diff --check`, 승인 범위·보호 파일 diff, Architect 독립 리뷰와 devlog 반영을 확인한다. PR은 develop 대상으로 생성하고 최종 push head의 CI·리뷰·충돌 및 머지 전 필수 검증을 Main이 확인한다. PR 준비 완료와 머지 준비 완료를 구분한다.

## 현재 게이트

- 설계: Main 승인 완료. 추가 사용자 결정과 계약 변경 필요성은 확인되지 않았다.
- 구현·테스트·독립 리뷰·devlog·PR·최종 head CI: 후속 단계에서 실제 결과를 확인한다.

---

## Frontend 이관 기록

# ASK-21 React TypeScript 전환

- [ASK-21](https://marcel615.atlassian.net/browse/ASK-21)은 유일한 프론트엔드 착수 가능 작업이었다. 박민재가 2026-11-23 13:59 KST에 착수 가능으로 전환했다. 이전 ASK-18은 완료됐으며 [PR #34](https://github.com/marcel615/Ask-Teacher-react/pull/34) 병합과 월별 devlog를 확인했다.
- React develop의 원격 기준은 ab42444367ccf33d7fca8ad7dcb036ddbb27162e다. 같은 작업 브랜치와 PR에서 앱 소스 30개를 단계적으로 전환한다.
- 1단계는 엄격한 TypeScript 검사와 JS·JSX 공존, TS·TSX 린트, Vite 개발·빌드 검증이다. 완료하면 Draft PR을 연다.
- 2단계는 게시글 목록, 카테고리, 검색, 페이징이다. 3단계는 다른 게시글 화면, 댓글, 첨부 및 공통 코드다. 4단계는 인증·세션과 최종 검증이다.
- 기존 API·화면 동작과 공식 계약을 유지한다. CSS·정적 자산은 기존 형식을 사용한다.
- 단계별 타입 검사·린트·빌드와 합의한 브라우저 흐름을 검증한다. 기존 로컬 인증·첨부 테스트는 회귀 확인에 재사용한다. 백엔드 연동 테스트는 격리된 대상 환경의 설정 여부를 확인한다.

## 공식 기준

Specs main ee0ef5313acab5887ee11b59272c29d88af9f504의 [요구사항](https://github.com/marcel615/Ask-Teacher-specs/blob/ee0ef5313acab5887ee11b59272c29d88af9f504/docs/requirements.md), [OpenAPI](https://github.com/marcel615/Ask-Teacher-specs/blob/ee0ef5313acab5887ee11b59272c29d88af9f504/docs/openapi.yaml), [인증 정책](https://github.com/marcel615/Ask-Teacher-specs/blob/ee0ef5313acab5887ee11b59272c29d88af9f504/docs/auth-policy.md), [UI 명세](https://github.com/marcel615/Ask-Teacher-specs/blob/ee0ef5313acab5887ee11b59272c29d88af9f504/docs/ui-spec.md), [ERD](https://github.com/marcel615/Ask-Teacher-specs/blob/ee0ef5313acab5887ee11b59272c29d88af9f504/docs/schema.dbml)를 적용한다.
