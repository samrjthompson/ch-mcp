package io.github.samrjthompson.chmcp.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaLoaderTest {

    private final SchemaLoader schemaLoader = new SchemaLoader();

    @Mock
    private ClassLoader classLoader;
    @Mock
    private InputStream inputStream;

    @Test
    void shouldLoadStringFromPath() {
        // given
        final String expected = """
                {
                  "field": "value"
                }""";

        // when
        final String actual =
                schemaLoader.loadFromResources(SchemaLoaderTest.class.getClassLoader(), "mcp/dummy_schema.json");

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenResourceIsMissing() {
        // given
        final String path = "mcp/missing_schema.json";

        // when / then
        assertThrows(
                InternalServerErrorException.class,
                () -> schemaLoader.loadFromResources(SchemaLoaderTest.class.getClassLoader(), path));
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenResourceCannotBeRead() throws IOException {
        // given
        final String path = "mcp/dummy_schema.json";

        when(classLoader.getResourceAsStream(path)).thenReturn(inputStream);
        when(inputStream.readAllBytes()).thenThrow(new IOException("Failed to read"));

        // when / then
        assertThrows(InternalServerErrorException.class, () -> schemaLoader.loadFromResources(classLoader, path));
    }
}