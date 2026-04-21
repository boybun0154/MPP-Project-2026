package controller;

import com.sun.net.httpserver.HttpExchange;
import config.ServiceRegistry;
import model.Department;
import service.interfaces.IDepartmentService;
import service.interfaces.IProjectService;
import utils.HttpUtils;
import utils.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DepartmentController {
    private final IDepartmentService service;
    private final IProjectService projectService;

    public DepartmentController() {
        this(ServiceRegistry.get().departments(), ServiceRegistry.get().projects());
    }

    public DepartmentController(IDepartmentService service, IProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    public void create(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Department d = new Department();
        d.setName(body.get("name"));
        d.setLocation(body.get("location"));
        if (body.get("annualBudget") != null) d.setAnnualBudget(new BigDecimal(body.get("annualBudget")));
        service.create(d);
        HttpUtils.sendJson(exchange, 201, Json.of(d));
    }

    public void getAll(HttpExchange exchange) throws IOException {
        List<String> items = service.getAll().stream().map(Json::of).collect(Collectors.toList());
        HttpUtils.sendJson(exchange, 200, Json.array(items));
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Department> found = service.getById(parsed);
        if (found.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Department not found: " + id);
            return;
        }
        HttpUtils.sendJson(exchange, 200, Json.of(found.get()));
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Department> existing = service.getById(parsed);
        if (existing.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Department not found: " + id);
            return;
        }
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Department d = existing.get();
        if (body.containsKey("name")) d.setName(body.get("name"));
        if (body.containsKey("location")) d.setLocation(body.get("location"));
        if (body.containsKey("annualBudget") && body.get("annualBudget") != null)
            d.setAnnualBudget(new BigDecimal(body.get("annualBudget")));
        service.update(parsed, d);
        HttpUtils.sendJson(exchange, 200, Json.of(d));
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        service.delete(parsed);
        HttpUtils.sendJson(exchange, 200, "{\"id\":" + parsed + ",\"deleted\":true}");
    }

    public void listProjects(HttpExchange exchange, String id) throws IOException {
        Map<String, String> q = HttpUtils.queryParams(exchange);
        String sortBy = q.getOrDefault("sortBy", "id");
        try {
            int departmentId = Integer.parseInt(id);
            List<String> items = projectService.getProjectsByDepartment(departmentId, sortBy)
                    .stream().map(Json::of).collect(Collectors.toList());
            HttpUtils.sendJson(exchange, 200, Json.array(items));
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid department id: " + id);
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
