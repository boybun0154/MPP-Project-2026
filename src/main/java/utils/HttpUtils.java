package utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[1024];
            int n;
            while ((n = is.read(chunk)) > 0) buf.write(chunk, 0, n);
            return buf.toString(StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> queryParams(HttpExchange exchange) {
        URI uri = exchange.getRequestURI();
        Map<String, String> result = new HashMap<>();
        String q = uri.getRawQuery();
        if (q == null || q.isEmpty()) return result;
        for (String pair : q.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) result.put(urlDecode(pair), "");
            else result.put(urlDecode(pair.substring(0, eq)), urlDecode(pair.substring(eq + 1)));
        }
        return result;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String body = "{\"error\":" + Json.escape(message) + "}";
        sendJson(exchange, status, body);
    }
}
