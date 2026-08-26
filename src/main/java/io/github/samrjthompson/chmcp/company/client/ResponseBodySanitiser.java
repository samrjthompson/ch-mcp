package io.github.samrjthompson.chmcp.company.client;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class ResponseBodySanitiser {

    private static final int MAX_LOGGED_BODY = 512;

    public String sanitise(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        final String decoded = new String(bytes, 0, Math.min(bytes.length, MAX_LOGGED_BODY), StandardCharsets.UTF_8)
                .replaceAll("\\s+", " ")
                .trim();

        return bytes.length > MAX_LOGGED_BODY ? decoded + "..." : decoded;
    }
}
