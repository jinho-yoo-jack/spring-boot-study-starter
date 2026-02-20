package com.study.myspringstudydiary.global;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimerAspect {
    @Around("@annotation(com.study.myspringstudydiary.global.ExecutionTimer)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // ① 메서드 이름 추출
        String methodName = joinPoint.getSignature().toShortString();

        // ② 시작 시간 기록
        long startTime = System.currentTimeMillis();
        log.info("▶ {} 시작", methodName);

        try {
            // ③ 실제 메서드 실행 (여기서 OrderService.createOrder()가 호출됨)
            Object result = joinPoint.proceed();

            // ④ 성공 시: 종료 시간 기록 및 로그
            long endTime = System.currentTimeMillis();
            log.info("◀ {} 완료 ({}ms)", methodName, endTime - startTime);

            // ⑤ 원래 메서드의 반환값을 그대로 돌려줌
            return result;
        } catch (Exception e) {
            // ⑥ 예외 시: 종료 시간 기록 및 에러 로그
            long endTime = System.currentTimeMillis();
            log.error("✖ {} 실패 ({}ms): {}", methodName, endTime - startTime, e.getMessage());

            // ⑦ 예외를 다시 던짐 — 삼키지 않는다!
            throw e;
        }
    }

}
