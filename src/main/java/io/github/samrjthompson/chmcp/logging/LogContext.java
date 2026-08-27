package io.github.samrjthompson.chmcp.logging;

import java.util.Map;
import java.util.Objects;
import org.slf4j.MDC;

public final class LogContext {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String QUERY_KEY = "query";
    private static final String TOOL_NAME_KEY = "toolName";
    private static final String UNINITIALISED = "uninitialised";
    private static final LogContext INSTANCE = new LogContext();

    private LogContext() {
    }

    public static void initialise(final String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    public static LogContext get() {
        return INSTANCE;
    }

    public LogContext query(final String query) {
        MDC.put(QUERY_KEY, query);
        return this;
    }

    public LogContext toolName(final String toolName) {
        MDC.put(TOOL_NAME_KEY, toolName);
        return this;
    }

    public static String getRequestId() {
        return Objects.requireNonNullElse(MDC.get(REQUEST_ID_KEY), UNINITIALISED);
    }

    public static Map<String, String> getLogMap() {
        return Objects.requireNonNullElseGet(MDC.getCopyOfContextMap(), Map::of);
    }

    public static void clear() {
        MDC.clear();
    }
}
