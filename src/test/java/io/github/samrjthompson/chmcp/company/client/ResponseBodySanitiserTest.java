package io.github.samrjthompson.chmcp.company.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseBodySanitiserTest {

    private static final int MAX_LOGGED_BODY = 512;
    private static final String WHITESPACE_BODY = "{\n  \"errors\": [\n    \"company-profile-not-found\"\n  ]\n}";

    @InjectMocks
    private ResponseBodySanitiser responseBodySanitiser;

    @ParameterizedTest
    @MethodSource("bodiesAndSanitisedBodies")
    void shouldSanitiseBodyForLogging(byte[] body, final String expected) {
        // given / when
        final String actual = responseBodySanitiser.sanitise(body);

        // then
        assertEquals(expected, actual);
    }

    static Stream<Arguments> bodiesAndSanitisedBodies() {
        return Stream.of(Arguments.of(null, ""), Arguments.of(new byte[0], ""),
                Arguments.of(WHITESPACE_BODY.getBytes(StandardCharsets.UTF_8),
                        "{ \"errors\": [ \"company-profile-not-found\" ] }"),
                Arguments.of("x".repeat(MAX_LOGGED_BODY + 1).getBytes(StandardCharsets.UTF_8),
                        "x".repeat(MAX_LOGGED_BODY) + "..."));
    }
}
