package com.study.myspringstudydiary.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Test API Documentation Interface
 * 테스트 관련 API 문서화를 위한 인터페이스
 */
@Tag(name = "Test", description = "테스트 API")
public interface TestControllerApi {

    @Operation(
            summary = "테스트 엔드포인트",
            description = "Swagger가 작동하는지 확인하는 엔드포인트"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공"
            )
    })
    String hello();
}