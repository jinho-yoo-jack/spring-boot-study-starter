# Spring Boot 실전 과정 - Theory-First 교안 예시

> **v2.0 Theory-First Approach 적용**
> 이론 60% | 코드 40% 비율로 구성된 교안

## Module 1: Spring Boot의 철학과 아키텍처 이해 (Week 1, Day 1-2)

### 📚 Part 1: Spring Boot의 탄생 배경과 철학 (이론 90%, 코드 10%)

#### 1.1 역사적 맥락 - Spring Boot는 왜 만들어졌는가?

**2000년대 초반의 Java 엔터프라이즈 개발 현실**

2002년, Rod Johnson은 "Expert One-on-One J2EE Design and Development"라는 책에서 당시 Java EE(구 J2EE)의 문제점을 신랄하게 비판했습니다. 당시 엔터프라이즈 Java 개발은 다음과 같은 고통을 겪고 있었습니다:

1. **설정 지옥 (XML Hell)**: 간단한 애플리케이션도 수백 줄의 XML 설정이 필요
2. **무거운 컨테이너**: 애플리케이션 서버(WebLogic, WebSphere)가 수 GB 메모리 점유
3. **복잡한 배포**: WAR/EAR 패키징, 애플리케이션 서버 의존성
4. **느린 개발 사이클**: 수정 → 컴파일 → 패키징 → 배포 → 서버 재시작 (5-10분)

**Spring Framework의 등장 (2003년)**

Spring은 이러한 문제를 해결하기 위해 등장했습니다. "경량 컨테이너"라는 혁신적인 개념으로 Java EE의 복잡성을 해결했지만, 여전히 많은 설정이 필요했습니다.

```xml
<!-- 2010년대 초 Spring 설정의 예 - 단순한 웹 애플리케이션에도 이런 설정들이 필요했음 -->
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
    <!-- 20줄 이상의 추가 설정... -->
</bean>

<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <!-- 30줄 이상의 Hibernate 설정... -->
</bean>
```

**Spring Boot의 혁명 (2014년)**

2014년 4월, Spring Boot 1.0이 출시되며 "Convention over Configuration"이라는 철학을 도입했습니다. 이는 Ruby on Rails에서 영감을 받은 것으로, "합리적인 기본값"을 제공하여 개발자가 설정보다 비즈니스 로직에 집중할 수 있게 했습니다.

#### 1.2 Spring Boot의 핵심 철학

**철학 1: 독선적인 기본값 (Opinionated Defaults)**

Spring Boot는 "대부분의 개발자가 대부분의 경우에 원하는 것"을 기본값으로 제공합니다. 이는 통계적 접근입니다:

- 80%의 웹 애플리케이션은 Tomcat을 사용 → Tomcat을 기본 내장
- 대부분의 REST API는 JSON 사용 → Jackson을 기본 포함
- 대부분의 개발자는 로깅 필요 → Logback 기본 설정

**일상 생활 비유**:
스마트폰을 살 때를 생각해보세요. 예전에는 배터리, 충전기, 이어폰, 케이스를 각각 사야 했지만, 이제는 박스를 열면 바로 사용할 수 있는 상태입니다. Spring Boot도 마찬가지로 "박스를 열면 바로 실행 가능한" 애플리케이션을 제공합니다.

**철학 2: 점진적 복잡성 (Progressive Disclosure of Complexity)**

초보자는 간단하게 시작하고, 전문가는 필요한 만큼 커스터마이징할 수 있습니다:

```
Level 1 (초보자): @SpringBootApplication만으로 시작
    ↓
Level 2 (중급자): application.properties로 설정 조정
    ↓
Level 3 (고급자): @Configuration으로 빈 커스터마이징
    ↓
Level 4 (전문가): Auto-configuration 비활성화 및 완전 제어
```

**철학 3: 프로덕션 준비 (Production Ready)**

개발 단계부터 운영을 고려한 기능들이 내장되어 있습니다:
- Health checks (애플리케이션 상태 모니터링)
- Metrics (성능 지표 수집)
- Externalized configuration (환경별 설정 분리)
- Graceful shutdown (안전한 종료)

#### 1.3 Spring Boot의 아키텍처 이해

**계층적 아키텍처 구조**

```
┌─────────────────────────────────────────┐
│         Your Application Code           │ ← 우리가 작성하는 코드
├─────────────────────────────────────────┤
│         Spring Boot Starters            │ ← 의존성 조합 패키지
├─────────────────────────────────────────┤
│      Spring Boot Auto-Configuration     │ ← 자동 설정 마법
├─────────────────────────────────────────┤
│         Spring Boot Core                │ ← 핵심 기능
├─────────────────────────────────────────┤
│         Spring Framework                │ ← IoC, DI, AOP 등
├─────────────────────────────────────────┤
│              JVM                        │ ← Java 실행 환경
└─────────────────────────────────────────┘
```

