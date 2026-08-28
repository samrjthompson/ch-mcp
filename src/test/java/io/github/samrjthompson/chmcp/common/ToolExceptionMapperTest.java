package io.github.samrjthompson.chmcp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToolExceptionMapperTest {

    private static final String MESSAGE = "message";

    @InjectMocks
    private ToolExceptionMapper toolExceptionMapper;

    @Mock
    private ConstraintViolation<?> firstConstraintViolation;
    @Mock
    private ConstraintViolation<?> secondConstraintViolation;

    @ParameterizedTest
    @MethodSource("exceptionsAndExpectedMessages")
    void shouldMapExceptionToFixedMessage(RuntimeException exception, final String expectedMessage) {
        // given / when
        final String actual = toolExceptionMapper.toErrorMessage(exception);

        // then
        assertEquals(expectedMessage, actual);
    }

    static Stream<Arguments> exceptionsAndExpectedMessages() {
        return Stream.of(Arguments.of(new BadRequestException(MESSAGE), "The request to Companies House was invalid"),
                Arguments.of(new UnauthorizedException(MESSAGE), "The Companies House API key was rejected"),
                Arguments.of(new ForbiddenException(MESSAGE),
                        "Access to the requested Companies House resource is forbidden"),
                Arguments.of(new NotFoundException(MESSAGE), "The requested company could not be found"),
                Arguments.of(new TooManyRequestsException(MESSAGE), "The Companies House rate limit has been exceeded"),
                Arguments.of(new InternalServerErrorException(MESSAGE),
                        "An internal error occurred while executing the tool"),
                Arguments.of(new BadGatewayException(MESSAGE), "Companies House could not be reached"),
                Arguments.of(new RuntimeException(MESSAGE), "An unexpected error occurred while executing the tool"));
    }

    @Test
    void shouldJoinConstraintViolationMessagesWhenMappingConstraintViolationException() {
        // given
        when(firstConstraintViolation.getMessage()).thenReturn("query must not be blank");
        when(secondConstraintViolation.getMessage()).thenReturn("itemsPerPage must be positive");

        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(firstConstraintViolation, secondConstraintViolation));

        // when
        final String actual = toolExceptionMapper.toErrorMessage(exception);

        // then
        assertTrue(actual.contains("query must not be blank"));
        assertTrue(actual.contains("itemsPerPage must be positive"));
    }
}
