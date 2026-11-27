# Ask Teacher Frontend

Ask Teacher는 사용자가 질문 게시글을 작성하고 조회할 수 있는 Q&A 기반 서비스입니다. 이 디렉터리는 Ask Teacher의 React 프론트엔드 프로젝트이며, Spring Boot 백엔드 API와 연동해 회원가입, 로그인, 게시글 CRUD, 카테고리 조회 흐름을 제공합니다.

- Frontend Repository: [frontend/](./)
- Backend Repository: [backend/](../backend/)

## 프로젝트 소개

Ask Teacher Frontend는 사용자가 브라우저에서 질문 게시글을 작성하고 관리할 수 있도록 만든 MVP 화면입니다. React Router로 주요 페이지를 연결하고, Axios 기반 API 모듈을 통해 백엔드의 게시글, 카테고리, 인증 API를 호출합니다.

현재 구현 범위는 게시글 목록/작성/상세/수정/삭제, 회원가입, 로그인, 카테고리 조회와 ASK-14의 메모리 인증 상태·세션 복구·작성자 버튼 제어입니다. 최신 Backend와의 실제 연동 검증은 아래 ASK-14 항목에 구분해 기록합니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Framework | React 19 |
| Build Tool | Vite 8 |
| Routing | React Router DOM 7 |
| API Client | Axios |
| Server State | React Query 5 |
| Forms | React Hook Form 7 |
| Language / Types | JavaScript·JSX / JSDoc·.d.ts (전체 TypeScript 전환 전) |
| Styling | CSS |
| Lint | ESLint |
| Backend | Spring Boot API |

## 주요 기능

- 게시글 목록 조회
- 게시글 상세 조회
- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 게시글 작성/수정 시 카테고리 조회 및 선택
- 회원가입
- 로그인
- 페이지별 로딩/에러 상태 처리
- 게시글 제목/내용/카테고리 입력값 검증

## 화면 및 라우팅

| 경로 | 화면 | 설명 |
|---|---|---|
| `/` | HomePage | 게시글 목록 화면을 기본 화면으로 표시 |
| `/posts` | PostListPage | 게시글 목록 조회 |
| `/posts/create` | PostCreatePage | 게시글 작성 |
| `/posts/:postId` | PostDetailPage | 게시글 상세 조회, 수정/삭제 진입 |
| `/posts/:postId/edit` | PostEditPage | 게시글 수정 |
| `/login` | LoginPage | 로그인 |
| `/signup` | SignupPage | 회원가입 |

## 폴더 구조

```txt
src/
  api/
    api.js
    authApi.js
    postApi.js
    postCategoryApi.js
  component/
    Header.jsx
    PostForm.jsx
  routes/
    AppRouter.jsx
  utils/
    postValidation.js
  HomePage.jsx
  PostListPage.jsx
  PostCreatePage.jsx
  PostDetailPage.jsx
  PostEditPage.jsx
  LoginPage.jsx
  SignupPage.jsx
```

## API 연동 방식

공통 Axios 인스턴스는 `src/api/api.js`에서 관리합니다.

```js
const API_BASE_URL = 'http://localhost:8080/api'
```

API 기능은 도메인별 파일로 분리했습니다.

| 파일 | 역할 |
|---|---|
| `src/api/api.js` | 공통 Axios 인스턴스 |
| `src/api/postApi.js` | 게시글 목록/상세/작성/수정/삭제 API |
| `src/api/postCategoryApi.js` | 카테고리 목록 API |
| `src/api/authApi.js` | 회원가입/로그인 API |

공식 API의 성공 DTO를 직접 읽고, 게시글 목록은 페이지 응답의 `content`를 표시합니다. 인증 요청에는 credential·CSRF를, 게시글 변경에는 Bearer 인증과 multipart 요청을 사용합니다. 삭제·로그아웃은 본문 없는 성공 응답을 처리합니다.

공식 명세는 이 저장소의 [specs/](../specs/)에서 관리합니다.

- [API 명세](../specs/openapi.yaml)
- [인증·세션 정책](../specs/auth-policy.md)
- [ERD](../specs/schema.dbml)

## ERD

데이터 구조는 [공식 ERD](../specs/schema.dbml)를 기준으로 확인합니다.

## 실행 방법

### 1. 의존성 설치

```bash
npm install
```

### 2. 백엔드 서버 실행

프론트엔드는 기본적으로 아래 API 서버를 호출합니다.

```txt
http://localhost:8080/api
```

따라서 프론트 실행 전에 백엔드 프로젝트를 로컬에서 실행해야 합니다.

