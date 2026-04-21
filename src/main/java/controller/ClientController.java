package controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import utils.HttpUtils;

public class ClientController {
    public void create(HttpExchange exchange) throws IOException {
        // Placeholder until ClientService#create is implemented.
        HttpUtils.sendJson(exchange, 201, "{\"message\":\"Client created (placeholder)\"}");
    }

    public void getAll(HttpExchange exchange) throws IOException {
        // Placeholder until ClientService#getAll is implemented.
        HttpUtils.sendJson(exchange, 200, "{\"data\":[]}");
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        // Placeholder until ClientService#getById is implemented.
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Client fetched (placeholder)\"}");
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        // Placeholder until ClientService#update is implemented.
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Client updated (placeholder)\"}");
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        // Placeholder for delete + cascade/referential integrity handling.
        HttpUtils.sendJson(exchange, 200, "{\"id\":\"" + id + "\",\"message\":\"Client deleted (placeholder)\"}");
    }
}
