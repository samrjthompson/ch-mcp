package io.github.samrjthompson.chmcp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.ToolException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToolExceptionAspectTest {

    private static final String MESSAGE = "message";
    private static final String MAPPED_MESSAGE = "mapped message";
    private static final Object RESULT = new Object();

    @Mock
    private ToolExceptionMapper toolExceptionMapper;

    @InjectMocks
    private ToolExceptionAspect toolExceptionAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Test
    void shouldReturnJoinPointResultWhenNoExceptionIsThrown() throws Throwable {
        // given
        when(proceedingJoinPoint.proceed()).thenReturn(RESULT);

        // when
        final Object actual = toolExceptionAspect.translateToolExceptions(proceedingJoinPoint);

        // then
        assertEquals(RESULT, actual);
    }

    @Test
    void shouldRethrowToolExceptionWithMappedMessageWhenJoinPointThrowsRuntimeException() throws Throwable {
        // given
        RuntimeException thrownException = new RuntimeException(MESSAGE);
        when(proceedingJoinPoint.proceed()).thenThrow(thrownException);
        when(toolExceptionMapper.toErrorMessage(any())).thenReturn(MAPPED_MESSAGE);

        // when
        final ToolException actual = assertThrows(ToolException.class,
                () -> toolExceptionAspect.translateToolExceptions(proceedingJoinPoint));

        // then
        assertEquals(MAPPED_MESSAGE, actual.getMessage());
        assertEquals(thrownException, actual.getCause());
        verify(toolExceptionMapper).toErrorMessage(thrownException);
    }
}
