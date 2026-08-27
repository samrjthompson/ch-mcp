package io.github.samrjthompson.chmcp.mcp;

import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaLoader.class);

    public String loadFromResources(ClassLoader classLoader, final String path) {
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            if (is == null) {
                final String msg = "Input Stream was null when loading schema from [%s]".formatted(path);
                LOGGER.error(msg);
                throw new InternalServerErrorException(msg);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            final String msg = "Failed to load schema at [%s]".formatted(path);
            LOGGER.error(msg);
            throw new InternalServerErrorException(msg, ex);
        }
    }
}