**각 계층의 역할과 책임**

1. **Spring Boot Core**: SpringApplication 클래스, 임베디드 서버 관리
2. **Auto-Configuration**: @Conditional 어노테이션 기반 스마트 설정
3. **Starters**: 큐레이션된 의존성 조합 (요리 레시피와 같음)
4. **Actuator**: 프로덕션 모니터링 및 관리 기능

### 📚 Part 2: 의존성 주입의 개념적 이해 (이론 80%, 코드 20%)

#### 2.1 의존성 주입이란 무엇인가?

**문제 상황: 강한 결합의 위험**

전통적인 프로그래밍에서는 클래스가 필요한 의존성을 직접 생성합니다:

```java
// ❌ 강한 결합의 예 - 이론적 설명을 위한 안티패턴
public class OrderService {
    // OrderService가 EmailService를 직접 생성
    private EmailService emailService = new EmailService();

    public void processOrder(Order order) {
        // 주문 처리 로직...
        emailService.sendConfirmation(order);
    }
}
```

**이 접근법의 문제점:**

1. **테스트 어려움**: EmailService가 실제로 이메일을 보내면 테스트할 때마다 메일 발송
2. **확장성 부족**: SMS 알림을 추가하려면 OrderService 코드 수정 필요
3. **재사용성 저하**: EmailService 구현을 바꾸려면 모든 사용처 수정

**일상 생활 비유: 전구와 램프**

```
강한 결합 (전통 방식):
- 램프에 전구가 붙박이로 고정
- 전구가 나가면 램프 전체를 버려야 함
- LED로 교체 불가능

약한 결합 (DI 방식):
- 램프에 표준 소켓 제공
- 백열등, LED, 스마트 전구 모두 사용 가능
- 전구만 교체하면 됨
```

#### 2.2 의존성 주입의 종류와 특징

**1. 생성자 주입 (Constructor Injection) - 권장**

```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    // Spring이 생성자를 통해 의존성 주입
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

**왜 생성자 주입을 권장하는가?**

- **불변성(Immutability)**: final 키워드 사용 가능
- **필수 의존성 보장**: 객체 생성 시점에 모든 의존성 확보
- **테스트 용이성**: 일반 자바 코드로 테스트 가능
- **순환 참조 방지**: 컴파일 타임에 순환 참조 감지

**2. 세터 주입 (Setter Injection) - 선택적 의존성**

선택적 의존성이나 변경 가능한 의존성에만 제한적으로 사용합니다.

**3. 필드 주입 (Field Injection) - 지양**

@Autowired를 필드에 직접 사용하는 방식은 테스트와 디버깅이 어려워 권장하지 않습니다.

#### 2.3 IoC 컨테이너의 동작 원리

**제어의 역전(Inversion of Control)이란?**

전통적인 프로그램:
```
Main → ClassA → ClassB → ClassC
(개발자가 모든 객체 생성과 흐름 제어)
```

IoC 적용:
```
Spring Container → 모든 객체 생성 및 관리
                 → 개발자는 사용만
```

**Hollywood Principle**: "Don't call us, we'll call you"
- 배우(개발자)는 대본(코드)만 준비
- 감독(Spring)이 필요할 때 호출

### 📚 Part 3: 실습 - 첫 번째 REST Controller (이론 50%, 코드 50%)

#### 3.1 REST API 설계 원칙 이해

**REST(Representational State Transfer)의 핵심 원칙**

Roy Fielding의 2000년 박사 논문에서 제안된 REST는 웹의 아키텍처 스타일입니다:

1. **리소스 중심 설계**: URL은 리소스를 나타냄 (동사X, 명사O)
2. **HTTP 메서드 활용**: GET(조회), POST(생성), PUT(수정), DELETE(삭제)
3. **무상태성**: 각 요청은 독립적
4. **표준 HTTP 상태 코드**: 200(성공), 404(없음), 500(서버 오류)

#### 3.2 Controller 구현과 상세 설명

```java
/**
 * REST Controller의 역할과 책임
 *
 * Controller는 MVC 패턴에서 사용자의 요청을 받아 처리하는 진입점입니다.
 * 웹 애플리케이션에서 Controller는 웨이터와 같은 역할을 합니다:
 * - 고객(클라이언트)의 주문(요청)을 받고
 * - 주방(서비스 계층)에 전달하며
 * - 완성된 음식(응답)을 고객에게 서빙합니다.
 */
@RestController  // @Controller + @ResponseBody의 조합
@RequestMapping("/api/v1/logs")  // 기본 URL 경로 설정
public class StudyLogController {

