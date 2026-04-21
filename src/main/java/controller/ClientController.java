package controller;

import com.sun.net.httpserver.HttpExchange;
import config.ServiceRegistry;
import model.Client;
import service.interfaces.IClientService;
import utils.HttpUtils;
import utils.Json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientController {
    private final IClientService service;

    public ClientController() {
        this(ServiceRegistry.get().clients());
    }

    public ClientController(IClientService service) {
        this.service = service;
    }

    public void create(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Client c = new Client();
        c.setName(body.get("name"));
        c.setIndustry(body.get("industry"));
        c.setPrimaryContactName(body.get("primaryContactName"));
        c.setPrimaryContactPhone(body.get("primaryContactPhone"));
        c.setPrimaryContactEmail(body.get("primaryContactEmail"));
        service.create(c);
        HttpUtils.sendJson(exchange, 201, Json.of(c));
    }

    public void getAll(HttpExchange exchange) throws IOException {
        List<String> items = service.getAll().stream().map(Json::of).collect(Collectors.toList());
        HttpUtils.sendJson(exchange, 200, Json.array(items));
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Client> found = service.getById(parsed);
        if (found.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Client not found: " + id);
            return;
        }
        HttpUtils.sendJson(exchange, 200, Json.of(found.get()));
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Client> existing = service.getById(parsed);
        if (existing.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Client not found: " + id);
            return;
        }
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Client c = existing.get();
        if (body.containsKey("name")) c.setName(body.get("name"));
        if (body.containsKey("industry")) c.setIndustry(body.get("industry"));
        if (body.containsKey("primaryContactName")) c.setPrimaryContactName(body.get("primaryContactName"));
        if (body.containsKey("primaryContactPhone")) c.setPrimaryContactPhone(body.get("primaryContactPhone"));
        if (body.containsKey("primaryContactEmail")) c.setPrimaryContactEmail(body.get("primaryContactEmail"));
        service.update(parsed, c);
        HttpUtils.sendJson(exchange, 200, Json.of(c));
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        service.delete(parsed);
        HttpUtils.sendJson(exchange, 200, "{\"id\":" + parsed + ",\"deleted\":true}");
    }

    public void findByUpcomingDeadline(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.queryParams(exchange);
        int days;
        try {
            days = Integer.parseInt(q.getOrDefault("days", "30"));
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid 'days' query parameter");
            return;
        }
        try {
            List<String> items = service.findClientsByUpcomingProjectDeadline(days)
                    .stream().map(Json::of).collect(Collectors.toList());
            HttpUtils.sendJson(exchange, 200, Json.array(items));
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    private Long parseId(HttpExchange exchange, String id) throws IOException {
        try { return Long.parseLong(id); }
        catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid id: " + id);
            return null;
        }
    }
}
