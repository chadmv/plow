package com.breakersoft.plow.thrift;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ThriftAspect {

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
            t.printStackTrace();
            throw new PlowException(0, "Plow operation failed: "  + t);
        }
    }
}
