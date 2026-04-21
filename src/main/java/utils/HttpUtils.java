package utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class HttpUtils {
    private HttpUtils() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public static String extractId(String path, String basePath) {
        if (path.equals(basePath) || path.equals(basePath + "/")) {
            return null;
        }
        String prefix = basePath + "/";
        if (path.startsWith(prefix)) {
            String id = path.substring(prefix.length());
            return id.isBlank() ? null : id;
        }
        return null;
    }
}
