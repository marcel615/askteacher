package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostDetailApiIntegrationTest extends ApiTestSupport {
    @Autowired
    JwtTokenProvider tokens;
    @Value("${jwt.secret}")
    String signingKey;

    @Nested
    @DisplayName("현재 조회자의 작성자 정보")
    class Authorship {
        @DisplayName("같은 글을 사용자별로 반복 조회해도 작성자 여부와 첨부파일을 정확히 반환한다")
        @Test
        void detailReflectsEachViewerWithoutCaching() throws Exception {
            String authorToken = signupAndLogin("author");
            String readerToken = signupAndLogin("reader");
            byte[] attachment = {1, 2, 3};
            long postId = body(mvc.perform(createRequest()
                            .file(new MockMultipartFile("files", "question.png", "image/png", attachment))
                            .header("Authorization", "Bearer " + authorToken))
                    .andExpect(status().isCreated())).get("postId").asLong();
            String storedName = files.findByPostIdOrderByCreatedAtAsc(postId).get(0).getStoredFileName();

            mvc.perform(post("/api/posts/{id}/likes", postId)
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isOk());

            assertDetail(postId, authorToken, true, false, storedName);
            assertDetail(postId, readerToken, false, true, storedName);
            assertDetail(postId, null, false, false, storedName);
            assertDetail(postId, authorToken, true, false, storedName);
        }

        @DisplayName("없는 글과 삭제된 글은 로그인 여부와 관계없이 404를 반환한다")
        @Test
        void missingAndDeletedPostsRemainNotFound() throws Exception {
            String authorToken = signupAndLogin("author");
            long postId = createPost(authorToken);
            mvc.perform(delete("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + authorToken))
                    .andExpect(status().isNoContent());

            for (long unavailableId : List.of(postId, Long.MAX_VALUE)) {
                for (String token : List.of("", authorToken)) {
                    MockHttpServletRequestBuilder request = get("/api/posts/{id}", unavailableId);
                    if (!token.isEmpty()) {
                        request.header("Authorization", "Bearer " + token);
                    }
                    mvc.perform(request)
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404))
                            .andExpect(jsonPath("$.message").value(ErrorCode.POST_NOT_FOUND.getMessage()))
                            .andExpect(jsonPath("$.isAuthor").doesNotExist());
                }
            }
        }

        private void assertDetail(
                long postId,
                String token,
                boolean isAuthor,
                boolean likedByMe,
                String storedName
        ) throws Exception {
            MockHttpServletRequestBuilder request = get("/api/posts/{id}", postId);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", containsString("no-store")))
                    .andExpect(jsonPath("$.isAuthor").isBoolean())
                    .andExpect(jsonPath("$.isAuthor").value(isAuthor))
                    .andExpect(jsonPath("$.author").doesNotExist())
                    .andExpect(jsonPath("$.likedByMe").value(likedByMe))
                    .andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.postId").value(postId))
                    .andExpect(jsonPath("$.userName").value("author"))
                    .andExpect(jsonPath("$.categoryName").value("Test Java"))
                    .andExpect(jsonPath("$.title").value("Spring question"))
                    .andExpect(jsonPath("$.content").value("question body"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.files.length()").value(1))
                    .andExpect(jsonPath("$.files[0].storedFileName").value(storedName))
                    .andExpect(jsonPath("$.files[0].contentType").value("image/png"))
                    .andExpect(jsonPath("$.files[0].fileUrl").value("/files/" + storedName));
        }
    }

    @Nested
    @DisplayName("공개 조회의 인증 실패와 수정·삭제 인가")
    class Authorization {
        @DisplayName("잘못된 형식·서명·만료·폐기 토큰을 익명 조회로 처리하지 않는다")
        @Test
        void invalidExpiredAndRevokedBearerTokensAreUnauthorized() throws Exception {
            String authorToken = signupAndLogin("author");
            long postId = createPost(authorToken);
            User author = users.findByEmail("author@example.com").orElseThrow();
            String sessionId = tokens.verifyToken(authorToken).orElseThrow().sessionId();
            JwtTokenProvider expiredTokens = new JwtTokenProvider(signingKey, Duration.ofMinutes(-1));
            JwtTokenProvider otherSigner = new JwtTokenProvider(
                    "other-only-v7KqX2CbM9HsR5JdW4PyT8LzF3An",
                    Duration.ofMinutes(15)
            );
            List<String> invalidTokens = List.of(
                    "invalid",
                    "",
                    otherSigner.createAccessToken(author.getId(), author.getRole(), sessionId),
                    expiredTokens.createAccessToken(author.getId(), author.getRole(), sessionId)
            );
            for (String invalidToken : invalidTokens) {
                assertUnauthorizedDetail(postId, invalidToken);
            }

            sessionStore.logoutAccess(author.getId(), sessionId);
            assertUnauthorizedDetail(postId, authorToken);
            mvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isAuthor").value(false));
        }

        @DisplayName("익명과 비작성자의 직접 수정·삭제를 거부하고 작성자만 변경할 수 있다")
        @Test
        void directWritesStillRequireTheAuthorAndPreserveRejectedData() throws Exception {
            String authorToken = signupAndLogin("author");
            String readerToken = signupAndLogin("reader");
            byte[] originalBytes = {9, 8};
            long postId = body(mvc.perform(createRequest()
                            .file(new MockMultipartFile("files", "original.png", "image/png", originalBytes))
                            .header("Authorization", "Bearer " + authorToken))
                    .andExpect(status().isCreated())).get("postId").asLong();
            Path originalFile = Path.of(files.findByPostIdOrderByCreatedAtAsc(postId).get(0).getFilePath());

            for (String token : List.of("", readerToken)) {
                for (HttpMethod method : List.of(HttpMethod.PATCH, HttpMethod.DELETE)) {
                    AbstractMockHttpServletRequestBuilder<?> request;
                    if (method == HttpMethod.PATCH) {
                        request = multipart(HttpMethod.PATCH, "/api/posts/{id}", postId)
                                .file(new MockMultipartFile("files", "rejected.png", "image/png", new byte[]{1}))
                                .param("categoryId", categoryId.toString())
                                .param("title", "rejected")
                                .param("content", "rejected body");
                    } else {
                        request = delete("/api/posts/{id}", postId);
                    }
                    if (!token.isEmpty()) {
                        request.header("Authorization", "Bearer " + token);
                    }
                    int expectedStatus = token.isEmpty() ? 401 : 403;
                    mvc.perform(request)
                            .andExpect(status().is(expectedStatus))
                            .andExpect(jsonPath("$.status").value(expectedStatus));

                    Post unchanged = posts.findById(postId).orElseThrow();
                    assertThat(unchanged.getTitle()).isEqualTo("Spring question");
                    assertThat(unchanged.getContent()).isEqualTo("question body");
                    assertThat(unchanged.isDeleted()).isFalse();
                    assertThat(files.findByPostIdOrderByCreatedAtAsc(postId)).hasSize(1);
                    assertThat(Files.readAllBytes(originalFile)).containsExactly(originalBytes);
                    try (Stream<Path> paths = Files.list(uploadDirectory)) {
                        assertThat(paths.toList()).containsExactly(originalFile);
                    }
                }
            }

            mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", postId)
                            .param("categoryId", categoryId.toString())
                            .param("title", "changed by author")
                            .param("content", "updated body")
                            .header("Authorization", "Bearer " + authorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("changed by author"));
            mvc.perform(delete("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + authorToken))
                    .andExpect(status().isNoContent());
            assertThat(posts.findById(postId).orElseThrow().isDeleted()).isTrue();
        }

        private void assertUnauthorizedDetail(long postId, String token) throws Exception {
            mvc.perform(get("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.isAuthor").doesNotExist());
        }
    }
}
