package com.study.myspringstudydiary.auth.controller;

import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

/**
 * Authentication API Documentation Interface
 * 인증 관련 API 문서화를 위한 인터페이스
 */
@Tag(name = "인증", description = "회원가입 및 로그인 API - 인증 불필요")
public interface AuthControllerApi {

    @Operation(
            summary = "로그인",
            description = """
                    사용자 인증 후 JWT 토큰을 발급합니다.

                    ### 사용 방법
                    1. 이 API로 로그인하여 JWT 토큰을 받습니다
                    2. 응답의 `data.accessToken` 값을 복사합니다
                    3. Swagger UI 우측 상단 **Authorize** 버튼 클릭
                    4. 토큰 값만 입력 (Bearer는 자동 추가)
                    5. 이제 인증이 필요한 API 사용 가능
                    """,
            security = @SecurityRequirement(name = "")  // 인증 불필요 표시
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 1800
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 - 잘못된 인증 정보",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "code": "UNAUTHORIZED",
                                                "message": "사용자명 또는 비밀번호가 올바르지 않습니다"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody(
                    description = "로그인 요청 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "testuser",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @Valid LoginRequest request
    );

    @Operation(
            summary = "회원가입",
            description = """
                    새로운 사용자를 등록합니다.

                    ### 검증 규칙
                    - **username**: 필수, 3-50자
                    - **email**: 필수, 이메일 형식, 최대 100자
                    - **password**: 필수, 6-100자

                    ### 주의사항
                    - 중복된 username이나 email은 허용되지 않습니다
                    - 비밀번호는 암호화되어 저장됩니다
                    """,
            security = @SecurityRequirement(name = "")  // 인증 불필요 표시
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "userId": 1,
                                                "username": "testuser",
                                                "email": "test@example.com",
                                                "message": "회원가입이 완료되었습니다"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패 또는 중복된 사용자",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "code": "BAD_REQUEST",
                                                "message": "이미 존재하는 사용자명입니다"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResponse<SignupResponse>> signup(
            @RequestBody(
                    description = "회원가입 요청 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "testuser",
                                              "email": "test@example.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @Valid SignupRequest request
    );

    @Operation(
            summary = "토큰 갱신",
            description = """
                    Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

                    ### 사용 시나리오
                    - Access Token이 만료되었을 때 사용
                    - Refresh Token이 유효한 경우 새로운 토큰 쌍을 발급
                    """,
            security = @SecurityRequirement(name = "")  // 인증 불필요 표시
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 1800
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token이 유효하지 않음"
            )
    })
    ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid RefreshTokenRequest request);

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하여 로그아웃 처리합니다.",
            security = @SecurityRequirement(name = "")  // 인증 불필요 표시
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": "Logged out successfully",
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResponse<String>> logout(@Valid RefreshTokenRequest request);
}