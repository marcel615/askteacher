# 인증·세션 정책

이 문서는 공식 인증·세션 정책이다. API 경로와 요청·응답은 `openapi.yaml`을 참고한다.

## Access Token과 세션

로그인 성공 시 발급받은 Access Token을 인증이 필요한 API 요청의 `Authorization: Bearer {accessToken}` 헤더에 사용한다. Access Token의 subject에는 사용자 ID가 들어가며 세션 ID와 Redis의 현재 세션을 함께 검증한다.

Access Token은 15분, Refresh Token은 회전마다 최대 7일이며 최초 로그인부터 최대 30일이다. 새 로그인은 이전 세션을 폐기한다. 사용된 현재 세션 토큰을 다시 제출하면 세션을 폐기하고, 교체된 과거 세션 토큰은 현재 세션을 건드리지 않는다. 응답 유실 시 재로그인한다. React의 자동 재발급 및 탭 간 조정은 별도 작업이다.

JWT 서명 키는 환경 변수로 제공하며 키를 변경하면 기존 Access Token과 Refresh 세션이 무효가 되어 재로그인이 필요하다. 이전 Redis 세션 데이터는 TTL에 따라 만료된다.

## CSRF와 쿠키

브라우저는 `GET /api/auth/csrf`로 응답의 `token`을 받고 함께 발급된 HttpOnly CSRF 쿠키를 보낸다. 로그인·재발급·로그아웃 POST에는 이 토큰을 `X-XSRF-TOKEN` 헤더로 보낸다. 토큰은 현재 Refresh 쿠키에 묶이므로 로그인·재발급·로그아웃 뒤에는 `/csrf`를 다시 호출한다. 보안 쿠키의 이름은 `__Host-XSRF-TOKEN`이고 비보안 개발 환경에서는 `XSRF-TOKEN`이다. 허용된 Origin의 credential 요청만 허용한다.

Refresh Token은 `refreshToken` HttpOnly 쿠키(경로 `/api/auth`)이며 본문에 반환하지 않는다. 운영 시 HTTPS와 Secure 쿠키가 필요하다.

## 재발급·로그아웃 및 장애

`POST /api/auth/refresh`는 본문 없이 Refresh 쿠키와 CSRF 헤더를 보낸다. 성공 시 로그인과 같은 JSON 및 회전된 쿠키를 반환한다. 만료·위조·폐기·재사용은 `401`, CSRF 실패는 `403`, Redis 장애는 `503`이다.

`POST /api/auth/logout`은 유효한 Access Token 또는 Refresh 쿠키로 식별한 현재 세션을 폐기하고 Refresh 쿠키를 지운다. 이미 만료·로그아웃 상태도 `204 No Content`이며 CSRF 실패 `403`, Redis 장애 `503`이 우선한다. 로그인에서도 Redis 장애는 `503`이다.
