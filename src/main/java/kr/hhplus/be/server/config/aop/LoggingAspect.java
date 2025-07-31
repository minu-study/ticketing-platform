package kr.hhplus.be.server.config.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("Method: {} started with args: {}", methodName, args);

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("Method: {} completed in {}ms", methodName, endTime - startTime);
            return result;
        } catch (Exception e) {
            log.error("Method: {} failed with exception: {}", methodName, e.getMessage());
            throw e;
        }
    }
}

