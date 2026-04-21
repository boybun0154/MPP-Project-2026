package controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import utils.HttpUtils;

public class DepartmentController {
    public void create(HttpExchange exchange) throws IOException {
        HttpUtils.sendJson(exchange, 201, "{\"message\":\"Department created (placeholder)\"}");
    }

    public void getAll(HttpExchange exchange) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"data\":[]}");
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Department fetched (placeholder)\"}");
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Department updated (placeholder)\"}");
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Department deleted (placeholder)\"}");
    }
}
