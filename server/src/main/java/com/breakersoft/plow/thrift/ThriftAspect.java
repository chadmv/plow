package com.breakersoft.plow.thrift;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;

@Aspect
public class ThriftAspect {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(ThriftAspect.class);

    @Pointcut("within(@com.breakersoft.plow.thrift.ThriftService *)")
    public void thriftService() {}

    @Pointcut("execution(* *(..))")
    public void methodPointcut() {}

    @Around("thriftService() && methodPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        }
        catch (Throwable t) {
            logger.warn("Eception " + joinPoint.getSignature().getName(), t);
            // TODO: translate to all exceptions.
            throw new PlowException(0, "Plow operation failed: "  + t);
        }
    }
}
