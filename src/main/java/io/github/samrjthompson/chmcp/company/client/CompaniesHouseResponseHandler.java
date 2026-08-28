package io.github.samrjthompson.chmcp.company.client;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CompaniesHouseResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompaniesHouseResponseHandler.class);

    private final ResponseBodySanitiser responseBodySanitiser;

    public CompaniesHouseResponseHandler(ResponseBodySanitiser responseBodySanitiser) {
        this.responseBodySanitiser = responseBodySanitiser;
    }

    public void checkStatus(HttpResponse<byte[]> response, final String path) {
        final int statusCode = response.statusCode();
        if (is2xx(statusCode)) {
            return;
        }

        final String body = responseBodySanitiser.sanitise(response.body());
        Optional.ofNullable(HttpStatus.resolve(statusCode)).ifPresentOrElse(status -> {
            switch (status) {
                case BAD_REQUEST -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new BadRequestException(msg);
                }
                case UNAUTHORIZED -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new UnauthorizedException(msg);
                }
                case FORBIDDEN -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new ForbiddenException(msg);
                }
                case NOT_FOUND -> {
                    final String msg = message(status, path, "");
                    LOGGER.info(msg);
                    throw new NotFoundException(msg);
                }
                case TOO_MANY_REQUESTS -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new TooManyRequestsException(msg);
                }
                case INTERNAL_SERVER_ERROR -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new InternalServerErrorException(msg);
                }
                case BAD_GATEWAY, SERVICE_UNAVAILABLE -> {
                    final String msg = message(status, path, body);
                    LOGGER.error(msg);
                    throw new BadGatewayException(msg);
                }
                default -> unexpected(statusCode, path);
            }
        }, () -> unexpected(statusCode, path));
    }

    private static String message(HttpStatus status, final String path, final String body) {
        return "Companies House API returned status [%d (%s)] for path [%s] with body [%s]".formatted(status.value(),
                status.getReasonPhrase(), path, body);
    }

    private static boolean is2xx(final int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private static void unexpected(final int statusCode, final String path) {
        final String msg =
                "Companies House API returned unexpected status code [%d] for path [%s]".formatted(statusCode, path);
        LOGGER.error(msg);
        throw new InternalServerErrorException(msg);
    }
}
