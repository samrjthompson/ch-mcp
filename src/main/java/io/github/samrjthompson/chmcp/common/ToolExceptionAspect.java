package io.github.samrjthompson.chmcp.common;

import io.github.samrjthompson.chmcp.common.exception.ToolException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ToolExceptionAspect {

    private final ToolExceptionMapper toolExceptionMapper;

    public ToolExceptionAspect(ToolExceptionMapper toolExceptionMapper) {
        this.toolExceptionMapper = toolExceptionMapper;
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object translateToolExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException exception) {
            throw new ToolException(toolExceptionMapper.toErrorMessage(exception), exception);
        }
    }
}