### 3. 프론트 개발 서버 실행

```bash
npm run dev
```

기본 접속 주소는 다음과 같습니다.

```txt
http://localhost:5173
```

### 4. 빌드

```bash
npm run build
```

### 5. 린트

```bash
npm run lint
```

### 6. 타입 검사와 테스트

```bash
npm run typecheck
npm test
```

## 검증 방법

- `npm run build`로 프로덕션 빌드가 통과하는지 확인합니다.
- `npm run lint`로 정적 검사를 확인합니다.
- React 개발 서버와 Spring Boot 서버를 함께 실행한 뒤 브라우저에서 주요 화면을 확인합니다.
- 브라우저 Network 탭에서 요청 URL, Request Payload, Response 구조가 [공식 API 명세](../specs/openapi.yaml)와 일치하는지 확인합니다.
- Console 탭에 불필요한 에러가 없는지 확인합니다.

## 작업 추적

PR과 Jira 이슈 연결 방법은 [공통 개발 안내](../docs/migration.md#작업-기록)를 참고합니다.

## 트러블슈팅

개발 당시 실제로 기록한 문제와 해결 과정을 보존합니다. 아래 과거 응답 래퍼 설명은 현재 공식 API 계약이 아닙니다.

### 게시글 목록 응답 처리 오류

게시글 목록 조회 후 `postList.map is not a function` 에러가 발생했습니다. 백엔드 응답이 배열 자체가 아니라 공통 응답 객체인 `ApiResponse` 형태였기 때문입니다.

해결 방법:

- 백엔드 응답 구조를 확인했습니다.
- 목록/상세 조회처럼 실제 데이터만 필요한 API는 `response.data.data`를 반환하도록 API 모듈을 수정했습니다.

### 삭제 후 뒤로 가기 처리

게시글 삭제 성공 후 단순히 목록으로 이동하면 브라우저 뒤로 가기로 삭제된 상세 페이지에 다시 진입할 수 있었습니다.

해결 방법:

- 삭제 성공 후 `navigate('/posts', { replace: true })`를 사용했습니다.
- 삭제된 상세 페이지가 브라우저 히스토리에 남지 않도록 처리했습니다.

## 현재 제한 사항 및 향후 개선

- 인증·세션 상태와 작성자 버튼 제어는 ASK-14에서 구현했으며, 실제 Backend 연동·브라우저 검증은 아직 필요합니다.
- 댓글 기능
- 검색 및 페이지네이션
- 좋아요 기능
- 파일 업로드
- 공통 UI 컴포넌트 정리
- API base URL 환경 변수 분리

## 배운 점

- 백엔드의 공통 응답 구조를 프론트 API 모듈에서 필요한 형태로 정리하는 방식
- React Router를 사용해 목록, 작성, 상세, 수정 화면을 연결하는 흐름
- 페이지 단위로 로딩, 에러, 제출 중 상태를 관리하는 방식
- 게시글 작성/수정에서 프론트 1차 검증과 백엔드 최종 검증의 역할을 분리하는 방식
- CRUD 요청 이후 사용자 흐름에 맞게 페이지를 이동시키는 방식

## ASK-14 인증 동작

로그인·로그아웃과 새로고침 세션 복구는 공식 CSRF·Refresh 쿠키 계약을 사용합니다. Access Token과 작성 중 입력은 메모리에만 보관하며, 탭 간 인증 조정에는 Web Locks와 BroadcastChannel을 사용합니다. 브라우저 저장소에는 토큰·본문 대신 조정 버전과 중단 상태만 기록합니다. 조정을 사용할 수 없거나 재발급 응답이 유실되면 자동 복구를 중단하고 재로그인을 안내합니다.

게시글 변경은 Bearer 인증과 multipart 요청을 사용하며, 현재 상세 응답의 `isAuthor === true`에서만 수정·삭제 수단을 표시합니다. 로그인 왕복 시 동일 계정으로 확인된 입력만 복원하고, 새로고침·탭 종료·명시적 로그아웃·다른 계정 로그인·성공한 제출에서는 입력을 폐기합니다.

인증 자동 검증은 `npm exec -- tsx --test tests/auth-session.test.mjs`로 실행합니다. `npm test`는 기존 인증·첨부 회귀 테스트를 함께 실행합니다. 빌드·린트와 모의 전송 기반 자동 검증은 실제 Backend 연동을 대신하지 않습니다. ASK-16의 실제 `isAuthor` 제공, 서버 인가, `Cache-Control: no-store` 및 Chrome 화면·Network·Console 검증은 아직 완료하지 않았습니다.
