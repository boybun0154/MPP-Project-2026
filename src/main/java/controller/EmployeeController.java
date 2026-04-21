package controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import utils.HttpUtils;

public class EmployeeController {
    public void create(HttpExchange exchange) throws IOException {
        HttpUtils.sendJson(exchange, 201, "{\"message\":\"Employee created (placeholder)\"}");
    }

    public void getAll(HttpExchange exchange) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"data\":[]}");
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Employee fetched (placeholder)\"}");
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Employee updated (placeholder)\"}");
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Employee deleted (placeholder)\"}");
    }
}
