package com.example.aiticketanalyser.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // ── Log all controller methods ──────────────────────────────────────────
    @Around("execution(* com.ticket.analyser.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "CONTROLLER");
    }

    // ── Log all service methods ─────────────────────────────────────────────
    @Around("execution(* com.ticket.analyser.service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "SERVICE");
    }

    // ── Shared execution logger ─────────────────────────────────────────────
    private Object logExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName()
                + "." + signature.getName() + "()";

        long start = System.currentTimeMillis();
        log.debug("[{}] >>> Entering: {}", layer, methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("[{}] <<< Completed: {} in {}ms", layer, methodName, elapsed);
            return result;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}] !!! Exception in: {} after {}ms — {}",
                    layer, methodName, elapsed, ex.getMessage());
            throw ex;
        }
    }
}
