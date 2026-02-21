package com.study.myspringstudydiary.global.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.study.myspringstudydiary.*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint jp) throws Throwable {
        log.info("Request: {}", jp.getSignature());
        Object result = jp.proceed();
        log.info("Response: {}", result);
        return result;
    }
}
