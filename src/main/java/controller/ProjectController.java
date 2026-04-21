package controller;

import com.sun.net.httpserver.HttpExchange;
import config.ServiceRegistry;
import model.Project;
import service.interfaces.IProjectService;
import utils.HttpUtils;
import utils.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectController {
    private final IProjectService service;

    public ProjectController() {
        this(ServiceRegistry.get().projects());
    }

    public ProjectController(IProjectService service) {
        this.service = service;
    }

    public void create(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Project p = new Project();
        applyBody(p, body);
        service.create(p);
        HttpUtils.sendJson(exchange, 201, Json.of(p));
    }

    public void getAll(HttpExchange exchange) throws IOException {
        List<String> items = service.getAll().stream().map(Json::of).collect(Collectors.toList());
        HttpUtils.sendJson(exchange, 200, Json.array(items));
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Project> found = service.getById(parsed);
        if (found.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Project not found: " + id);
            return;
        }
        HttpUtils.sendJson(exchange, 200, Json.of(found.get()));
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Project> existing = service.getById(parsed);
        if (existing.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Project not found: " + id);
            return;
        }
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Project p = existing.get();
        applyBody(p, body);
        service.update(parsed, p);
        HttpUtils.sendJson(exchange, 200, Json.of(p));
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        service.delete(parsed);
        HttpUtils.sendJson(exchange, 200, "{\"id\":" + parsed + ",\"deleted\":true}");
    }

    public void hrCost(HttpExchange exchange, String id) throws IOException {
        try {
            int projectId = Integer.parseInt(id);
            BigDecimal cost = service.calculateProjectHRCost(projectId);
            HttpUtils.sendJson(exchange, 200,
                    "{\"projectId\":" + projectId + ",\"hrCost\":" + cost.toPlainString() + "}");
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid project id: " + id);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 404, e.getMessage());
        }
    }

    private void applyBody(Project p, Map<String, String> body) {
        if (body.containsKey("name")) p.setName(body.get("name"));
        if (body.containsKey("description")) p.setDescription(body.get("description"));
        if (body.containsKey("startDate") && body.get("startDate") != null)
            p.setStartDate(LocalDate.parse(body.get("startDate")));
        if (body.containsKey("endDate") && body.get("endDate") != null)
            p.setEndDate(LocalDate.parse(body.get("endDate")));
        if (body.containsKey("budget") && body.get("budget") != null)
            p.setBudget(new BigDecimal(body.get("budget")));
        if (body.containsKey("status")) p.setStatus(body.get("status"));
    }

    private Long parseId(HttpExchange exchange, String id) throws IOException {
        try { return Long.parseLong(id); }
        catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid id: " + id);
            return null;
        }
    }
}