    // final 키워드를 사용하는 이유:
    // 1. 불변성 보장 - 한번 주입된 서비스는 변경 불가
    // 2. 스레드 안전성 - 여러 요청이 동시에 와도 안전
    // 3. 필수 의존성 명시 - 이 컨트롤러는 반드시 StudyLogService가 필요
    private final StudyLogService studyLogService;

    /**
     * 생성자 주입을 사용하는 이유:
     *
     * Spring 4.3부터는 생성자가 하나만 있으면 @Autowired 생략 가능.
     * 이는 Spring이 "합리적인 기본값"을 제공하는 좋은 예입니다.
     *
     * 생성자 주입의 장점:
     * 1. 순환 참조 컴파일 시점 감지
     * 2. 테스트 시 Mock 객체 주입 용이
     * 3. 객체 생성 시점에 모든 의존성 확보
     */
    public StudyLogController(StudyLogService studyLogService) {
        this.studyLogService = studyLogService;
    }

    /**
     * POST 요청 처리 - 리소스 생성
     *
     * HTTP POST는 새로운 리소스를 생성할 때 사용합니다.
     * 성공 시 201 Created 상태 코드를 반환하는 것이 RESTful합니다.
     *
     * @RequestBody는 HTTP 요청 본문의 JSON을 Java 객체로 변환합니다.
     * 이 과정을 역직렬화(Deserialization)라고 하며,
     * Spring은 Jackson 라이브러리를 사용하여 자동으로 처리합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudyLogResponse>> createStudyLog(
            @RequestBody StudyLogCreateRequest request) {

        // 서비스 계층에 비즈니스 로직 위임
        // Controller는 요청/응답 변환만 담당 (단일 책임 원칙)
        StudyLogResponse response = studyLogService.createStudyLog(request);

        // ResponseEntity를 사용하는 이유:
        // 1. HTTP 상태 코드를 명시적으로 설정 가능
        // 2. 헤더 추가 가능 (Location 헤더 등)
        // 3. 더 RESTful한 응답 구성
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 상태 코드
                .body(ApiResponse.success(response));
    }
}
```

### 📝 Module 평가 및 성찰

#### 이론 이해도 체크 (개념적 이해 확인)

1. **Spring Boot가 해결하려던 문제는 무엇인가요?**
   <details>
   <summary>답변 보기</summary>

   Spring Boot는 다음 문제들을 해결하고자 했습니다:
   - XML 설정 지옥에서 벗어나기
   - 빠른 개발 시작 (Quick Start)
   - 마이크로서비스 아키텍처 지원
   - 운영 준비된 애플리케이션 제공
   - 임베디드 서버로 독립 실행 가능
   </details>

2. **의존성 주입이 제공하는 가장 큰 이점은 무엇인가요?**
   <details>
   <summary>답변 보기</summary>

   - 낮은 결합도(Loose Coupling): 컴포넌트 간 의존성 최소화
   - 테스트 용이성: Mock 객체로 쉽게 대체 가능
   - 유연성: 구현체를 런타임에 교체 가능
   - 관심사의 분리: 객체 생성과 사용의 분리
   </details>

3. **REST API에서 POST와 PUT의 차이점을 설명하세요.**
   <details>
   <summary>답변 보기</summary>

   - POST: 새로운 리소스 생성, 멱등성 없음, 201 Created 반환
   - PUT: 기존 리소스 전체 수정, 멱등성 있음, 200 OK 반환
   - 멱등성: 같은 요청을 여러 번 해도 결과가 동일
   </details>

#### 실습 과제 (이론 적용)

**과제: 학습한 개념을 적용한 간단한 도서 관리 API 설계**

요구사항 분석부터 시작하세요:
1. 먼저 도메인 모델을 그려보세요 (종이에 펜으로)
2. RESTful URL을 설계하세요
3. 각 엔드포인트의 요청/응답 형식을 정의하세요
4. 마지막에 코드를 작성하세요

---

## 학습 성과 측정

### 이해도 평가 기준

| 평가 항목 | 비중 | 측정 방법 |
|---------|------|----------|
| 개념 이해 | 40% | 구술 설명 능력 |
| 원리 파악 | 30% | "왜"에 대한 답변 |
| 적용 능력 | 20% | 새로운 상황에 적용 |
| 코드 구현 | 10% | 동작하는 코드 작성 |

### 학습 목표 달성 지표

✅ Spring Boot의 탄생 배경과 철학을 설명할 수 있다
✅ 의존성 주입의 필요성과 작동 원리를 이해한다
✅ IoC 컨테이너의 역할을 설명할 수 있다
✅ REST API 설계 원칙을 이해하고 적용할 수 있다

---

*이 교안은 Theory-First Approach를 적용하여 작성되었습니다.*
*코드는 이론을 뒷받침하는 도구일 뿐, 핵심은 개념의 깊은 이해입니다.*