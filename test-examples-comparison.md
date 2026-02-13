# 테스트 코드 예제 비교 및 강의 교안 업데이트

## 📋 비교 결과 요약

옵시디언의 강의 교안과 실제 프로젝트 테스트 코드를 비교한 결과, 다음과 같은 차이점이 발견되었습니다:

### 1. 어노테이션 사용 차이

**강의 교안 예제**:
```java
// 기본적인 JUnit assertions 사용
import static org.junit.jupiter.api.Assertions.*;

assertEquals(30, result);
assertTrue(list.isEmpty());
assertThrows(ArithmeticException.class, () -> {...});
```

**프로젝트 실제 코드**:
```java
// AssertJ의 fluent API 사용
import static org.assertj.core.api.Assertions.*;

assertThat(result).isEqualTo(30);
assertThat(list).isEmpty();
assertThatThrownBy(() -> {...})
    .isInstanceOf(ArithmeticException.class)
    .hasMessage("Expected message");
```

### 2. 테스트 구조 차이

**강의 교안 예제**:
```java
class CalculatorTest {
    @Test
    void add_shouldReturnSum() {
        // Given
        int a = 10;
        int b = 20;

        // When
        int result = calculator.add(a, b);

        // Then
        assertEquals(30, result);
    }
}
```

**프로젝트 실제 코드**:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("StudyLog Service 기본 테스트")
class StudyLogServiceSimpleTest {

    @Mock
    private StudyLogDao studyLogDao;

    @InjectMocks
    private StudyLogService studyLogService;

    @Test
    @DisplayName("ID로 학습 기록 조회 - 성공")
    void getStudyLogById_Success() {
        // Given
        when(studyLogDao.findById(1L)).thenReturn(Optional.of(testStudyLog));

        // When
        StudyLogResponse response = studyLogService.getStudyLogById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(studyLogDao).findById(1L);
    }
}
```

### 3. Mock 사용 패턴 차이

**강의 교안**:
- 기본적인 Mockito when-thenReturn 패턴만 설명

**프로젝트 실제 코드**:
```java
// 다양한 Mockito 패턴 사용
doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
    .when(authentication).getAuthorities();

verify(userDao).saveRefreshToken(eq(1L), eq("token"), any(Timestamp.class));
verify(userDao, never()).save(any(User.class));
```

### 4. 통합 테스트 구조 차이

**프로젝트 실제 코드의 특징**:
- JWT 토큰을 이용한 인증 처리
- @Order 어노테이션으로 테스트 순서 지정
- 실제 API 호출 시뮬레이션

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudyLogIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        // JWT 토큰 획득
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        accessToken = extractTokenFromResponse(loginResult);
    }

    @Test
    @Order(1)
    void createStudyLog_IntegrationTest() throws Exception {
        mockMvc.perform(post("/api/v1/logs")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

## 🔄 강의 교안 개선 제안

### 1. AssertJ 추가

강의 교안에 AssertJ 사용법 추가:
```java
// 기존 JUnit assertions 대신 AssertJ 권장
import static org.assertj.core.api.Assertions.*;

// 더 읽기 쉬운 코드
assertThat(actual).isEqualTo(expected);
assertThat(list).hasSize(3).contains("item1", "item2");
assertThat(value).isBetween(1, 10);
```

### 2. @Nested 클래스 활용

테스트 그룹화 예제 추가:
```java
@DisplayName("StudyLog Controller 테스트")
class StudyLogControllerTest {

    @Nested
    @DisplayName("CREATE 작업")
    class CreateOperations {
        @Test
        void createSuccess() { }

        @Test
        void createValidationFail() { }
    }

    @Nested
    @DisplayName("READ 작업")
    class ReadOperations {
        @Test
        void readAll() { }

        @Test
        void readById() { }
    }
}
```

### 3. JWT 인증 테스트

실제 프로젝트에서 많이 사용하는 JWT 테스트 패턴:
```java
@Test
@DisplayName("JWT 토큰 생성 및 검증")
void jwtTokenTest() {
    // 토큰 생성
    String token = jwtProvider.generateToken("user", "ROLE_USER");

    // 토큰 검증
    assertThat(token).isNotNull();
    assertThat(token.split("\\.")).hasSize(3);

    // 토큰 파싱
    String username = jwtProvider.getUsernameFromToken(token);
    assertThat(username).isEqualTo("user");

    // 만료 검증
    assertThat(jwtProvider.validateToken(token)).isTrue();
}
```

### 4. 실무 팁 추가

**테스트 작성 베스트 프랙티스**:
1. 테스트 메서드명은 명확하게: `메서드명_시나리오_예상결과`
2. Given-When-Then 패턴 준수
3. 한 테스트에 하나의 검증만
4. Mock은 최소한으로
5. @DisplayName으로 테스트 의도 명확히

## 📚 추가 학습 자료

### 실제 프로젝트 테스트 파일 위치
- 단위 테스트: `/src/test/java/com/study/myspringstudydiary/*/service/*Test.java`
- Controller 테스트: `/src/test/java/com/study/myspringstudydiary/*/controller/*Test.java`
- 통합 테스트: `/src/test/java/com/study/myspringstudydiary/integration/*Test.java`
- JWT 테스트: `/src/test/java/com/study/myspringstudydiary/global/security/jwt/*Test.java`

### 테스트 커버리지
현재 프로젝트의 테스트 커버리지:
- **JwtTokenProvider**: 100% (모든 메서드 테스트)
- **AuthService**: 100% (login, signup, refresh, logout)
- **StudyLogService**: 부분 테스트
- **Controllers**: MockMvc를 통한 엔드포인트 테스트

## 결론

강의 교안은 기본적인 테스트 개념을 잘 설명하고 있지만, 실제 프로젝트에서는:
1. AssertJ를 통한 더 읽기 쉬운 검증
2. @Nested를 통한 체계적인 테스트 구조
3. JWT 같은 보안 관련 테스트
4. MockMvc를 통한 실제 API 테스트

이러한 실무 패턴들을 강의 교안에 추가하면 더욱 실용적인 교육 자료가 될 것입니다.