package io.github.samrjthompson.chmcp.common;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ToolExceptionMapper {

    private static final String BAD_REQUEST_MESSAGE = "The request to Companies House was invalid";
    private static final String UNAUTHORIZED_MESSAGE = "The Companies House API key was rejected";
    private static final String FORBIDDEN_MESSAGE = "Access to the requested Companies House resource is forbidden";
    private static final String NOT_FOUND_MESSAGE = "The requested company could not be found";
    private static final String TOO_MANY_REQUESTS_MESSAGE = "The Companies House rate limit has been exceeded";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An internal error occurred while executing the tool";
    private static final String BAD_GATEWAY_MESSAGE = "Companies House could not be reached";
    private static final String DEFAULT_MESSAGE = "An unexpected error occurred while executing the tool";

    public String toErrorMessage(RuntimeException exception) {
        return switch (exception) {
            case BadRequestException _ -> BAD_REQUEST_MESSAGE;
            case UnauthorizedException _ -> UNAUTHORIZED_MESSAGE;
            case ForbiddenException _ -> FORBIDDEN_MESSAGE;
            case NotFoundException _ -> NOT_FOUND_MESSAGE;
            case TooManyRequestsException _ -> TOO_MANY_REQUESTS_MESSAGE;
            case InternalServerErrorException _ -> INTERNAL_SERVER_ERROR_MESSAGE;
            case BadGatewayException _ -> BAD_GATEWAY_MESSAGE;
            case ConstraintViolationException constraintViolationException -> toMessage(constraintViolationException);
            default -> DEFAULT_MESSAGE;
        };
    }

    private static String toMessage(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }
}
