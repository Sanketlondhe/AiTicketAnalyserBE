package com.example.aiticketanalyser.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.aiticketanalyser.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "CONTROLLER");
    }

    @Around("execution(* com.example.aiticketanalyser.service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "SERVICE");
    }

    private Object logExecution(ProceedingJoinPoint joinPoint,
                                 String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName()
                + "." + signature.getName() + "()";

        long start = System.currentTimeMillis();
        log.debug("[{}] >>> Entering: {}", layer, methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[{}] <<< Completed: {} in {}ms",
                    layer, methodName, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("[{}] !!! Exception in: {} — {}",
                    layer, methodName, ex.getMessage());
            throw ex;
        }
    }
}
